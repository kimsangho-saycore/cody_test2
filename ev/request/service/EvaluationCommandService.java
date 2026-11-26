package kr.innoaca.usr.web.ev.request.service;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.dto.Constants;
import kr.innoaca.core.common.notification.domain.NotificationTemplate;
import kr.innoaca.core.common.notification.dto.NotificationEvaluationResponse;
import kr.innoaca.core.common.notification.event.NotificationEvent;
import kr.innoaca.core.common.notification.event.NotificationMessage;
import kr.innoaca.core.common.notification.mapper.NotificationTemplateMapper;
import kr.innoaca.core.domain.bbs.NtcBbsBasMapper;
import kr.innoaca.core.web.alarm.domain.constant.EventBoundary;
import kr.innoaca.core.web.alarm.domain.constant.EventDetail;
import kr.innoaca.core.web.alarm.dto.NtcBbsBasDTO;
import kr.innoaca.core.web.member.mapper.LocationMapper;
import kr.innoaca.core.web.member.mapper.MemberMapper;
import kr.innoaca.core.web.point.dto.MbrPntHstDto;
import kr.innoaca.core.web.point.service.PointService;
import kr.innoaca.core.domain.guild.GuildActivityTrigger;
import kr.innoaca.core.web.point.service.GuildScoreGranter;
import kr.innoaca.core.common.utils.StringUtil;
import kr.innoaca.core.common.notification.builder.CommonNotificationMessageBuilder;
import kr.innoaca.usr.common.exception.UsrBadRequestException;
import kr.innoaca.usr.evaluation.EvaluationAppendMapper;
import kr.innoaca.usr.evaluation.EvaluationAppender;
import kr.innoaca.usr.evaluation.EvlMatchHistMapper;
import kr.innoaca.usr.evaluation.MentorEvaluationAppendRequest;
import kr.innoaca.usr.evaluation.WeightedReviewerSelector;
import kr.innoaca.usr.evaluation.dto.EvlMatchHistoryDto;
import kr.innoaca.usr.evaluation.dto.MatchResult;
import kr.innoaca.usr.evaluation.dto.AppendReviewValidateRequest;
import kr.innoaca.usr.evaluation.dto.query.AppendedEvaluationResponse;
import kr.innoaca.usr.evaluation.validator.RequestAppendValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import kr.innoaca.usr.evaluation.PartnerSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kr.innoaca.core.common.dto.Constants.*;
import kr.innoaca.core.domain.evaluation.TeamRosterMember;

@Service
@RequiredArgsConstructor
public class EvaluationCommandService {

        private static final Logger log = LoggerFactory.getLogger(EvaluationCommandService.class);

        private final EvaluationAppender evaluationAppender;
        private final WeightedReviewerSelector weightedReviewerSelector;
        private final EvlMatchHistMapper evlMatchHistMapper;
        private final MessageSource messageSource;
        private final EvaluationAppendMapper evaluationAppendMapper;
        private final RequestAppendValidator requestAppendValidator;
        private final MemberMapper memberMapper;
        private final LocationMapper locationMapper;

        private final CommonNotificationMessageBuilder commonNotificationMessageBuilder;
        private final ApplicationEventPublisher applicationEventPublisher;
        private final NtcBbsBasMapper ntcBbsBasMapper;
        private final NotificationTemplateMapper notificationTemplateMapper;
        private final PointService pointService;
        private final GuildScoreGranter guildScoreGranter;
        private final kr.innoaca.core.mapper.evaluation.TeamRosterMapper teamRosterMapper; // #350 팀 로스터(usr·adm 공용)

        /**
         * 요청자 팀의 팀원 전원 각자 소속 길드에 정책 기반 길드 점수를 적립한다 (이슈 #166).
         * 팀 로스터는 팀원 테이블 기준이라 평가자(멘토/전문가)는 애초에 포함되지 않는다.
         */
        private void grantGuildToRequesterTeam(Member member, MentorEvaluationAppendRequest request,
                        GuildActivityTrigger trigger, String referenceId) {
                List<TeamRosterMember> team = teamRosterMapper.selectTeamRoster(
                                request.getProjectNo() == null ? null : String.valueOf(request.getProjectNo()),
                                request.getLcorsNo() == null ? null : String.valueOf(request.getLcorsNo()),
                                request.getUqstnNo() == null ? null : String.valueOf(request.getUqstnNo()),
                                request.getTeamSn() == null ? null : String.valueOf(request.getTeamSn()));
                if (team == null) {
                        return;
                }
                for (TeamRosterMember tm : team) {
                        guildScoreGranter.grant(tm.getInstCd(), trigger, tm.getMbrId(), tm.getRoleCd(),
                                        referenceId, member.getMbrId());
                }
        }

        // 동료 평가 요청 시 요청자가 보유해야 하는 최소 포인트
        // 원래 regReqEvalSave 에 있던 검증이 KAN-97(2024-06-28) 커밋에서 제거되어 복원
        private static final int MIN_POINT_FOR_PARTNER_EVAL_REQUEST = 100;

        public void validate(Member requestMember, AppendReviewValidateRequest request) {
                requestAppendValidator.validate(requestMember, request);
        }

        /**
         * 동료 평가 요청자의 보유 포인트 검증
         * 원본(commit 079d37c1) 에 있던 100 포인트 미만 차단 검증을 복원
         * 프론트 우회 시에도 차단되도록 서비스 진입부에서 검증
         */
        private void validateRequesterPoint(Member member) {
                MbrPntHstDto pntDto = new MbrPntHstDto();
                pntDto.setInstCd(member.getInstCd());
                pntDto.setMbrId(member.getMbrId());
                pntDto = pointService.selectNowPntScr(pntDto);

                Integer nowPntScr = pntDto == null ? null : pntDto.getNowPntScr();
                int currentPoint = nowPntScr == null ? 0 : nowPntScr;

                if (currentPoint < MIN_POINT_FOR_PARTNER_EVAL_REQUEST) {
                        throw new UsrBadRequestException(messageSource.getMessage(
                                        "usr.evreq.msg.insufficientpoint",
                                        new Object[]{MIN_POINT_FOR_PARTNER_EVAL_REQUEST},
                                        LocaleContextHolder.getLocale()));
                }
        }

        /**
         * 평가 요청 전 자기자신의 스케줄 충돌 체크 (사용중/삭제될 스케줄 구분)
         */
        public Map<String, Object> checkOwnScheduleConflicts(Member member, MentorEvaluationAppendRequest request) {
                Map<String, Object> result = new HashMap<>();

                try {
                        List<PartnerSchedule> conflictingSchedules = evaluationAppendMapper.findOwnSchedules(
                                        member.getMbrId(),
                                        member.getInstCd(),
                                        request.getBgngDt(),
                                        request.getBeginTimes());

                        if (conflictingSchedules.isEmpty()) {
                                result.put("hasConflict", false);
                                result.put("hasUsedSchedule", false);
                                result.put("hasDeletableSchedule", false);
                                return result;
                        }

                        // 사용 중인 스케줄과 삭제 가능한 스케줄 구분
                        List<PartnerSchedule> usedSchedules = conflictingSchedules.stream()
                                        .filter(schedule -> schedule.getMtlEvlSn() != null)
                                        .collect(Collectors.toList());

                        List<PartnerSchedule> deletableSchedules = conflictingSchedules.stream()
                                        .filter(schedule -> schedule.getMtlEvlSn() == null)
                                        .collect(Collectors.toList());

                        result.put("hasConflict", true);
                        result.put("hasUsedSchedule", !usedSchedules.isEmpty());
                        result.put("hasDeletableSchedule", !deletableSchedules.isEmpty());

                        // (#227) 삭제될 슬롯의 시각을 함께 준다. 요청을 넣으면 이 슬롯들이 영구 삭제되고
                        // 평가가 끝나도 복구되지 않으므로(기획 결정 2-B), 화면이 "무엇이 사라지는지"를
                        // 구체적으로 보여주고 확인을 받을 수 있어야 한다.
                        if (!deletableSchedules.isEmpty()) {
                                result.put("deletableTimes", deletableSchedules.stream()
                                                .map(PartnerSchedule::getEvlPsblTm)
                                                .collect(Collectors.toList()));
                        }

                        // 사용 중인 스케줄이 있으면 에러 메시지 추가
                        if (!usedSchedules.isEmpty()) {
                                List<String> usedTimes = usedSchedules.stream()
                                                .map(PartnerSchedule::getEvlPsblTm)
                                                .collect(Collectors.toList());
                                result.put("usedTimes", usedTimes);
                                result.put("errorMessage",
                                                String.format("평가 요청 시간(%s)에 이미 사용 중인 본인의 스케줄이 있어 평가 요청을 할 수 없습니다. 다른 시간대를 선택해주세요.",
                                                                String.join(", ", usedTimes)));
                        }

                } catch (RuntimeException e) {
                        log.error("스케줄 충돌 체크 중 오류 발생");
                        result.put("hasConflict", false);
                        result.put("hasUsedSchedule", false);
                        result.put("hasDeletableSchedule", false);
                }

                return result;
        }

        // 동료 평가 요청
        public AppendedEvaluationResponse appendPartnerEvaluationRequest(Member member,
                        MentorEvaluationAppendRequest request) {
                validateRequesterPoint(member);

                AppendedEvaluationResponse result = evaluationAppender.appendPartnerEvaluation(member, request);

                Member reviewrMember = memberMapper.getMember(result.getReviewerMemberId());
                String rgtrRoleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;

                String seatLocation = locationMapper.getLocatoinbyMbrId(member.getMbrId());

                String notificationBody = buildPartnerNotificationBody(member, result, seatLocation);
                savePartnerEvaluationRequestNotification(member.getMbrId(), result.getReviewerMemberId(),
                        member.getInstCd(), notificationBody, rgtrRoleCd);

                String subtitle = messageSource.getMessage("evalreq.msg.evldesnted.title", null,
                                LocaleContextHolder.getLocale());

                // Discord 알림 유형 메시지 구성
                String discordNotificationType = messageSource.getMessage("usr.evreq.msg.regsched2", null, LocaleContextHolder.getLocale());

                // 템플릿 조회
                NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(Constants.TMPLT_CD_6);

                // 빌더 호출 (템플릿 전달)
                NotificationMessage notificationMessage = commonNotificationMessageBuilder.buildContentNotificationMessage(
                        template,
                        result.getMtlEvlSn(),
                        member,
                        reviewrMember,
                        member.getInstCd(),
                        rgtrRoleCd,
                        subtitle,
                        buildPartnerEmailBody(member, result, seatLocation),
                        discordNotificationType);

                applicationEventPublisher.publishEvent(new NotificationEvent(
                                EventBoundary.EVALUATION, EventDetail.REQUEST, notificationMessage));

                // 동료 평가 요청 — 요청 팀원 전원 각자 소속 길드에 적립 (건당 1회)
                grantGuildToRequesterTeam(member, request, GuildActivityTrigger.PEER_EVAL_REQUEST,
                                String.valueOf(result.getMtlEvlSn()));

                return result;
        }

        /**
         * 가중 랜덤 배정 기반 동료 평가 요청 (Phase 3)
         * - WeightedReviewerSelector로 평가자 선정
         * - 매칭 이력 저장
         * - 기존 동료평가 로직 실행 + 알림
         */
        public AppendedEvaluationResponse appendWeightedPartnerEvaluationRequest(Member member,
                        MentorEvaluationAppendRequest request) {
                validateRequesterPoint(member);

                MatchResult matchResult;

                if (request.getReviewerMemberId() != null && request.getReviewerMemberId() > 0) {
                        // 지정 모드: 프론트에서 선택한 평가자 사용 (가중 랜덤 건너뜀)
                        matchResult = null;
                        log.info("[appendWeightedPartnerEvaluationRequest] 지정 모드 - reviewerMemberId={}", request.getReviewerMemberId());
                } else {
                        // 랜덤 모드: 가중 랜덤으로 평가자 선정
                        matchResult = weightedReviewerSelector.selectReviewer(request);
                        request.setReviewerMemberId(matchResult.getSelectedMbrId());
                }

                // scopeCd 조회 (지정 모드에서도 필요)
                String scopeCd = matchResult != null ? matchResult.getScopeCd()
                        : weightedReviewerSelector.resolveScopeCdPublic(request);

                // 동료평가 로직 실행 (EvaluationReviewerSelector 우회)
                AppendedEvaluationResponse result = evaluationAppender.appendWeightedPartnerEvaluation(member, request, scopeCd);

                // 3. 매칭 이력 저장 (랜덤 배정일 때만)
                if (matchResult != null) {
                        try {
                                Integer maxSeq = evlMatchHistMapper.getMaxMatchSeq(result.getEvlNo(), result.getEvlDegr());
                                int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;

                                Member requestorMember = memberMapper.getMember(member.getMbrId());

                                EvlMatchHistoryDto histDto = EvlMatchHistoryDto.builder()
                                        .evlNo(result.getEvlNo())
                                        .evlDegr(result.getEvlDegr())
                                        .mtlEvlSn(result.getMtlEvlSn())
                                        .evlrTypeCd("00002")
                                        .matchCriteria(matchResult.getMatchCriteriaJson())
                                        .candidateList(matchResult.getCandidateListJson())
                                        .candidateCnt(matchResult.getCandidateCnt())
                                        .selectedMbrId(matchResult.getSelectedMbrId())
                                        .selectedMbrNm(matchResult.getSelectedMbrNm())
                                        .selectedInstCd(matchResult.getSelectedInstCd())
                                        .selectedEvalPoint(matchResult.getSelectedEvalPoint())
                                        .selectedEffPoint(matchResult.getSelectedEffPoint())
                                        .selectedWeight(matchResult.getSelectedWeight())
                                        .selectedProbability(matchResult.getSelectedProbability())
                                        .requestorMbrId(Long.parseLong(member.getMbrId()))
                                        .requestorMbrNm(requestorMember != null ? requestorMember.getMbrNm() : null)
                                        .requestorInstCd(member.getInstCd())
                                        .matchTypeCd("AUTO")
                                        .matchSeq(nextSeq)
                                        .rgtrId(member.getMbrId())
                                        .build();

                                evlMatchHistMapper.insertMatchHist(histDto);
                                log.info("[MatchHist] 매칭 이력 저장 완료 - evlNo={}, degr={}, seq={}, selected={}",
                                        result.getEvlNo(), result.getEvlDegr(), nextSeq, matchResult.getSelectedMbrId());
                        } catch (Exception e) {
                                log.error("[MatchHist] 매칭 이력 저장 실패 (평가 요청 자체는 성공)", e);
                        }
                }

                // 4. 알림 처리 (기존과 동일)
                Member reviewrMember = memberMapper.getMember(result.getReviewerMemberId());
                String rgtrRoleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;
                String seatLocation = locationMapper.getLocatoinbyMbrId(member.getMbrId());

                String notificationBody = buildPartnerNotificationBody(member, result, seatLocation);
                savePartnerEvaluationRequestNotification(member.getMbrId(), result.getReviewerMemberId(),
                        member.getInstCd(), notificationBody, rgtrRoleCd);

                String subtitle = messageSource.getMessage("evalreq.msg.evldesnted.title", null,
                                LocaleContextHolder.getLocale());
                String discordNotificationType = messageSource.getMessage("usr.evreq.msg.regsched2", null, LocaleContextHolder.getLocale());

                NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(Constants.TMPLT_CD_6);
                NotificationMessage notificationMessage = commonNotificationMessageBuilder.buildContentNotificationMessage(
                        template,
                        result.getMtlEvlSn(),
                        member,
                        reviewrMember,
                        member.getInstCd(),
                        rgtrRoleCd,
                        subtitle,
                        buildPartnerEmailBody(member, result, seatLocation),
                        discordNotificationType);

                applicationEventPublisher.publishEvent(new NotificationEvent(
                                EventBoundary.EVALUATION, EventDetail.REQUEST, notificationMessage));

                // 동료 평가 요청 — 요청 팀원 전원 각자 소속 길드에 적립 (건당 1회)
                grantGuildToRequesterTeam(member, request, GuildActivityTrigger.PEER_EVAL_REQUEST,
                                String.valueOf(result.getMtlEvlSn()));

                return result;
        }

        /**
         * 재평가 요청 (통과 문제에 대한 재도전) — 평가 리셋만 수행
         * - 평가자 배정/알림 없음
         * - 사용자가 이후 일반 평가요청 플로우로 다시 평가 진행
         */
        public void resetForReEvaluation(Member member, MentorEvaluationAppendRequest request) {
                // 재평가 거부 판정(팀·텀·파이널·동료평가없음·운영자차단)은 EvaluationAppender 한 곳에서만 한다. (#237)
                // 여기서 팀 미션을 따로 막으면 같은 판정이 두 곳으로 갈려 메시지도 이원화된다.
                evaluationAppender.resetForReEvaluation(member, request);
        }

        /**
         * KAN-110 자동평가-정작분석
         * 
         * @param member
         * @param request
         * @return
         */
        public AppendedEvaluationResponse appendAutoEvaluationRequest(Member member,
                        MentorEvaluationAppendRequest request) {
                return evaluationAppender.appendAutoEvaluation(member, request);
        }

        /**
         * KAN-210 입학선발
         * 
         * @param request
         * @return
         */
        public AppendedEvaluationResponse appendExamEvaluationRequest(Member member,
                        MentorEvaluationAppendRequest request) {
                return evaluationAppender.appendExamEvaluation(member, request);
        }

        private NotificationEvaluationResponse toNotificationResponse(AppendedEvaluationResponse result) {
                return NotificationEvaluationResponse.from(
                                result.getMtlEvlSn(),
                                result.getMtlEvlStusCd(),
                                result.getEvlrTypeCd(),
                                result.getMtlEvlPamBgngDt(),
                                result.getProjectNm(),
                                result.getLcorsNm(),
                                result.getUqstnNm(),
                                result.getReviewerInstCd(),
                                result.getReviewerMemberId());
        }

        public AppendedEvaluationResponse appendMentorEvaluationRequest(Member member,
                        MentorEvaluationAppendRequest request) {
                AppendedEvaluationResponse result = evaluationAppender.appendMentorEvaluation(member, request);

                Member reviewrMember = memberMapper.getMember(result.getReviewerMemberId());
                String seatLocation = locationMapper.getLocatoinbyMbrId(member.getMbrId());

                String notificationBody = buildMentorNotificationBody(member, result, seatLocation);
                String roleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;

                saveMentorEvaluationRequestNotification(member.getMbrId(), result.getReviewerMemberId(),
                                member.getInstCd(), notificationBody, roleCd);

                String subtitle = messageSource.getMessage("usr.evreq.msg.evldesntedmt.title", null,
                                LocaleContextHolder.getLocale());

                // Discord 알림 유형 메시지 구성
                String discordNotificationType = messageSource.getMessage("usr.evreq.msg.regmtsched2", null, LocaleContextHolder.getLocale());

                // 템플릿 조회
                NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(Constants.TMPLT_CD_6);

                // 빌더 호출 (템플릿 전달)
                NotificationMessage mentorNotificationMessage = commonNotificationMessageBuilder.buildContentNotificationMessage(
                                template,
                                result.getMtlEvlSn(),
                                member,
                                reviewrMember,
                                result.getReviewerInstCd(),
                                MBR_AUTH_CD_M,
                                subtitle,
                                buildMentorEmailBody(member, result, seatLocation),
                                discordNotificationType);

                applicationEventPublisher.publishEvent(new NotificationEvent(
                                EventBoundary.EVALUATION, EventDetail.REQUEST, mentorNotificationMessage));

                // 멘토 평가 요청 — 신청 팀원 전원 각자 소속 길드에 적립 (건당 1회, 멘토 본인 제외)
                grantGuildToRequesterTeam(member, request, GuildActivityTrigger.MENTOR_EVAL_REQUEST,
                                String.valueOf(result.getMtlEvlSn()));

                return result;
        }

        protected final String HTML_BR = "<br/>";

        private String buildSeatLocationLine(String location) {
                if (StringUtil.isEmptyString(location)) {
                        return "";
                }
                String label = messageSource.getMessage("usr.evreg.msg.regschedSeatLocation", null,
                                LocaleContextHolder.getLocale());
                return label + " " + location + HTML_BR;
        }

        private String buildPartnerNotificationBody(Member requestMember,
                        AppendedEvaluationResponse appendedEvaluation, String seatLocation) {
                String msg1 = messageSource.getMessage("usr.evreq.msg.regsched1", null,
                                LocaleContextHolder.getLocale());
                String msg2 = messageSource.getMessage("usr.evreq.msg.regsched2", null,
                                LocaleContextHolder.getLocale());
                String msg3 = messageSource.getMessage("usr.evreq.msg.regsched3", null,
                                LocaleContextHolder.getLocale());
                String msg4 = messageSource.getMessage("usr.evreq.msg.regsched4", null,
                                LocaleContextHolder.getLocale());
                String msg5 = messageSource.getMessage("usr.evreq.msg.regsched5", null,
                                LocaleContextHolder.getLocale());
                String msg6 = messageSource.getMessage("usr.evreq.msg.regsched6", null,
                                LocaleContextHolder.getLocale());
                String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null,
                                LocaleContextHolder.getLocale());
                String msg8 = messageSource.getMessage("usr.evreg.msg.regschedRequester", null,
                                LocaleContextHolder.getLocale());
                String msg9 = messageSource.getMessage("usr.evreg.msg.regschedRequesterDiscordID", null,
                                LocaleContextHolder.getLocale());
                String body = msg1 + HTML_BR +
                                msg2 + HTML_BR +
                                msg3 + HTML_BR +
                                msg8 + " " + requestMember.getMbrNm() + "(" + requestMember.getEmlAddr() + ")" + HTML_BR
                                +
                                msg9 + " "
                                + (requestMember.getDiscordId() != null && !requestMember.getDiscordId().equals("")
                                                ? requestMember.getDiscordId()
                                                : "-")
                                + HTML_BR +
                                buildSeatLocationLine(seatLocation) +
                                msg7 + " " + appendedEvaluation.getMtlEvlPamBgngDt() + HTML_BR +
                                msg4 + " " + appendedEvaluation.getProjectNm() + HTML_BR +
                                msg5 + " " + appendedEvaluation.getLcorsNm() + HTML_BR +
                                msg6 + " " + appendedEvaluation.getUqstnNm();

                return body;
        }

        private String buildPartnerEmailBody(Member requestMember,
                        AppendedEvaluationResponse appendedEvaluation, String seatLocation) {
                String msg4 = messageSource.getMessage("usr.evreq.msg.regsched4", null,
                                LocaleContextHolder.getLocale());
                String msg5 = messageSource.getMessage("usr.evreq.msg.regsched5", null,
                                LocaleContextHolder.getLocale());
                String msg6 = messageSource.getMessage("usr.evreq.msg.regsched6", null,
                                LocaleContextHolder.getLocale());
                String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null,
                                LocaleContextHolder.getLocale());
                String msg8 = messageSource.getMessage("usr.evreg.msg.regschedRequester", null,
                                LocaleContextHolder.getLocale());

                String body = msg8 + " " + requestMember.getMbrNm() + "(" + requestMember.getEmlAddr() + ")" + HTML_BR +
                                buildSeatLocationLine(seatLocation) +
                                msg7 + " " + appendedEvaluation.getMtlEvlPamBgngDt() + HTML_BR +
                                msg4 + " " + appendedEvaluation.getProjectNm() + HTML_BR +
                                msg5 + " " + appendedEvaluation.getLcorsNm() + HTML_BR +
                                msg6 + " " + appendedEvaluation.getUqstnNm();

                return body;
        }

        private String buildMentorEmailBody(Member requestMember, AppendedEvaluationResponse appendedEvaluation, String seatLocation) {
                String msg4 = messageSource.getMessage("usr.evreq.msg.regmtsched4", null,
                                LocaleContextHolder.getLocale());
                String msg5 = messageSource.getMessage("usr.evreq.msg.regmtsched5", null,
                                LocaleContextHolder.getLocale());
                String msg6 = messageSource.getMessage("usr.evreq.msg.regmtsched6", null,
                                LocaleContextHolder.getLocale());
                String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null,
                                LocaleContextHolder.getLocale());

                String body = buildSeatLocationLine(seatLocation) +
                                msg7 + " " + appendedEvaluation.getMtlEvlPamBgngDt() + HTML_BR +
                                msg4 + " " + appendedEvaluation.getProjectNm() + HTML_BR +
                                msg5 + " " + appendedEvaluation.getLcorsNm() + HTML_BR +
                                msg6 + " " + appendedEvaluation.getUqstnNm();

                return body;
        }

        private String buildMentorNotificationBody(Member requestMember, AppendedEvaluationResponse appendedEvaluation, String seatLocation) {

                String msg1 = messageSource.getMessage("usr.evreq.msg.regmtsched1", null,
                                LocaleContextHolder.getLocale());
                String msg2 = messageSource.getMessage("usr.evreq.msg.regmtsched2", null,
                                LocaleContextHolder.getLocale());
                String msg3 = messageSource.getMessage("usr.evreq.msg.regmtsched3", null,
                                LocaleContextHolder.getLocale());
                String msg4 = messageSource.getMessage("usr.evreq.msg.regmtsched4", null,
                                LocaleContextHolder.getLocale());
                String msg5 = messageSource.getMessage("usr.evreq.msg.regmtsched5", null,
                                LocaleContextHolder.getLocale());
                String msg6 = messageSource.getMessage("usr.evreq.msg.regmtsched6", null,
                                LocaleContextHolder.getLocale());
                String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null,
                                LocaleContextHolder.getLocale());

                String body = msg1 + HTML_BR +
                                msg2 + HTML_BR +
                                msg3 + HTML_BR +
                                buildSeatLocationLine(seatLocation) +
                                msg7 + " " + appendedEvaluation.getMtlEvlPamBgngDt() + HTML_BR +
                                msg4 + " " + appendedEvaluation.getProjectNm() + HTML_BR +
                                msg5 + " " + appendedEvaluation.getLcorsNm() + HTML_BR +
                                msg6 + " " + appendedEvaluation.getUqstnNm();

                return body;
        }

        private void savePartnerEvaluationRequestNotification(String requestMemberId, String reviewerMemberId,
                        String reviewerInstCd, String body, String roleCd) {
                String title = messageSource.getMessage("evalreq.msg.evldesnted.title", null,
                                LocaleContextHolder.getLocale());
                roleCd = roleCd != null ? roleCd.replaceFirst("^ROLE_", "") : null;
                // 공통 DTO로 변환
                NtcBbsBasDTO ntcBbsBasDTO = new NtcBbsBasDTO();
                ntcBbsBasDTO.setInstCd(reviewerInstCd);
                ntcBbsBasDTO.setMbrId(reviewerMemberId);
                ntcBbsBasDTO.setRgtrId(requestMemberId);
                ntcBbsBasDTO.setReadYn("N");
                // 동료평가이므로 발신자와 수신자 역할 동일
                ntcBbsBasDTO.setRoleCd(roleCd);
                ntcBbsBasDTO.setRgtrRoleCd(roleCd);
                ntcBbsBasDTO.setMdfrId(requestMemberId);
                ntcBbsBasDTO.setPstartTitlNm(title);
                ntcBbsBasDTO.setPstartCn(body);
                ntcBbsBasDTO.setNtcDivCd(NTC_DIV_CD_101);
                ntcBbsBasDTO.setSysDivCd(DM_SYS_DIV_CD_17);

                ntcBbsBasMapper.save(ntcBbsBasDTO);
        }

        private void saveMentorEvaluationRequestNotification(String requestMemberId, String reviewerMemberId,
                        String reviewerInstCd, String body, String mbrRole) {
                String title = messageSource.getMessage("usr.evreq.msg.evldesntedmt.title", null,
                                LocaleContextHolder.getLocale());

                // 공통 DTO로 변환
                NtcBbsBasDTO ntcBbsBasDTO = new NtcBbsBasDTO();
                ntcBbsBasDTO.setInstCd(reviewerInstCd);
                ntcBbsBasDTO.setMbrId(reviewerMemberId);
                ntcBbsBasDTO.setRgtrId(requestMemberId);
                ntcBbsBasDTO.setReadYn("N");
                // 멘토평가이므로 수신자는 멘토, 발신자는 학생
                ntcBbsBasDTO.setRoleCd(MBR_AUTH_CD_M);
                ntcBbsBasDTO.setRgtrRoleCd(mbrRole);
                ntcBbsBasDTO.setMdfrId(requestMemberId);
                ntcBbsBasDTO.setPstartTitlNm(title);
                ntcBbsBasDTO.setPstartCn(body);
                ntcBbsBasDTO.setNtcDivCd(NTC_DIV_CD_101);
                ntcBbsBasDTO.setSysDivCd(DM_SYS_DIV_CD_21);

                ntcBbsBasMapper.save(ntcBbsBasDTO);
        }
}
