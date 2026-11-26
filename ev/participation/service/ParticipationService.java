package kr.innoaca.usr.web.ev.participation.service;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.dto.Constants;
import kr.innoaca.core.common.exception.ValidCustomException;
import kr.innoaca.core.common.notification.builder.CommonNotificationMessageBuilder;
import kr.innoaca.core.common.notification.domain.NotificationTemplate;
import kr.innoaca.core.common.notification.event.NotificationEvent;
import kr.innoaca.core.common.notification.event.NotificationMessage;
import kr.innoaca.core.common.notification.mapper.NotificationTemplateMapper;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.common.utils.ApiUtil;
import kr.innoaca.core.common.utils.AwsS3Util;
import kr.innoaca.core.common.utils.EmailUtil;
import kr.innoaca.core.common.utils.StringUtil;
import kr.innoaca.core.domain.bbs.NtcBbsBasMapper;
import kr.innoaca.core.domain.inst.InstBasMapper;
import kr.innoaca.core.web.alarm.domain.constant.EventBoundary;
import kr.innoaca.core.web.alarm.domain.constant.EventDetail;
import kr.innoaca.core.web.alarm.dto.NtcBbsBasDTO;
import kr.innoaca.core.web.alarm.service.NotificationSender;
import kr.innoaca.core.web.member.mapper.MemberMapper;
import kr.innoaca.core.web.credit.constant.CreditTrigger;
import kr.innoaca.core.web.credit.dto.CreditGrantEvent;
import kr.innoaca.core.web.credit.service.CreditCompletionGate;
import kr.innoaca.core.web.credit.service.CreditProjectLcorsGate;
import kr.innoaca.core.web.credit.service.CreditGrantService;
import kr.innoaca.core.web.credit.service.CreditLevelResolver;
import kr.innoaca.core.web.point.dto.MbrCreditHstDto;
import kr.innoaca.core.web.point.dto.MbrExprcHstDto;
import kr.innoaca.core.web.point.dto.MbrPntHstDto;
import kr.innoaca.core.web.point.dto.PointDto;
import kr.innoaca.core.web.point.mapper.PointMapper;
import kr.innoaca.core.web.point.service.PointService;
import kr.innoaca.core.domain.guild.GuildActivityTrigger;
import kr.innoaca.core.web.point.service.GuildScoreGranter;
import kr.innoaca.usr.common.utils.Criteria;
import kr.innoaca.usr.common.utils.Paginator;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDetailDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationListDto;
import kr.innoaca.usr.web.ev.participation.dto.ProjectSuccessQualGrntDTO;
import kr.innoaca.usr.evaluation.EvaluationBas;
import kr.innoaca.core.common.utils.PeerPassMethod;
import kr.innoaca.usr.web.ev.participation.mapper.ParticipationMapper;
import kr.innoaca.usr.web.ev.request.dto.*;
import kr.innoaca.usr.web.ev.request.mapper.EvalutionReqMapper;
import kr.innoaca.usr.web.ev.result.dto.EvalutionResultAutoEvlListDto;
import kr.innoaca.usr.web.learning.dto.LearningDto;
import kr.innoaca.usr.web.learning.dto.UqstnTeam;
import kr.innoaca.usr.web.learning.mapper.LearningMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.innoaca.usr.common.exception.UsrBadRequestException;
// 시험 허용 IP 주소 조회
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kr.innoaca.core.common.dto.Constants.*;
import kr.innoaca.core.domain.evaluation.TeamRosterMember;

/**
 * @package kr.inno.usr.community.free.service
 * @name ParticipationService
 * @date 2023-11-10
 * @author zeroj0706
 * @version 1.0.0
 *          description :
 *          ===========================================================
 *          DATE AUTHOR NOTE
 *          -----------------------------------------------------------
 *          2023-11-10 zeroj0706 최초 생성
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationService {
    @Value("${domain-info.ams-url}")
    String domainInfoAmsUrl;
    private final ParticipationMapper participationMapper;
    private final kr.innoaca.core.mapper.evaluation.PassJudgmentMapper passJudgmentMapper; // #159 통과방식 조회(판정과 같은 매퍼)
    private final kr.innoaca.core.mapper.evaluation.ReEvaluationMapper reEvaluationMapper; // #193 재도전 이력(usr·adm 공용)
    private final kr.innoaca.core.mapper.evaluation.TeamRosterMapper teamRosterMapper; // #350 팀 로스터(usr·adm 공용)
    private final EvalutionReqMapper evalutionReqMapper;
    private final ApiUtil apiUtil;
    private final PointService pointService;
    private final CreditGrantService creditGrantService;   // [#34] 정책 기반 크레딧 파이프라인
    private final GuildScoreGranter guildScoreGranter;     // [#166] 정책 기반 길드 점수 적립
    private final CreditLevelResolver creditLevelResolver; // [#34] 난이도/단계레벨 산정
    private final CreditCompletionGate creditCompletionGate; // [#34] 단계 완주(CRS_CMPL) 게이트
    private final CreditProjectLcorsGate creditProjectLcorsGate; // [#321] 텀·파이널 보완 발행 게이트
    private final AwsS3Util awsS3Util;
    private final MessageSource messageSource;
    private final EvalutionReqMapper evalutionMapper;
    private final EmailUtil emailUtil;
    private final NtcBbsBasMapper ntcBbsBasMapper;
    private final PointMapper pointMapper;
    private final CommonNotificationMessageBuilder commonNotificationMessageBuilder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberMapper memberMapper;
    private final LearningMapper learningMapper;
    private final NotificationTemplateMapper notificationTemplateMapper;
    private final NotificationSender notificationSender;
    private final kr.innoaca.usr.evaluation.EvaluationAppendMapper evaluationAppendMapper;
    private final kr.innoaca.usr.web.ev.participation.mapper.EvlRuleCnfrmMapper evlRuleCnfrmMapper; // #351 규칙 확인 이력

    private static final String HTML_BR = "<br/>";

    /**
     * 동료 평가 규칙 확인 이력 기록 (#351)
     * 평가 진행 시작 직전 게이트를 통과한 당사자(세션)가 규칙을 확인한 시점을 해당 평가건에 남긴다.
     * 확인 주체·기관은 요청 본문이 아니라 세션에서 도출한다 — 남의 이름으로 확인 기록을 남길 수 없다.
     */
    @Transactional
    public ResponseMessage saveRuleConfirm(kr.innoaca.core.common.auth.Member member,
                                           kr.innoaca.usr.web.ev.participation.dto.EvlRuleCnfrmDto dto) {
        if (dto.getMtlEvlSn() == null || dto.getMtlEvlSn().isEmpty()
                || dto.getRuleVer() == null || dto.getRuleVer().isEmpty()) {
            throw new UsrBadRequestException(
                    messageSource.getMessage("usr.participation.ruleCnfrm.required", null,
                            LocaleContextHolder.getLocale()));
        }
        dto.setMbrId(member.getMbrId());
        dto.setInstCd(member.getInstCd());
        // 매퍼가 본인(평가자) 평가건 + 미확인 상태일 때만 1행을 넣는다(소유권·멱등 가드).
        // 0행이어도 정상 응답 — 이미 확인됐거나(재확인) 본인 건이 아니면 기록 없이 통과한다.
        evlRuleCnfrmMapper.insertEvlRuleCnfrm(dto);
        return ResponseMessage.ok(null);
    }

    /**
     * 평가요청 조회
     *
     * @param participationDto
     * @return
     */
    public ResponseMessage participationList(ParticipationDto participationDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        // Paging Start
        Criteria criteria = new Criteria();
        criteria.setPageNum(participationDto.getPage());
        criteria.setPagePerRows(participationDto.getPagePerRows());
        participationDto.setCriteria(criteria);

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDto.setLanguage(language);
        List<ParticipationListDto> list = participationMapper.participationList(participationDto);
        int allCount = participationMapper.participationListAllCnt(participationDto);

        int total = 0;
        try {
            if (!list.isEmpty()) {
                total = list.get(0).getTotal();
            }
        } catch (RuntimeException e) {
            total = list.size();
        }

        Paginator paginator = new Paginator(criteria, total);
        // Paging End

        resultMap.put("list", list);
        resultMap.put("paginator", paginator);
        resultMap.put("allCount", allCount);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가 참여 전체 조회(페이징x)
     *
     * @param participationDto
     * @return
     */
    public ResponseMessage participationListAll(ParticipationDto participationDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDto.setLanguage(language);
        List<ParticipationListDto> list = participationMapper.participationListAll(participationDto);

        resultMap.put("list", list);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가요청 수락/거절처리
     *
     * @param participationDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage evalutionAcceptRefuseUpdate(ParticipationDto participationDto,
            @AuthenticationPrincipal Member member) {
        ResponseMessage responseMessage = new ResponseMessage();

        // 평가 수락 거절 전 상호평가 내역 상태 조회
        String preEvlStatus = participationMapper.getPreEvlStatus(participationDto);
        if (Constants.MTL_EVL_STUS_CD_5.equals(preEvlStatus) || Constants.MTL_EVL_STUS_CD_4.equals(preEvlStatus)) {
            // 평가 수락 거절 전 상호평가 내역 상태값 업데이트
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.msg.alreadyCanceled", null, LocaleContextHolder.getLocale()));
        }

        if (MTL_EVL_STUS_CD_2.equals(preEvlStatus)) {
            throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.alreadyAccepted", null, LocaleContextHolder.getLocale()));
        }

        // 상호평가 상태값 업데이트
        participationMapper.evalutionAcceptRefuseUpdate(participationDto);

        // 수락 시 평가기본 상태값 업데이트
        if (Constants.MTL_EVL_STUS_CD_2.equals(participationDto.getMtlEvlStusCd())) {
            EvalutionBasDto evalutionBasDto = new EvalutionBasDto();
            evalutionBasDto.setEvlNo(participationDto.getEvlNo().intValue());
            evalutionBasDto.setEvlDegr(participationDto.getEvlDegr().longValue());
            evalutionBasDto.setMbrId(participationDto.getMbrId());
            evalutionBasDto.setEvlStusCd(Constants.EVL_STUS_CD_2);

            String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
            evalutionBasDto.setLanguage(language);

            // 평가기본내역 조회
            EvalutionBasDto evlBasInfo = evalutionReqMapper.selectEvlBasByPk(evalutionBasDto);

            if (evlBasInfo.getEvlBgngDt() == null) {
                // 평가상태코드 Update(00002 : 진행중)
                participationMapper.evlBasStusCdUpdate(evalutionBasDto);
            }

        }

        // 수락인 경우는알림 발송 안함
        if (Constants.MTL_EVL_STUS_CD_4.equals(participationDto.getMtlEvlStusCd())) { // 메일 발송 (거절)
            RegReqEvalDto regReqEvalDto = new RegReqEvalDto();

            participationMapper.rejectEvaluationRequestAndResetSchedule(participationDto);

            String msg1 = messageSource.getMessage("usr.rej.cancel1", null, LocaleContextHolder.getLocale());
            String msg2 = messageSource.getMessage("usr.rej.cancel2", null, LocaleContextHolder.getLocale());
            String msg3 = messageSource.getMessage("usr.rej.cancel3", null, LocaleContextHolder.getLocale());
            String msg4 = messageSource.getMessage("usr.per.permission4", null, LocaleContextHolder.getLocale());
            String msg5 = messageSource.getMessage("usr.per.permission5", null, LocaleContextHolder.getLocale());
            String msg6 = messageSource.getMessage("usr.per.permission6", null, LocaleContextHolder.getLocale());
            String msg7 = messageSource.getMessage("usr.per.permission7", null, LocaleContextHolder.getLocale());

            // 4. 알림게시판기본에 INSERT 처리
            String pstartCn = msg1 + HTML_BR +
                    msg2 + HTML_BR +
                    HTML_BR +
                    msg3 + " " + participationDto.getCancelCn() + HTML_BR +
                    msg4 + " " + participationDto.getProjectNm() + HTML_BR +
                    msg5 + " " + participationDto.getLrnCorsNm() + HTML_BR +
                    msg6 + " " + participationDto.getUqstnNm() + HTML_BR +
                    msg7 + " " + participationDto.getMtlEvlPamBgngDt();

            String pstartTitlNm = messageSource.getMessage("usr.rej.cancel.title", null,
                    LocaleContextHolder.getLocale());
            Long nowTime = System.currentTimeMillis();

            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String mdfcnDt = timeFormat.format(new Date(nowTime));

            regReqEvalDto.setEvlInstCd(member.getInstCd());
            regReqEvalDto.setEvlMbrId(participationDto.getCancelMbrId());
            regReqEvalDto.setMbrId(participationDto.getMbrId());
            regReqEvalDto.setMdfcnDt(mdfcnDt);
            regReqEvalDto.setPstartTitlNm(pstartTitlNm);
            regReqEvalDto.setPstartCn(pstartCn);
            regReqEvalDto.setSysDivCd(DM_SYS_DIV_CD_19);
            regReqEvalDto.setNtcDivCd(NTC_DIV_CD_101);

            // 평가자 정보 구하기
            MbrBasDto mbrBasDto = new MbrBasDto();
            mbrBasDto.setInstCd(regReqEvalDto.getEvlInstCd());
            mbrBasDto.setMbrId(regReqEvalDto.getEvlMbrId());

            List<MbrBasDto> resultList = evalutionMapper.selectMbrBasList(mbrBasDto);

            List<PointDto> lstPointInfo = pointService.getPointInfo("/ev/participation/evaluationAcceptRefuseCancleUpdate");

            // 팀 미션(PJT_LCORS_UQSTN.TEAM_COMPOSITION != '00000') 은 신청 시 차감이 없으므로
            // 거절 시에도 포인트 반환 없음. participationDto 에 projectNo/lcorsNo/uqstnNo 가
            // 비어 있을 수 있어 evlNo → EvaluationBas 경로로 조회한다.
            if (!isTeamMissionByEvlNo(participationDto.getEvlNo())) {
                for(PointDto pointInfo : lstPointInfo) {
                    MbrPntHstDto mbrPntHstDto = new MbrPntHstDto();
                    mbrPntHstDto.setRgtrId(member.getMbrId());
                    mbrPntHstDto.setInstCd(member.getInstCd());
                    mbrPntHstDto.setMbrId(regReqEvalDto.getEvlMbrId());
                    mbrPntHstDto.setRoleCd(member.getRoleCd().replace("ROLE_", ""));
                    mbrPntHstDto.setPntScr(pointInfo.getCrtrScr());
                    mbrPntHstDto.setPntDtlDesc(pointInfo.getCrtrDtlDesc());
                    mbrPntHstDto.setEngPntDtlDesc(pointInfo.getEngCrtrDtlDesc());
                    mbrPntHstDto.setPntTypeNo(pointInfo.getCrtrTypeNo());
                    mbrPntHstDto.setSysDivCd(pointInfo.getSysDivCd());
                    mbrPntHstDto.setWrkjbDivCd(pointInfo.getWrkjbDivCd());

                    // 포인트 이력 등록 처리.
                    pointService.insertMbrPntHst(mbrPntHstDto);

                    // 포인트 업데이트
                    pointService.updateMbrInstRelPnt(mbrPntHstDto);
                }
            }

            // 빌더에서 Email과 Discord를 모두 생성하여 NotificationMessage 반환 (거절)
            Member receiveMember = memberMapper.getMember(regReqEvalDto.getEvlMbrId());
            String receiveMemberRoleCd = resultList.get(0).getRoleCd().replace("ROLE_", "");
            String subTitle = messageSource.getMessage("usr.evreq.msg.evlclcl.title", null,
                    LocaleContextHolder.getLocale());

            Long mtlEvlSn = participationDto.getMtlEvlSn() != null && !participationDto.getMtlEvlSn().isEmpty()
                    ? Long.parseLong(participationDto.getMtlEvlSn())
                    : null;

            String roleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;

            // 웹 알림 등록
            NtcBbsBasDTO ntcBbsBasDto = new NtcBbsBasDTO();
            // 기관코드
            ntcBbsBasDto.setInstCd(member.getInstCd());
            // 수신이
            ntcBbsBasDto.setMbrId(receiveMember.getMbrId());
            // 발송자
            ntcBbsBasDto.setRgtrId(member.getMbrId());
            // 발신자 권한
            ntcBbsBasDto.setRgtrRoleCd(roleCd);
            // 수신자 권한
            ntcBbsBasDto.setRoleCd(roleCd);
            // 알림 제목
            ntcBbsBasDto.setPstartTitlNm(pstartTitlNm);
            // 알림 내용
            ntcBbsBasDto.setPstartCn(pstartCn);
            // 알림 구분 코드
            ntcBbsBasDto.setNtcDivCd(NTC_DIV_CD_101);
            // 알림 상세 코드
            ntcBbsBasDto.setSysDivCd(DM_SYS_DIV_CD_19);
            ntcBbsBasDto.setReadYn("N");
            ntcBbsBasMapper.save(ntcBbsBasDto);

            // Discord 알림 유형 메시지 구성
            String discordNotificationType = msg2;
            String notificationBody = msg3 + " " + participationDto.getCancelCn() + HTML_BR +
                    msg4 + " " + participationDto.getProjectNm() + HTML_BR +
                    msg5 + " " + participationDto.getLrnCorsNm() + HTML_BR +
                    msg6 + " " + participationDto.getUqstnNm() + HTML_BR +
                    msg7 + " " + participationDto.getMtlEvlPamBgngDt();

            // 템플릿 조회 후 빌더 호출
            NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);
            NotificationMessage notificationMessage = commonNotificationMessageBuilder.buildContentNotificationMessage(
                    template, mtlEvlSn, member, receiveMember,
                    regReqEvalDto.getEvlInstCd(), receiveMemberRoleCd, pstartTitlNm, notificationBody,
                    discordNotificationType);

            try {
                applicationEventPublisher.publishEvent(
                        new NotificationEvent(EventBoundary.EVALUATION, EventDetail.REJECT, notificationMessage));
            } catch (RuntimeException e) {
                log.error("Error publishing notification event");
            }
        }

        responseMessage.setResult(1);
        return responseMessage;
    }

    /**
     * 평가요청자 정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage lrnMbrInfo(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.lrnMbrInfo(participationDetailDto));
        return responseMessage;
    }

    /**
     * 프로젝트 정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage projectInfo(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.projectInfoDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 학습과정정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage studyInfoDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.studyInfoDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 단위문제정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage uqstnInfoDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        participationDetailDto.setKwajungStr(
                messageSource.getMessage("usr.evreq.title.kwajungStr", null, LocaleContextHolder.getLocale()));
        responseMessage.setResult(participationMapper.uqstnInfoDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 단위문제, 과정평가 갯수 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage queInfoDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.queInfoDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 평가상세이력 내용 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage evlInfo(ParticipationDetailDto participationDetailDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDetailDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.evlInfo(participationDetailDto));
        return responseMessage;
    }

    /**
     * 평가문항 정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage uqstnQitemCnDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.uqstnQitemCnDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 평가결과 정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage adtnlScrDetail(ParticipationDetailDto participationDetailDto) {
        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDetailDto.setLanguage(language);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.adtnlScrDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 역량점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage cpblyDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.cpblyDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 자동평가 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage autoEvlDetail(ParticipationDetailDto participationDetailDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDetailDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.autoEvlDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 동시성 처리: 현재 상호평가 상태가 진행중(00003)이 아니면 예외를 던져
     * 클라이언트가 이전 페이지로 돌아가도록 한다.
     *
     * @param participationDetailDto mtlEvlSn 가 채워져 있어야 함
     */
    private void validateMtlEvlStusCdInProgress(ParticipationDetailDto participationDetailDto) {
        String mtlEvlStusCd = participationMapper.selectMtlEvlStusCd(participationDetailDto);
        if (mtlEvlStusCd == null || !MTL_EVL_STUS_CD_3.equals(mtlEvlStusCd)) {
            throw new ValidCustomException(messageSource.getMessage("usr.participation.msg.invalidEvlStatus", null,
                    LocaleContextHolder.getLocale()));
        }
    }

    /**
     * 프로젝트, 학습과정, 단위문제, 평가자료, 평가문항, 평가결과 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage projectInfoDetail(ParticipationDetailDto participationDetailDto) {
        // 동시성 처리: 평가 상태가 진행중(00003)이 아니면 차단
        validateMtlEvlStusCdInProgress(participationDetailDto);

        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        // 평가요청자 정보 조회
        ParticipationDetailDto lrnMbrInfo = (ParticipationDetailDto) this.lrnMbrInfo(participationDetailDto)
                .getResult();
        // 프로젝트 정보 조회
        ParticipationDetailDto projectInfo = (ParticipationDetailDto) this.projectInfo(participationDetailDto)
                .getResult();
        // 학습과정 정보 조회
        ParticipationDetailDto lcorsInfo = (ParticipationDetailDto) this.studyInfoDetail(participationDetailDto)
                .getResult();
        // 단위문제, 과정평가 평가가이드 조회
        ParticipationDetailDto queInfo = (ParticipationDetailDto) this.queInfoDetail(participationDetailDto)
                .getResult();

        // participationMapper.getMinEvlTime()의 결과를 배열 에 저장한다.
        // ------------삭제 예정 ------------
        List<String> minEvlTimeList = participationMapper.getMinEvlTime();
        if (minEvlTimeList.size() > 0) {
            queInfo.setPeerEvlImplHr(minEvlTimeList.get(0));
        }
        // ------------삭제 예정 ------------

        // 평가상세이력 내용 조회
        List<ParticipationDetailDto> evlInfo = (List<ParticipationDetailDto>) this.evlInfo(participationDetailDto)
                .getResult();
        // 단위문제, 평가자료 정보 조회
        ParticipationDetailDto uqstnInfo = (ParticipationDetailDto) this.uqstnInfoDetail(participationDetailDto)
                .getResult();
        // 평가문항 정보 조회
        List<ParticipationDetailDto> uqstnQitemCnList = (List<ParticipationDetailDto>) this
                .uqstnQitemCnDetail(participationDetailDto).getResult();
        // 평가결과 정보 조회
        ParticipationDetailDto adtnlScrInfo = (ParticipationDetailDto) this.adtnlScrDetail(participationDetailDto)
                .getResult();
        // 역량점수 조회
        List<ParticipationDetailDto> cpblyList = (List<ParticipationDetailDto>) this.cpblyDetail(participationDetailDto)
                .getResult();

        resultMap.put("lrnMbrInfo", lrnMbrInfo);
        resultMap.put("projectInfo", projectInfo);
        resultMap.put("lcorsInfo", lcorsInfo);
        resultMap.put("uqstnInfo", uqstnInfo);
        resultMap.put("queInfo", queInfo);
        resultMap.put("evlInfo", evlInfo);
        resultMap.put("adtnlScrInfo", adtnlScrInfo);
        resultMap.put("uqstnQitemCnList", uqstnQitemCnList);
        resultMap.put("cpblyList", cpblyList);

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 상호평가상태코드 수정
     *
     * @param participationDto
     * @return
     */
    public ResponseMessage mtlEvlStusCdUpdate(ParticipationDto participationDto) {
        ResponseMessage responseMessage = new ResponseMessage();

        // 동시성 처리: 현재 상태가 대기중(00002)일 때만 진행중(00003)으로 변경 가능
        ParticipationDetailDto checkDto = new ParticipationDetailDto();
        checkDto.setMtlEvlSn(participationDto.getMtlEvlSn());
        String currentStusCd = participationMapper.selectMtlEvlStusCd(checkDto);
        if (currentStusCd == null || !MTL_EVL_STUS_CD_2.equals(currentStusCd)) {
            throw new ValidCustomException(messageSource.getMessage("usr.participation.msg.invalidEvlStatus", null,
                    LocaleContextHolder.getLocale()));
        }

        int rtn = participationMapper.mtlEvlStusCdUpdate(participationDto);

        if (rtn != 1) {
            throw new ValidCustomException(messageSource.getMessage("usr.participation.msg.mtlevlupdterror", null,
                    LocaleContextHolder.getLocale()));
        }
        responseMessage.setResult(rtn);
        return responseMessage;
    }

    /**
     * 피드백 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage fdbList(ParticipationDetailDto participationDetailDto) {
        // 동시성 처리: 평가 상태가 진행중(00003)이 아니면 차단
        validateMtlEvlStusCdInProgress(participationDetailDto);

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDetailDto.setLanguage(language);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(participationMapper.evlFdbDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 피드백 등록
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage fdbSave(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        int rtn = participationMapper.fdbSave(participationDetailDto);

        if (rtn != 1) {
            throw new ValidCustomException(messageSource.getMessage("usr.participation.msg.fdbadderror", null,
                    LocaleContextHolder.getLocale()));
        }

        responseMessage.setResult(rtn);
        return responseMessage;
    }

    /**
     * 평가참여 임시등록 (평가 피드백 포함)
     *
     * @param participationDetailDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage evlSave(ParticipationDetailDto participationDetailDto) {
        // 동시성 처리: 평가 상태가 진행중(00003)이 아니면 차단
        validateMtlEvlStusCdInProgress(participationDetailDto);

        ResponseMessage responseMessage = new ResponseMessage();

        // 평가 피드백 저장 (피드백 내용이 있는 경우)
        String evlFdbkCn = participationDetailDto.getEvlFdbkCn();

        
        
        if (evlFdbkCn != null && !evlFdbkCn.trim().isEmpty()) {
            if (evlFdbkCn.trim().length() < 100) {
                log.error("평가 피드백 내용은 최소 100자 이상이어야 합니다. (현재 {}자)", evlFdbkCn.trim().length());
                throw new UsrBadRequestException(messageSource.getMessage("evl.feedback.content.min.length", null, LocaleContextHolder.getLocale()));
            }
            if (evlFdbkCn.length() > 2000) {
                log.error("평가 피드백 내용은 2000자를 초과할 수 없습니다. {}", evlFdbkCn);
                throw new UsrBadRequestException(messageSource.getMessage("evl.feedback.content.max.length", null, LocaleContextHolder.getLocale()));
            }
            participationDetailDto.setMbrId(participationDetailDto.getEvlMbrId());
            participationDetailDto.setInstCd(participationDetailDto.getEvlInstCd());
            participationMapper.fdbSave(participationDetailDto);
        }

        if (participationDetailDto.getMtlEvlAdtnlScr() != null) {
            if (!participationDetailDto.getMtlEvlAdtnlScr().equals("0")
                    && !participationDetailDto.getMtlEvlAdtnlScr().isEmpty()) {
                participationDetailDto.setMtlEvlAdtnlScrYn("Y");
            } else {
                participationDetailDto.setMtlEvlAdtnlScrYn("N");
            }
        }

        // 평가문항 저장
        if (!participationDetailDto.getQitemNoList().isEmpty()) {
            participationMapper.evlSave(participationDetailDto);
        }

        // 역량평가 저장
        if (!participationDetailDto.getCpblyList().isEmpty()) {
            participationMapper.cpblySave(participationDetailDto);
        }

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        participationDetailDto.setLanguage(language);

        // 경험정도 저장
        participationMapper.exprcDgrCdUpdate(participationDetailDto);

        responseMessage.setResult(1);
        return responseMessage;
    }

    /**
     * 평가확정처리 이전에 데이터 체크
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage saveChckDetail(ParticipationDetailDto participationDetailDto) {
        // 동시성 처리: 평가 상태가 진행중(00003)이 아니면 차단
        validateMtlEvlStusCdInProgress(participationDetailDto);

        ResponseMessage responseMessage = new ResponseMessage();
        List<String> minEvlTimeList = participationMapper.getMinEvlTime();

        // participationMapper.getMinEvlTime()의 결과를 배열 에 저장한다.
        // ------------삭제 예정 ------------
        if (minEvlTimeList.size() > 0) {
            participationDetailDto.setPeerEvlImplHr(minEvlTimeList.get(0));
        }
        // ------------삭제 예정 ------------

        responseMessage.setResult(participationMapper.saveChckDetail(participationDetailDto));
        return responseMessage;
    }

    /**
     * 평가참여 확정처리
     *
     * @param participationDetailDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage evlEndSave(ParticipationDetailDto participationDetailDto, Member member) {
        // 동시성 처리: 평가 상태가 진행중(00003)이 아니면 차단
        validateMtlEvlStusCdInProgress(participationDetailDto);

        ResponseMessage responseMessage = new ResponseMessage();

        if (participationDetailDto.getMtlEvlAdtnlScr() != null) {
            if (!participationDetailDto.getMtlEvlAdtnlScr().equals("0")
                    && !participationDetailDto.getMtlEvlAdtnlScr().isEmpty()) {
                participationDetailDto.setMtlEvlAdtnlScrYn("Y");
            } else {
                participationDetailDto.setMtlEvlAdtnlScrYn("N");
            }
        }

        // 상호평가 종료처리
        participationMapper.evlEnd(participationDetailDto);
        // #80 평가완료 시 연결 오픈 슬롯(예약 00002)을 종료(00003)로 전이 — 완료 후 예약 방치 해소(PASS/FAIL 공통)
        participationMapper.completeEvlPsblScdl(participationDetailDto);
        // 단위문제 마지막 평가자 확인
        String lastUqstn = participationMapper.getLastUqstn(participationDetailDto);
        // 학습과정 마지막 평가자 확인
        String lastLcors = null;
        // 프로젝트 학습과정 마지막 확인
        String lastPjtLcors = null;
        // 이번 단위문제가 필수인지 — 크레딧 게이트와 학습단계 완료 알림이 같이 쓴다
        String uqstnRequireYn = null;

        // [#159] 누적 통과 보정 — 누적은 개별 평가 결과가 아니라 누적 PASS 건수로 판정한다.
        //        FAIL 이 나와도 기준을 채웠으면 이 차수를 통과로 마감하고(개별 FAIL 기록은 MTL_EVL_TXN 에 그대로 남는다),
        //        아직 못 채웠으면 남은 평가자의 PASS 를 더 받을 수 있게 차수를 닫지 않는다.
        //        연속(CONSECUTIVE)은 기존 동작 그대로 — 한 명이라도 FAIL 이면 그 차수는 실패다.
        boolean keepRoundOpen = false; // 차수를 마감하지 않고 남은 평가자를 기다리는 경우
        if (Constants.EVL_RESLT_CD_FAIL.equals(participationDetailDto.getMtlEvlResltCd())
                && isCumulativePass(participationDetailDto)) {
            if (Constants.USE_YN_Y.equals(lastUqstn)) {
                participationDetailDto.setMtlEvlResltCd(Constants.EVL_RESLT_CD_PASS);
                log.info("[#159] 누적 통과 기준 충족 — FAIL 이지만 차수를 통과로 마감. evlNo={}, evlDegr={}",
                        participationDetailDto.getEvlNo(), participationDetailDto.getEvlDegr());
            } else if (participationMapper.selectRolloverTargetCount(participationDetailDto) > 0) {
                keepRoundOpen = true;
                log.info("[#159] 누적 통과 기준 미달 — 남은 평가자가 있어 차수를 유지한다. evlNo={}, evlDegr={}",
                        participationDetailDto.getEvlNo(), participationDetailDto.getEvlDegr());
            }
            // 기준 미달 + 남은 평가자 없음 → 아래 FAIL 처리로 차수를 마감한다(재요청 가능해야 하므로)
        }

        // FAIL 시
        if (Constants.EVL_RESLT_CD_FAIL.equals(participationDetailDto.getMtlEvlResltCd())) {
            // #159 누적에서 차수를 유지하는 경우는 마감·취소를 건너뛴다.
            //      (평가자 보상·알림은 아래 공통 흐름에서 그대로 처리돼야 하므로 여기서 빠져나가지 않는다)
            if (!keepRoundOpen) {
                // [#70] 미진행 평가자(평가요청중/평가대기중)가 있으면 신규 차수를 생성해 이동하고,
                //       없으면 기존대로 평가요청취소 처리한다
                boolean rolledOver = rolloverPeerEvaluationIfPossible(participationDetailDto);
                if (!rolledOver) {
                    participationMapper.evlEndRtrcn(participationDetailDto); // 실패로 추후 다른 평가자 평가요청 취소
                    participationMapper.evlEndScdl(participationDetailDto); // 실패로 추후 다른 평가자 평가 스케출 취소
                    participationMapper.evlMtCancel(participationDetailDto); // 실패로 추후 전문가 요청 취소
                }
                participationMapper.evlEndPOF(participationDetailDto); // EVL_BAS 테이블 평가결과 저장(기존 차수 FAIL 종료)
            }
            // PASS 시
        } else if (Constants.EVL_RESLT_CD_PASS.equals(participationDetailDto.getMtlEvlResltCd())
                && Constants.USE_YN_Y.equals(lastUqstn)) {
            participationMapper.evlEndPOF(participationDetailDto); // EVL_BAS 테이블 평가결과 저장
            participationMapper.lastUqstnExec1(participationDetailDto); // 단위문제 평가내역 저장
            participationMapper.lastUqstnExec2(participationDetailDto); // 역량문제 평가내역 저장

            // 학습과정에 대한 평가가 아닐 경우, 학습과정단위문제팀별강제종료 업데이트
//            if (!Constants.LCORS_UQSTN_NO.equals(participationDetailDto.getUqstnNo())) {
//                participationMapper.lastLcorsExec2p5(participationDetailDto); // 학습과정단위문제팀별강제종료 업데이트
//            }

            lastLcors = participationMapper.getLastLcors(participationDetailDto); // 학습과정의 마지막 확인 구분자
//            if (Constants.USE_YN_Y.equals(lastLcors)) {
//                participationMapper.lastLcorsExec1(participationDetailDto); // 학습과정평가내역 저장
//                participationMapper.lastLcorsExec2(participationDetailDto); // 학습과정역량평가내역 저장
//            }

            // lastPjtLcors = participationMapper.getLastPjtLcors(participationDetailDto);
            // // 프로젝트 학습과정 마지막 확인

            // 프로젝트 최종 PASS 확인
            lastPjtLcors = participationMapper.getAllPjtSuccess(participationDetailDto); // 프로젝트 최종 PASS 확인

//            if (Constants.USE_YN_Y.equals(lastPjtLcors)) {
//                participationMapper.lastLcorsExec3(participationDetailDto); // 팀상태코드 업데이트

                // 프로젝트 마지막 학습과정이 완료된 상태이면, 학사관리에 단계완료 정보를 업데이트 한다.
//                participationMapper.setARSubsidyClassisMember(participationDetailDto);

                // 완료한 프로젝트의 부여자격(qual_grnt) 목록과 팀원별 보유 여부 조회
//                List<ProjectSuccessQualGrntDTO> qualGrntList = participationMaplearningper
//                        .getTeamMbrOwnsQualYn(participationDetailDto);

                // 프로젝트 부여자격을 팀원에게 추가 (기존에 보유하지 않은 경우만)
//                for (ProjectSuccessQualGrntDTO qualGrnt : qualGrntList) {
//                    if (!Constants.USE_YN_Y.equals(qualGrnt.getQualGrntYn())) {
//                        // 해당 자격을 보유하지 않은 경우에만 자격 부여
//                        participationMapper.insertMbrOwnQual(qualGrnt);
//                    }
//                }
//            }
        }

        List<TeamRosterMember> teamInfo = teamRosterMapper.selectTeamRoster(
                participationDetailDto.getProjectNo(), participationDetailDto.getLcorsNo(),
                participationDetailDto.getUqstnNo(), participationDetailDto.getTeamSn());

        // 포인트 정보 조회
        ExprPointDto pointParam = new ExprPointDto();
        pointParam.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
        pointParam.setCrtrTypeNo("13");
        PointDto pointInfo = participationMapper.getPoint(pointParam);

        // 평가자 경험치 정보 조회
        ExprPointDto expParam = new ExprPointDto();
        expParam.setCrtrTypeCd(Constants.CRTR_TYPE_CD_E);
        expParam.setCrtrTypeNo("25");
        PointDto expInfo = participationMapper.getPoint(expParam);

        // 학습자 경험치 정보 조회
        expParam.setCrtrTypeCd(Constants.CRTR_TYPE_CD_E);
        expParam.setCrtrTypeNo("24");
        PointDto expLrnrInfo = participationMapper.getPoint(expParam);

        // [#193] 재평가 여부 체크 (경험치/크래딧 적립 스킵용)
        //   기존엔 EVL_NO 의 MAX 차수 1행이 재도전 차수인지만 봤다. 그래서
        //   "재도전 → FAIL → 일반 평가요청으로 신규 차수 → 통과" 로 돌면 마지막 차수가 REEVL_YN='N' 이라
        //   게이트가 풀려 경험치·크레딧이 전액 재지급됐다(반복 가능).
        //   판정을 평가번호 단위로 바꿔, 한 번이라도 재도전을 쓴 EVL_NO 는 이후 차수도 재평가로 본다.
        boolean isReEvaluation = false;
        if (participationDetailDto.getEvlNo() != null) {
            isReEvaluation = reEvaluationMapper.existsReEvaluationDegree(
                    participationDetailDto.getEvlNo().longValue());
            if (isReEvaluation) {
                log.info("[재평가] 경험치/크래딧 적립 스킵(평가참여 포인트는 지급). evlNo={}",
                        participationDetailDto.getEvlNo());
            }
        }

        // 평가완료 보상(평가참여 포인트 type-13 / 평가자 경험치 type-25)의 수령자는
        // '그 평가의 평가자'(mtl_evl_txn.evl_mbr_id)이며 로그인/요청 사용자(mbrId)가 아니다.
        // (기존엔 PointFilter 가 로그인 사용자에게, 경험치는 DTO mbrId 에 적립 → 학습자가 완료를 호출하면
        //  평가자 보상이 학습자에게 오지급. 포인트·경험치 모두 평가자로 정렬한다.)
        // 지급행 role_cd 는 FK(inst_cd,mbr_id,role_cd)→mbr_role_rel 를 만족해야 하므로
        // 평가자의 실재 역할을 평가유형 인지로 조회한다(동료=S, 전문가=M 우선). getMember 는 role_cd 미조회라 사용 불가.
        // [#193] 수령자 결정은 재평가 여부와 무관하게 한다 — 재평가여도 평가참여 포인트는 지급하기 때문이다.
        //        실제 지급 여부는 항목별 게이트에서 가른다(포인트 지급 / 경험치·크레딧 차단).
        String rewardMbrId = null, rewardInstCd = null, rewardRoleCd = null;
        if (pointInfo != null || expInfo != null) {
            Map<String, Object> evlParam = new HashMap<>();
            evlParam.put("projectNo", participationDetailDto.getProjectNo());
            evlParam.put("lcorsNo", participationDetailDto.getLcorsNo());
            evlParam.put("uqstnNo", participationDetailDto.getUqstnNo());
            evlParam.put("teamSn", participationDetailDto.getTeamSn());
            evlParam.put("mtlEvlSn", participationDetailDto.getMtlEvlSn());
            kr.innoaca.usr.evaluation.dto.query.AppendedEvaluationResponse evl = evaluationAppendMapper
                    .getEvlBasAndMtlEvlTxn(evlParam);
            if (evl != null && !StringUtils.isEmpty(evl.getReviewerMemberId())) {
                rewardMbrId = evl.getReviewerMemberId();
                rewardInstCd = evl.getReviewerInstCd();
                Map<String, Object> roleParam = new HashMap<>();
                roleParam.put("evlInstCd", rewardInstCd);
                roleParam.put("evlMbrId", rewardMbrId);
                roleParam.put("preferRole",
                        Constants.EVL_TYPE_CD_2.equals(evl.getEvlrTypeCd()) ? "M" : "S");
                rewardRoleCd = evaluationAppendMapper.selectReviewerRoleCd(roleParam);
                if (StringUtils.isEmpty(rewardRoleCd)) {
                    // 역할을 확정 못하면 지급하지 않는다(완료가 FK/NOT NULL 로 롤백되는 것 방지).
                    log.warn("[평가완료] 평가자 역할 조회 실패로 완료 보상 미지급. mtlEvlSn={}, evlMbrId={}",
                            participationDetailDto.getMtlEvlSn(), rewardMbrId);
                    rewardMbrId = null;
                }
            } else {
                log.warn("[평가완료] 평가자 조회 실패로 완료 보상 미지급. mtlEvlSn={}",
                        participationDetailDto.getMtlEvlSn());
            }
        }

        // 평가참여 포인트(type-13) → 평가자. (경험치 활성 여부와 독립: pointInfo 기준)
        // 팀미션이어도 평가자는 완료 1건당 1회만 지급한다(팀원수 무관).
        // [#193] 재평가 차수여도 지급한다 — 평가자는 실제로 평가를 수행했고, 정책상 포인트는 제한 대상이 아니다
        //        (제한 대상은 경험치·크레딧). 이 게이트에만 isReEvaluation 을 걸지 않는다.
        if (pointInfo != null && rewardMbrId != null) {
            MbrPntHstDto mbrPntHstDto = new MbrPntHstDto();
            mbrPntHstDto.setRgtrId(rewardMbrId);
            mbrPntHstDto.setInstCd(rewardInstCd);
            mbrPntHstDto.setMbrId(rewardMbrId);
            mbrPntHstDto.setRoleCd(rewardRoleCd);
            mbrPntHstDto.setPntScr(pointInfo.getCrtrScr());
            mbrPntHstDto.setPntDtlDesc(pointInfo.getCrtrDtlDesc());
            mbrPntHstDto.setEngPntDtlDesc(pointInfo.getEngCrtrDtlDesc());
            mbrPntHstDto.setPntTypeNo(pointParam.getCrtrTypeNo());
            mbrPntHstDto.setSysDivCd(pointInfo.getSysDivCd());
            mbrPntHstDto.setWrkjbDivCd(pointInfo.getWrkjbDivCd());

            pointService.insertMbrPntHst(mbrPntHstDto);
            pointService.updateMbrInstRelPnt(mbrPntHstDto);
        }

        // 평가자 경험치(type-25) → 평가자. (수령자·역할을 포인트와 동일하게 평가자로 정렬)
        // 팀미션이어도 평가자는 완료 1건당 1회만 지급한다(팀원수 무관).
        if (expInfo != null && !isReEvaluation && rewardMbrId != null) {
            MbrExprcHstDto mbrExprParam = new MbrExprcHstDto();
            mbrExprParam.setRgtrId(rewardMbrId);
            mbrExprParam.setInstCd(rewardInstCd);
            mbrExprParam.setMbrId(rewardMbrId);
            mbrExprParam.setRoleCd(rewardRoleCd);
            mbrExprParam.setExprcScr(expInfo.getCrtrScr());
            mbrExprParam.setExprcDtlDesc(expInfo.getCrtrDtlDesc());
            mbrExprParam.setEngExprcDtlDesc(expInfo.getEngCrtrDtlDesc());
            mbrExprParam.setExprcTypeNo(expInfo.getCrtrTypeNo());
            mbrExprParam.setSysDivCd(expInfo.getSysDivCd());
            mbrExprParam.setWrkjbDivCd(expInfo.getWrkjbDivCd());

            pointService.insertMbrExprcHst(mbrExprParam);
            pointService.updateMbrInstRelExp(mbrExprParam);
        }

        // [#34] 동료평가 작성 크레딧(PEER_FB). 평가자 본인 대상, 상호평가 1건당 1회(멱등키=mtlEvlSn).
        // 작성 완료 기준이라 PASS/FAIL 무관. 재평가도 새 mtlEvlSn이라 발화(아웃박스 UQ가 중복만 무시), 전문가평가(멘토)는 제외.
        // (ADM 평가관리 종료 경로와 지급 동작 일치 — 리뷰 #4)
        if (creditGrantService.isPolicyEnabled()
                && !Constants.MBR_AUTH_CD_M.equals(participationDetailDto.getRoleCd())
                && !Constants.ROLE_M.equals(participationDetailDto.getRoleCd())
                && !StringUtils.isEmpty(participationDetailDto.getMtlEvlSn())) {
            creditGrantService.publish(CreditGrantEvent.builder()
                    .trigger(CreditTrigger.PEER_FB)
                    .mbrId(participationDetailDto.getMbrId())
                    .instCd(participationDetailDto.getInstCd())
                    .roleCd(participationDetailDto.getRoleCd())
                    .refTbl("MTL_EVL")
                    .refSn(participationDetailDto.getMtlEvlSn())
                    .build());
        }

        // 동료 평가 참여 — 평가 완료 학습자 본인 소속 길드에 적립 (상호평가 1건당 1회, 멘토 제외)
        if (!Constants.MBR_AUTH_CD_M.equals(participationDetailDto.getRoleCd())
                && !Constants.ROLE_M.equals(participationDetailDto.getRoleCd())
                && !StringUtils.isEmpty(participationDetailDto.getMtlEvlSn())) {
            guildScoreGranter.grant(participationDetailDto.getInstCd(), GuildActivityTrigger.PEER_EVAL_PARTICIPATE,
                    participationDetailDto.getMbrId(), participationDetailDto.getRoleCd(),
                    participationDetailDto.getMtlEvlSn(), member.getMbrId());
        }

        // 평가 문제 PASS — 마지막 평가자의 PASS로 결과가 확정된 시점, 팀원 전원 각자 소속 길드에 적립 (평가 건당 1회)
        // (운영자 종료 경로는 adm EvaluationMgmtService.evlEnd 에서 동일하게 적립)
        if (Constants.EVL_RESLT_CD_PASS.equals(participationDetailDto.getMtlEvlResltCd())
                && Constants.USE_YN_Y.equals(lastUqstn)
                && !StringUtils.isEmpty(participationDetailDto.getMtlEvlSn())) {
            for (TeamRosterMember passTeamMember : teamInfo) {
                guildScoreGranter.grant(passTeamMember.getInstCd(), GuildActivityTrigger.EVAL_PROBLEM_PASS,
                        passTeamMember.getMbrId(), passTeamMember.getRoleCd(),
                        participationDetailDto.getMtlEvlSn(), member.getMbrId());
            }
        }

        if (expLrnrInfo != null && !isReEvaluation) {
            for (TeamRosterMember teamMember : teamInfo) {
                        // [평가종료(학습자)] 경험치 적립 차감 : START
                       MbrExprcHstDto lrnrExprParam = new MbrExprcHstDto();
                       lrnrExprParam.setRgtrId(teamMember.getMbrId());
                       lrnrExprParam.setInstCd(teamMember.getInstCd());
                       lrnrExprParam.setMbrId(teamMember.getMbrId());
                       lrnrExprParam.setRoleCd(teamMember.getRoleCd());
                       lrnrExprParam.setExprcScr(expLrnrInfo.getCrtrScr());
                       lrnrExprParam.setExprcDtlDesc(expLrnrInfo.getCrtrDtlDesc());
                       lrnrExprParam.setEngExprcDtlDesc(expLrnrInfo.getEngCrtrDtlDesc());
                       lrnrExprParam.setExprcTypeNo(expLrnrInfo.getCrtrTypeNo());
                       lrnrExprParam.setSysDivCd(expLrnrInfo.getSysDivCd());
                       lrnrExprParam.setWrkjbDivCd(expLrnrInfo.getWrkjbDivCd());

                       pointService.insertMbrExprcHst(lrnrExprParam);
                       // [평가종료(학습자)] 경험치 적립 차감 : END
                    }
        }

        if (Constants.EVL_RESLT_CD_FAIL.equals(participationDetailDto.getMtlEvlResltCd())) {
            // 취소요청 할 평가자 리스트 조회
            List<MtlEvlTxnDto> evlCancelList = participationMapper.getMtCancelList(participationDetailDto);

            // KAN185
            // 깃주소 조회
            // String gitAddr = participationMapper.getGitAddr(participationDetailDto);

            MenteeReqDto menteeReqDto = new MenteeReqDto();

            // KAN185
            // GitLab API 주소
            // String apiUrlGit = Constants.GITLAB_PROXY_PROJECT_MEMBER_DELETE;
            // 팀원수만큼 GitLab 프로젝트 멤버 삭제 API 호출
            for (MtlEvlTxnDto evlCancel : evlCancelList) {
                JSONObject data = new JSONObject();

                // KAN185
                /*
                if (StringUtil.isNotEmpty(gitAddr)) {
                    data.put("userName", evlCancel.getEvlMbrId());
                    data.put("gitAddress", gitAddr);

                    JSONObject sendJsonObj = null;
                    // Git Lab 프로젝트 멤버 삭제 API 호출
                    sendJsonObj = apiUtil.callRestApi(data, apiUrlGit);

                    if (!sendJsonObj.containsKey("retCode") || !Constants.API_RESPONSE_CD_SUCCESS.equals(sendJsonObj.get("retCode"))) {
                        throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.gitlabpjtdelerror", null, LocaleContextHolder.getLocale()));
                    }
               }
               */

                // 전문가평가자라면 멘토 일정 취소
                if (Constants.EVL_TYPE_CD_2.equals(evlCancel.getEvlrTypeCd())) {
                    menteeReqDto.setMteMbrInstCd(evlCancel.getEvlInstCd());
                    menteeReqDto.setMteMbrId(evlCancel.getEvlMbrId());
                    menteeReqDto.setDmndSn(evlCancel.getMteDmndSn().intValue());
                    evalutionReqMapper.deleteMenteeSchedule(menteeReqDto);
                }
            }
        }

        // PASS 시. [#34 리뷰 #4] 정책모드 크레딧은 재평가에도 발화(아웃박스 UQ가 멱등 보장 → 기존 지급분은 무시, 재평가로 새로 충족된 것만 지급).
        // 단 경험치·레거시 직접적립(비멱등)은 재평가 시 스킵해야 이중지급을 막으므로 각 지점에서 개별 가드.
        if (Constants.EVL_RESLT_CD_PASS.equals(participationDetailDto.getMtlEvlResltCd())
                && Constants.USE_YN_Y.equals(lastUqstn)) {
            // 평가 PASS 경험치 조회
            String exp = participationMapper.getPassExpScore(participationDetailDto);

            // 학습자 경험치 정보 조회
            expParam.setCrtrTypeCd(Constants.CRTR_TYPE_CD_E);
            expParam.setCrtrTypeNo("52");
            PointDto expPassInfo = participationMapper.getPoint(expParam);

            // 평가 PASS 경험치 적립 (경험치는 비멱등 → 재평가 시 스킵)
            if (expPassInfo != null && !isReEvaluation) {
                for (TeamRosterMember teamMember : teamInfo) {
                   MbrExprcHstDto evlPassParam = new MbrExprcHstDto();
                   evlPassParam.setRgtrId(teamMember.getMbrId());
                   evlPassParam.setInstCd(teamMember.getInstCd());
                   evlPassParam.setMbrId(teamMember.getMbrId());
                   evlPassParam.setRoleCd(teamMember.getRoleCd());
                   evlPassParam.setExprcScr(exp);
                   evlPassParam.setExprcDtlDesc(expPassInfo.getCrtrDtlDesc());
                   evlPassParam.setEngExprcDtlDesc(expPassInfo.getEngCrtrDtlDesc());
                   evlPassParam.setExprcTypeNo(expPassInfo.getCrtrTypeNo());
                   evlPassParam.setSysDivCd(expPassInfo.getSysDivCd());
                   evlPassParam.setWrkjbDivCd(expPassInfo.getWrkjbDivCd());

                   pointService.insertMbrExprcHst(evlPassParam);
                   pointService.updateMbrInstRelExp(evlPassParam);
               }
            }

            // ========== 크레딧 적립 시작 ==========

            // 1. 단위문제 통과 시 크레딧 적립 (lastUqstn = 'Y' 조건 이미 충족)
            ExprPointDto uqstnCreditdto = new ExprPointDto();
            uqstnCreditdto.setCrtrTypeCd(CRTR_TYPE_CD_C);
            uqstnCreditdto.setCrtrTypeNo(C_CRDT_TYPE_NO_1);
            PointDto uqstnCredit = participationMapper.getPoint(uqstnCreditdto);
            String uqstnCreditScr = participationMapper.getUqstnCreditScore(participationDetailDto);
            // [#34] 미션 통과 크레딧. 정책모드: 난이도(uqstn_dffly) SUB_KEY로 아웃박스 발행. 레거시: 고정값 즉시 적립.
            String msnRefSn = participationDetailDto.getProjectNo() + "-" + participationDetailDto.getLcorsNo() + "-" + participationDetailDto.getUqstnNo();
            if (creditGrantService.isPolicyEnabled() && !isReEvaluation) { // #193 재도전 이력이면 정책모드 미션 크레딧도 미발행
                String msnSubKey = creditLevelResolver.difficultySubKey(
                        participationDetailDto.getProjectNo(), participationDetailDto.getLcorsNo(), participationDetailDto.getUqstnNo());
                for (TeamRosterMember teamMember : teamInfo) {
                    creditGrantService.publish(CreditGrantEvent.builder()
                            .trigger(CreditTrigger.MSN_CMPL)
                            .mbrId(teamMember.getMbrId()).instCd(teamMember.getInstCd()).roleCd(teamMember.getRoleCd())
                            .subKey(msnSubKey)
                            .refTbl("PARTICIPATION_MSN").refSn(msnRefSn)
                            .build());
                }
            } else if (!isReEvaluation && uqstnCredit != null && StringUtil.isNotEmpty(uqstnCreditScr) && !"0".equals(uqstnCreditScr)) {
                for (TeamRosterMember teamMember : teamInfo) {
                    MbrCreditHstDto creditDto = new MbrCreditHstDto();
                    creditDto.setRgtrId(teamMember.getMbrId());
                    creditDto.setInstCd(teamMember.getInstCd());
                    creditDto.setMbrId(teamMember.getMbrId());
                    creditDto.setRoleCd(teamMember.getRoleCd());
                    creditDto.setCreditScr(uqstnCreditScr);
                    creditDto.setCreditTypeNo(uqstnCredit.getCrtrTypeNo());
                    creditDto.setWrkjbDivCd(uqstnCredit.getWrkjbDivCd());
                    creditDto.setSysDivCd(uqstnCredit.getSysDivCd());
                    creditDto.setCreditDtlDesc(uqstnCredit.getCrtrDtlDesc());
                    creditDto.setEngCreditDtlDesc(uqstnCredit.getEngCrtrDtlDesc());

                    pointService.insertMbrCreditHst(creditDto);
                    pointService.updateMbrInstRelCredit(creditDto);
                }
            }

            // 4. 보너스 크레딧 적립 (모든 평가자가 추가 점수를 제공한 경우)
            String allAdtnlScrY = participationMapper.checkAllEvlrAdtnlScrPvsnYn(participationDetailDto);
            if (Constants.USE_YN_Y.equals(allAdtnlScrY)) {
                // [#34] 보너스 문제 크레딧. 정책모드: 단계레벨 SUB_KEY로 발행. 레거시: 고정값 즉시 적립.
                if (creditGrantService.isPolicyEnabled() && !isReEvaluation) { // #193 재도전 이력이면 정책모드 보너스 크레딧도 미발행
                    String bnsSubKey = creditLevelResolver.stageLevelSubKey(participationDetailDto.getProjectNo());
                    for (TeamRosterMember teamMember : teamInfo) {
                        creditGrantService.publish(CreditGrantEvent.builder()
                                .trigger(CreditTrigger.BNS_QSTN)
                                .mbrId(teamMember.getMbrId()).instCd(teamMember.getInstCd()).roleCd(teamMember.getRoleCd())
                                .subKey(bnsSubKey)
                                .refTbl("PARTICIPATION_BNS").refSn(msnRefSn)
                                .build());
                    }
                } else if (!isReEvaluation) {
                    ExprPointDto bonusCreditDto = new ExprPointDto();
                    bonusCreditDto.setCrtrTypeCd(CRTR_TYPE_CD_C);
                    bonusCreditDto.setCrtrTypeNo(C_CRDT_TYPE_NO_4);
                    PointDto bonusCredit = participationMapper.getPoint(bonusCreditDto);
                    String bonusCreditScr = participationMapper.getBonusCreditScore(participationDetailDto);
                    if (bonusCredit != null && StringUtil.isNotEmpty(bonusCreditScr) && !"0".equals(bonusCreditScr)) {
                        for (TeamRosterMember teamMember : teamInfo) {
                            MbrCreditHstDto creditDto = new MbrCreditHstDto();
                            creditDto.setRgtrId(teamMember.getMbrId());
                            creditDto.setInstCd(teamMember.getInstCd());
                            creditDto.setMbrId(teamMember.getMbrId());
                            creditDto.setRoleCd(teamMember.getRoleCd());
                            creditDto.setCreditScr(bonusCreditScr);
                            creditDto.setCreditTypeNo(bonusCredit.getCrtrTypeNo());
                            creditDto.setWrkjbDivCd(bonusCredit.getWrkjbDivCd());
                            creditDto.setSysDivCd(bonusCredit.getSysDivCd());
                            creditDto.setCreditDtlDesc(bonusCredit.getCrtrDtlDesc());
                            creditDto.setEngCreditDtlDesc(bonusCredit.getEngCrtrDtlDesc());

                            pointService.insertMbrCreditHst(creditDto);
                            pointService.updateMbrInstRelCredit(creditDto);
                        }
                    }
                }
            }

            // 현재 단위문제의 필수 여부 조회
            uqstnRequireYn = participationMapper.getUqstnRequireYn(participationDetailDto);

            // 2. 학습과정 통과 시 크레딧 적립 (필수 단위문제인 경우만)
            ExprPointDto lcorsCreditdto = new ExprPointDto();
            lcorsCreditdto.setCrtrTypeCd(CRTR_TYPE_CD_C);
            lcorsCreditdto.setCrtrTypeNo(C_CRDT_TYPE_NO_2);
            PointDto lcorsCredit = participationMapper.getPoint(lcorsCreditdto);
            // [#34] 학습과정(lcors)은 크레딧 부여 대상 아님(plan §1) → 정책모드에서는 미적립. 레거시에서만 유지.
            if (!creditGrantService.isPolicyEnabled() && !isReEvaluation
                    && Constants.USE_YN_Y.equals(lastLcors) && Constants.USE_YN_Y.equals(uqstnRequireYn)) {
                String lcorsCreditScr = participationMapper.getLcorsCreditScore(participationDetailDto);
                if (lcorsCredit != null && StringUtil.isNotEmpty(lcorsCreditScr) && !"0".equals(lcorsCreditScr)) {
                    for (TeamRosterMember teamMember : teamInfo) {
                        MbrCreditHstDto creditDto = new MbrCreditHstDto();
                        creditDto.setRgtrId(teamMember.getMbrId());
                        creditDto.setInstCd(teamMember.getInstCd());
                        creditDto.setMbrId(teamMember.getMbrId());
                        creditDto.setRoleCd(teamMember.getRoleCd());
                        creditDto.setCreditScr(uqstnCreditScr);
                        creditDto.setCreditTypeNo(lcorsCredit.getCrtrTypeNo());
                        creditDto.setWrkjbDivCd(lcorsCredit.getWrkjbDivCd());
                        creditDto.setSysDivCd(lcorsCredit.getSysDivCd());
                        creditDto.setCreditDtlDesc(lcorsCredit.getCrtrDtlDesc());
                        creditDto.setEngCreditDtlDesc(lcorsCredit.getEngCrtrDtlDesc());

                        pointService.insertMbrCreditHst(creditDto);
                        pointService.updateMbrInstRelCredit(creditDto);
                    }
                }
            }

            // 3. 프로젝트 통과 시 크레딧 적립 (필수 단위문제인 경우만)
            ExprPointDto projectCreditDto = new ExprPointDto();
            projectCreditDto.setCrtrTypeCd(CRTR_TYPE_CD_C);
            projectCreditDto.setCrtrTypeNo(C_CRDT_TYPE_NO_3);
            PointDto projectCredit = participationMapper.getPoint(projectCreditDto);
            // [#297] 택N(주제 택1) 구성의 텀 프로젝트를 require_yn 게이트에서 빼낸다.
            // 네이티브 텀은 주제 택1(pjt_lcors.choose_rand=1) 구성이라 어떤 주제도 단독 필수일 수
            // 없어 require_yn 이 전부 'N' 인 것이 정상값이다. 이수 판정 SSOT(CreditMetaMapper.xml
            // stageSignalAgg)가 CHOOSE_RAND>=1 을 필수와 동급으로 보던 시절, 이 게이트만
            // require_yn='Y' 를 요구해 택1 텀프는 통과해도 발행 코드에 닿지 못했다.
            // 게이트는 택N 이거나 필수면 열린다 — choose_rand=0 인 텀·파이널은 종전대로
            // require_yn='Y' 로만 통과한다(운영의 파이널은 전부 choose_rand=0).
            // ⚠ 지금은 SSOT 와 기준이 같지 않다. stageSignalAgg 는 필수 미션을 전부 요구하고
            //   선택은 CHOOSE_RAND 개까지만 세므로, 택N 과 필수가 섞인 학습주제에서는
            //   이 게이트가 더 느슨하게 열린다. 둘은 묻는 것이 다르다 — 게이트는 "이 미션 통과가
            //   발행 시점인가"(미션 단위 트리거), SSOT 는 "단계를 다 마쳤는가"(단계 집계).
            //   맞출지 여부는 크레딧 지급 시점이 바뀌는 문제라 별도 판단이 필요하다.
            // 레거시(정책 off)는 prjGate 가 null 이라 게이트 조건이 종전과 같다.
            CreditLevelResolver.ProjectGate prjGate = (creditGrantService.isPolicyEnabled() && Constants.USE_YN_Y.equals(lastPjtLcors))
                    ? creditLevelResolver.projectGate(participationDetailDto.getProjectNo(), participationDetailDto.getLcorsNo()) : null;
            CreditTrigger prjTrigger = prjGate == null ? null : prjGate.getTrigger();
            boolean prjCreditGate = Constants.USE_YN_Y.equals(uqstnRequireYn)
                    || (prjGate != null && prjGate.opensWithoutRequireYn());
            // [#321] 텀·파이널 보완 발행 — prjCreditGate(이 미션이 발행 신호인가) 와 무관하게 돈다.
            // 게이트 안에 두면 정작 고치려던 순서를 잡지 못한다. 텀을 먼저 통과한 뒤 일반 택N 학습주제의
            // 선택 미션으로 단계를 마치는 경우, 그 미션은 uqstnRequireYn='N' 이고 일반이라 게이트가 닫혀
            // 이 자리에 닿지 못하는데 그것이 마지막 미션이라 다시 발화할 계기도 없다.
            // 조회(selectPassedProjectLcors)가 일반 학습주제 완주 여부를 스스로 보므로 게이트가 필요 없다.
            if (creditGrantService.isPolicyEnabled() && Constants.USE_YN_Y.equals(lastPjtLcors)) {
                for (TeamRosterMember teamMember : teamInfo) {
                    creditProjectLcorsGate.publishPassedProjectLcors(
                            teamMember.getMbrId(), teamMember.getInstCd(),
                            participationDetailDto.getProjectNo(), teamMember.getRoleCd());
                }
            }
            if (Constants.USE_YN_Y.equals(lastPjtLcors) && prjCreditGate) {
                // [#34] 학습단계(프로젝트) 통과 크레딧. 정책모드: lcors_type_cd로 텀/파이널 분기 발행(일반 00000은 미적립).
                if (creditGrantService.isPolicyEnabled()) {
                    // #193 재도전 이력이면 텀/파이널 크레딧도 미발행.
                    // 아래 CRS_CMPL 은 이수 판정을 다시 하므로 게이트 대상이 아니다 — 여기서 함께 막으면
                    // 다른 미션으로 단계를 완주한 학습자의 정당한 완주 크레딧까지 사라진다.
                    if (prjTrigger != null && !isReEvaluation) {
                        String prjSubKey = prjTrigger == CreditTrigger.TERM_PRJ
                                ? creditLevelResolver.stageLevelSubKey(participationDetailDto.getProjectNo()) : null;
                        String prjRefSn = participationDetailDto.getProjectNo() + "-" + participationDetailDto.getLcorsNo();
                        for (TeamRosterMember teamMember : teamInfo) {
                            creditGrantService.publish(CreditGrantEvent.builder()
                                    .trigger(prjTrigger)
                                    .mbrId(teamMember.getMbrId()).instCd(teamMember.getInstCd()).roleCd(teamMember.getRoleCd())
                                    .subKey(prjSubKey)
                                    .refTbl("PARTICIPATION_PRJ").refSn(prjRefSn)
                                    .build());
                        }
                    }
                    // [#34] 학습단계 종료 신호 → 이수 판정 후 CRS_CMPL(단계 완주) 파생 발행(단계×회원 멱등).
                    // 게이트를 통과한 신호는 prjTrigger 유무와 무관하게 재판정한다
                    // (일반 학습과정 통과로 단계가 완성될 수 있다).
                    // 단 일반(00000) 택N 학습과정의 선택 미션 통과는 바깥 게이트에 걸려 여기까지 오지
                    // 않는다 — SSOT 와 같은 형태의 불일치가 일반 학습과정에는 남아 있다(#297 스코프 밖).
                    for (TeamRosterMember teamMember : teamInfo) {
                        creditCompletionGate.publishIfStageCompleted(
                                teamMember.getMbrId(), teamMember.getInstCd(),
                                participationDetailDto.getProjectNo(), teamMember.getRoleCd());
                    }
                } else if (!isReEvaluation) {
                    String projectCreditScr = participationMapper.getProjectCreditScore(participationDetailDto);
                    if (projectCredit != null && StringUtil.isNotEmpty(projectCreditScr) && !"0".equals(projectCreditScr)) {
                        for (TeamRosterMember teamMember : teamInfo) {
                            MbrCreditHstDto creditDto = new MbrCreditHstDto();
                            creditDto.setRgtrId(teamMember.getMbrId());
                            creditDto.setInstCd(teamMember.getInstCd());
                            creditDto.setMbrId(teamMember.getMbrId());
                            creditDto.setRoleCd(teamMember.getRoleCd());
                            creditDto.setCreditScr(uqstnCreditScr);
                            creditDto.setCreditTypeNo(projectCredit.getCrtrTypeNo());
                            creditDto.setWrkjbDivCd(projectCredit.getWrkjbDivCd());
                            creditDto.setSysDivCd(projectCredit.getSysDivCd());
                            creditDto.setCreditDtlDesc(projectCredit.getCrtrDtlDesc());
                            creditDto.setEngCreditDtlDesc(projectCredit.getEngCrtrDtlDesc());

                            pointService.insertMbrCreditHst(creditDto);
                            pointService.updateMbrInstRelCredit(creditDto);
                        }
                    }
                }
            }
        }

        
        String roleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;

        // 학습미션 통과 알림 (웹 알림 + 이메일 + Discord)
        if (lastUqstn.equals(USE_YN_Y) && Constants.EVL_RESLT_CD_PASS.equals(participationDetailDto.getMtlEvlResltCd())) { // 미션 통과한 경우
            try {
                String subtitle = messageSource.getMessage("mission.pass.subtitle", null, LocaleContextHolder.getLocale());
                String body = messageSource.getMessage("mission.pass.body", null, LocaleContextHolder.getLocale());
                String notificationType = messageSource.getMessage("mission.pass.discord", null, LocaleContextHolder.getLocale());
                NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);

                for (TeamRosterMember teamMember : teamInfo) {
                    try {
                        Member receiveMember = memberMapper.getMember(teamMember.getMbrId());
                        if (receiveMember != null) {
                            notificationSender.sendLearningNotification(member, receiveMember, Long.valueOf(teamMember.getProjectNo()),
                                    member.getInstCd(),
                                    subtitle, body, notificationType, roleCd, roleCd,
                                    NTC_DIV_CD_101, DM_SYS_DIV_CD_29, EventBoundary.LEARNING, EventDetail.COMPLETE, template);
                        }
                    } catch (RuntimeException e) {
                        log.error("학습미션 통과 알림 발송 중 오류 발생 - mbrId: {}", teamMember.getMbrId());
                    }
                }
            } catch (RuntimeException e) {
                log.error("학습미션 통과 알림 초기화 중 오류 발생");
            }
        }

        // 학습단계 완료 알림 (웹 알림 + 이메일 + Discord)
        // 단계 통과는 팀 단위 판정이라 팀원별로 다시 묻지 않는다. 이전엔 루프 안에서 팀원 DTO 로
        // getAllPjtSuccess 를 다시 불렀는데 그 DTO 엔 lcorsNo·uqstnNo·teamSn 이 없어 항상 'N' 이 나왔고,
        // 그래서 이 알림 두 종류가 한 번도 발송되지 않았다.
        //
        // 통과 판정은 "지금 필수 미션을 다 채웠는가" 만 보고 "이미 알렸는가" 는 모른다. 그래서 단계를
        // 끝낸 뒤 비필수·보너스 미션을 통과할 때마다 또 나간다. 필수 미션 통과일 때만, 재도전이 아닐 때만
        // 보내 반복을 막는다.
        //
        // 크레딧 게이트(prjCreditGate)보다 좁다 — 거기엔 택N(choose_rand>=1) 텀·파이널을 여는
        // opensWithoutRequireYn() 우회가 더 있다. 그 우회까지 맞추면 택N 텀에서 반복 발송이 크게 는다
        // (크레딧은 아웃박스 UNIQUE 로 1회지만 알림엔 멱등키가 없다). 택N 텀의 이 알림이 계속 안 나가는
        // 것은 알고 남긴 잔여이며, 크레딧 지급 사실 자체는 적립 시 나가는 크레딧 획득 알림으로 통지된다.
        if (lastPjtLcors != null && lastPjtLcors.equals(USE_YN_Y) && !isReEvaluation
                && Constants.USE_YN_Y.equals(uqstnRequireYn)
                && Constants.EVL_RESLT_CD_PASS.equals(participationDetailDto.getMtlEvlResltCd())) {
            try {
                String stageSubtitle = messageSource.getMessage("stage.complete.subtitle", null, LocaleContextHolder.getLocale());
                String stageBody = messageSource.getMessage("stage.complete.body", null, LocaleContextHolder.getLocale());
                String stageNotificationType = messageSource.getMessage("stage.complete.discord", null, LocaleContextHolder.getLocale());
                NotificationTemplate stageTemplate = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);

                String openSubtitle = messageSource.getMessage("stage.open.subtitle", null, LocaleContextHolder.getLocale());
                String openBody = messageSource.getMessage("stage.open.body", null, LocaleContextHolder.getLocale());
                String openNotificationType = messageSource.getMessage("stage.open.discord", null, LocaleContextHolder.getLocale());

                for (TeamRosterMember teamMember : teamInfo) {
                    Member receiveMember = memberMapper.getMember(teamMember.getMbrId());
                    // 학습단계 완료 알림
                    try {
                        if (receiveMember != null) {
                            notificationSender.sendLearningNotification(member, receiveMember, Long.valueOf(teamMember.getProjectNo()),
                                    member.getInstCd(),
                                    stageSubtitle, stageBody, stageNotificationType, roleCd, roleCd,
                                    NTC_DIV_CD_101, DM_SYS_DIV_CD_30, EventBoundary.LEARNING, EventDetail.COMPLETE, stageTemplate);
                        }
                    } catch (RuntimeException e) {
                        log.error("학습단계 완료 알림 발송 중 오류 발생 - mbrId: {}", teamMember.getMbrId());
                    }

                    // 다음 프로젝트 조회
                    LearningDto learningDto = new LearningDto();
                    learningDto.setProjectNo(Integer.valueOf(teamMember.getProjectNo()));
                    learningDto.setMbrId(teamMember.getMbrId());
                    learningDto.setRcrtAplyYn("N");

                    LearningDto nextProject = learningMapper.selectPrevNextProject(learningDto);
                    if (nextProject != null && nextProject.getNextPjtNo() != null) {
                        // 새로운 단계 오픈 알림
                        try {
                            if (receiveMember != null) {
                                notificationSender.sendLearningNotification(member, receiveMember, nextProject.getNextPjtNo(),
                                        member.getInstCd(),
                                        openSubtitle, openBody, openNotificationType, roleCd, roleCd,
                                        NTC_DIV_CD_101, DM_SYS_DIV_CD_37, EventBoundary.LEARNING, EventDetail.OPEN, stageTemplate);
                            }
                        } catch (RuntimeException e) {
                            log.error("새로운 단계 오픈 알림 발송 중 오류 발생 - mbrId: {}", teamMember.getMbrId());
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.error("학습단계 완료 알림 초기화 중 오류 발생");
            }
        }

        // 알림 수신자 조회 (리더만)
        ParticipationDetailDto leaderMbr = participationMapper.getLrnMbrs(participationDetailDto);
        // 보낼 문구
        String teamStr1 = messageSource.getMessage("usr.participation.msg.cancelsched4", null,
                LocaleContextHolder.getLocale());

        Date nowDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strNowDate = simpleDateFormat.format(nowDate);
        leaderMbr.setUqstnNo(participationDetailDto.getUqstnNo());
        leaderMbr.setLcorsNo(participationDetailDto.getLcorsNo());

        ParticipationDetailDto evalRsltDtl = participationMapper.goQueryEvalRsltDtlInfo(leaderMbr);

        // 알림 URL : 평가결과 목록
        String pstartCn = messageSource
                .getMessage("usr.participation.msg.cancelsched1", null, LocaleContextHolder.getLocale())
                + HTML_BR +
                messageSource.getMessage(
                        "usr.participation.msg.cancelsched2", null, LocaleContextHolder.getLocale())
                + HTML_BR +
                messageSource.getMessage("usr.participation.msg.cancelsched3", null,
                        LocaleContextHolder.getLocale())
                + HTML_BR +
                teamStr1 + "<br/><br/>" +
                messageSource.getMessage("usr.participation.msg.cancelsched.title", null,
                        LocaleContextHolder.getLocale())
                +
                messageSource.getMessage("usr.participation.msg.cancelsched8", null,
                        LocaleContextHolder.getLocale())
                + evalRsltDtl.getProjectNm() + HTML_BR +
                messageSource.getMessage("usr.participation.msg.cancelsched7", null,
                        LocaleContextHolder.getLocale())
                + strNowDate;

        // 공통 DTO로 변환
        NtcBbsBasDTO ntcBbsBasDTO = new NtcBbsBasDTO();
        ntcBbsBasDTO.setInstCd(member.getInstCd());
        ntcBbsBasDTO.setMbrId(leaderMbr.getMbrId()); // 수신자
        ntcBbsBasDTO.setRgtrId(member.getMbrId()); // 발신자
        ntcBbsBasDTO.setReadYn("N");
        // 수신자 역할코드
        ntcBbsBasDTO.setRoleCd(roleCd);
        // 발신자 역할코드 설정
        ntcBbsBasDTO.setRgtrRoleCd(roleCd);
        ntcBbsBasDTO.setPstartTitlNm(messageSource.getMessage("usr.participation.msg.cancelsched.lcorsEndStr",
                new String[] { leaderMbr.getProjectNm() }, LocaleContextHolder.getLocale())); // 게시물제목명
        ntcBbsBasDTO.setPstartCn(pstartCn);
        ntcBbsBasDTO.setNtcDivCd(NTC_DIV_CD_101); // 학습
        ntcBbsBasDTO.setSysDivCd(DM_SYS_DIV_CD_20); // 동료평가 결과
        ntcBbsBasDTO.setCallPstartSn(participationDetailDto.getProjectNo());
        ntcBbsBasDTO.setCallPstartSn2(participationDetailDto.getLcorsNo());
        ntcBbsBasDTO.setCallPstartSn3(participationDetailDto.getUqstnNo());
        // 알림 게시판 기본 등록
        ntcBbsBasMapper.save(ntcBbsBasDTO);
        Member receiveMember = memberMapper.getMember(leaderMbr.getMbrId());
        // participationDetailDto에서 mtlEvlSn을 가져와야 하는데, 없을 수 있으므로 null 체크 필요
        Long referenceId = null;
        if (participationDetailDto.getMtlEvlSn() != null
                && !participationDetailDto.getMtlEvlSn().isEmpty()) {
            referenceId = Long.parseLong(participationDetailDto.getMtlEvlSn());
        } else if (participationDetailDto.getEvlNo() != null) {
            referenceId = participationDetailDto.getEvlNo().longValue();
        }

        // 템플릿 조회 후 빌더 호출
        NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);

        String subtitle = messageSource.getMessage("notification.evaluation.partner.complete", null, LocaleContextHolder.getLocale());
        String discordNotificationType = messageSource.getMessage("notification.evaluation.complete.msg", null, LocaleContextHolder.getLocale());

        String evlMsg4 = messageSource.getMessage("usr.per.permission4", null, LocaleContextHolder.getLocale());
        String evlMsg5 = messageSource.getMessage("usr.per.permission5", null, LocaleContextHolder.getLocale());
        String evlMsg6 = messageSource.getMessage("usr.per.permission6", null, LocaleContextHolder.getLocale());

        String notificationBody = evlMsg4 + " " + evalRsltDtl.getProjectNm() + HTML_BR +
                evlMsg5 + " " + evalRsltDtl.getLcorsNm() + HTML_BR +
                evlMsg6 + " " + evalRsltDtl.getUqstnNm();

        NotificationMessage notificationMessage = commonNotificationMessageBuilder
                .buildContentNotificationMessage(
                        template, referenceId, member, receiveMember,
                        member.getInstCd(), leaderMbr.getRoleCd(), subtitle, notificationBody,
                        discordNotificationType);

        try {
            applicationEventPublisher.publishEvent(
                    new NotificationEvent(EventBoundary.EVALUATION, EventDetail.COMPLETE,
                            notificationMessage));
        } catch (RuntimeException e) {
            log.error("Error publishing notification event");
        }
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("teamMembers", teamInfo);
        responseMessage.setResult(resultMap);

        return responseMessage;
    }

    /**
     * [#70] 동료평가 FAIL 시 미진행 평가자 자동 차수 이동
     * 같은 평가번호/차수에서 평가요청중('00001')·평가대기중('00002')인 평가자(본인 제외)가 있으면
     * 평가기본 신규 차수(MAX(EVL_DEGR)+1)를 생성하고 해당 평가자들을 신규 차수로 이동한다.
     * '00003'(평가진행중) 평가자는 이동하지 않고 현재 차수에서 계속 진행한다.
     * 호출자(evlEndSave)의 트랜잭션을 공유하므로 @Transactional을 부착하지 않는다.
     *
     * @param participationDetailDto
     * @return 차수 이동 수행 시 true, 이동 대상이 없으면 false
     */
    /**
     * 이 미션이 누적(CUMULATIVE) 통과방식인지 (#159).
     *
     * <p>조회에 실패하거나 설정이 없으면 기존 동작인 연속(CONSECUTIVE)으로 본다 —
     * 통과방식을 잘못 읽어 FAIL 을 통과로 바꾸는 쪽으로 흐르지 않게 한다.
     */
    private boolean isCumulativePass(ParticipationDetailDto participationDetailDto) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("projectNo", participationDetailDto.getProjectNo());
            params.put("lcorsNo", participationDetailDto.getLcorsNo());
            params.put("uqstnNo", participationDetailDto.getUqstnNo());
            return PeerPassMethod.CUMULATIVE.name().equals(passJudgmentMapper.selectPeerPassAccmCd(params));
        } catch (RuntimeException e) {
            log.error("[#159] 통과방식 조회 실패 — 연속으로 처리한다. evlNo={}, evlDegr={}",
                    participationDetailDto.getEvlNo(), participationDetailDto.getEvlDegr(), e);
            return false;
        }
    }

    private boolean rolloverPeerEvaluationIfPossible(ParticipationDetailDto participationDetailDto) {
        // 차수 이동 대상(미진행 평가자) 수 조회
        int rolloverTargetCount = participationMapper.selectRolloverTargetCount(participationDetailDto);
        if (rolloverTargetCount == 0) {
            return false;
        }

        // 평가기본 신규 차수 생성 (selectKey로 newEvlDegr 채번 — evlDegr는 기존 차수 종료용으로 보존)
        participationMapper.insertEvlBasEvlDegr(participationDetailDto);

        // 미진행 평가자들의 상호평가내역을 신규 차수로 이동
        // (평가가능일정기본은 MTL_EVL_SN으로 연결되어 별도 갱신 없이 기존 평가 일정 유지)
        participationMapper.updateMtlEvlTxnRolloverDegr(participationDetailDto);

        log.info("[#70] 동료평가 FAIL 차수 이동 — evlNo={}, evlDegr={} → newEvlDegr={}, 이동대상={}명",
                participationDetailDto.getEvlNo(), participationDetailDto.getEvlDegr(),
                participationDetailDto.getNewEvlDegr(), rolloverTargetCount);

        return true;
    }

    /**
     * 피드백 삭제
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage fdbDelete(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();

        String mtlEvlStusCd = participationMapper.selectMtlEvlStusCd(participationDetailDto);

        if (mtlEvlStusCd.equals(MTL_EVL_STUS_CD_6)) {
            throw new ValidCustomException(messageSource.getMessage("usr.participation.msg.alreadyConfirm", null, LocaleContextHolder.getLocale()));
        }

        responseMessage.setResult(participationMapper.fdbDelete(participationDetailDto));
        return responseMessage;
    }

    /**
     * 평가참여 상세
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage evlInfoDetail(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        // 프로젝트 정보 조회
        ParticipationDetailDto projectInfo = (ParticipationDetailDto) this.projectInfo(participationDetailDto)
                .getResult();
        // 단위문제 정보 조회
        ParticipationDetailDto uqstnInfo = (ParticipationDetailDto) this.uqstnInfoDetail(participationDetailDto)
                .getResult();
        // 평가상세이력 내용 조회
        List<ParticipationDetailDto> evlInfoList = (List<ParticipationDetailDto>) this.evlInfo(participationDetailDto)
                .getResult();
        // 자동평가 조회
        EvalutionResultAutoEvlListDto autoEvlInfo = (EvalutionResultAutoEvlListDto) this
                .autoEvlDetail(participationDetailDto).getResult();

        resultMap.put("projectInfo", projectInfo);
        resultMap.put("evlInfoList", evlInfoList);
        resultMap.put("autoEvlInfo", autoEvlInfo);
        resultMap.put("uqstnInfo", uqstnInfo);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가자료, 자동평가, 평가문항, 보너스점수/평가결과, 역량점수, 피드백 조회
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage evlDetailDataList(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        // 평가문항 정보 조회
        List<ParticipationDetailDto> uqstnQitemCnList = (List<ParticipationDetailDto>) this
                .uqstnQitemCnDetail(participationDetailDto).getResult();
        // 평가결과 정보 조회
        ParticipationDetailDto adtnlScrInfo = (ParticipationDetailDto) this.adtnlScrDetail(participationDetailDto)
                .getResult();
        // 역량점수 조회
        List<ParticipationDetailDto> cpblyList = (List<ParticipationDetailDto>) this.cpblyDetail(participationDetailDto)
                .getResult();
        // 피드백 조회 (확정 후 상세보기에서 호출되므로 상태 검증 생략)
        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko";
        participationDetailDto.setLanguage(language);
        List<ParticipationDetailDto> fdbList = participationMapper.evlFdbDetail(participationDetailDto);

        resultMap.put("uqstnQitemCnList", uqstnQitemCnList);
        resultMap.put("adtnlScrInfo", adtnlScrInfo);
        resultMap.put("cpblyList", cpblyList);
        resultMap.put("fdbList", fdbList);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가참여 확정처리
     *
     * @param participationDetailDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage evlTestEndSave(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();

        // 학습과정 마지막 평가자 확인
        String lastLcors = null;
        // 프로젝트 학습과정 마지막 확인
        String lastPjtLcors = null;

        participationMapper.evlTestEndPOF(participationDetailDto); // EVL_BAS 테이블 평가결과 저장
        participationMapper.lastUqstnExec1(participationDetailDto); // 단위문제 평가내역 저장
        // participationMapper.lastUqstnExec2(participationDetailDto); // 역량문제 평가내역 저장
        //
        // if (!Constants.LCORS_UQSTN_NO.equals(participationDetailDto.getUqstnNo())) {
        // participationMapper.lastLcorsExec2p5(participationDetailDto); //
        // 학습과정단위문제팀별강제종료 업데이트
        // }
        //
        // lastLcors = participationMapper.getLastLcors(participationDetailDto); // 학습과정
        // 마지막 확인 구분자
        // if (Constants.USE_YN_Y.equals(lastLcors)) {
        // participationMapper.lastLcorsExec1(participationDetailDto); // 학습과정평가내역 저장
        // participationMapper.lastLcorsExec2(participationDetailDto); // 학습과정역량평가내역 저장
        // }
        //
        // lastPjtLcors = participationMapper.getLastPjtLcors(participationDetailDto);
        // // 프로젝트 학습과정 마지막 확인
        // if (Constants.USE_YN_Y.equals(lastPjtLcors) &&
        // Constants.USE_YN_Y.equals(lastLcors)) {
        // participationMapper.lastLcorsExec3(participationDetailDto); // 팀상태코드 업데이트
        // }

        return responseMessage;
    }

    /**
     * 클라이언트 IP 주소 조회
     * 
     * @param req
     * @return
     * @throws UnknownHostException
     */
    public static String getClientIp(HttpServletRequest req) throws UnknownHostException {
        String ip = req.getHeader("X-FORWARDED-FOR");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-RealIP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("REMOTE_ADDR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            InetAddress address = InetAddress.getLocalHost();
            ip = address.getHostAddress();
        }

        return ip;
    }

    public Boolean participationIsAllowedIpAddr(String ipAddr, String instCd) {
        // 허용되는 IP인지 확인
        List<String> participationIpAddrList = participationMapper.getParticipationIpAddrList(instCd);

        if (participationIpAddrList.isEmpty()) {
            return true;
        } else if (participationIpAddrList.contains(ipAddr)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 평가 피드백 만족도 점수 업데이트
     *
     * @param participationDetailDto
     * @return
     */
    public ResponseMessage updateFdbkSatScr(ParticipationDetailDto participationDetailDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        try {
            participationMapper.updateFdbkSatScr(participationDetailDto);
            responseMessage.setResult("success");
        } catch (RuntimeException e) {
            responseMessage.setResult("fail");
            responseMessage.setCode(HttpStatus.BAD_REQUEST.value());
        }

        return responseMessage;
    }

    /**
     * evlNo 로 EvaluationBas 를 조회한 뒤 PJT_LCORS_UQSTN.TEAM_COMPOSITION 을 확인해
     * 팀 미션(개인='00000' 외) 여부 반환. evlNo 가 없거나 조회 실패 시 false (= 개인 처리).
     */
    private boolean isTeamMissionByEvlNo(BigDecimal evlNo) {
        if (evlNo == null) {
            return false;
        }
        try {
            EvaluationBas evlBas = evaluationAppendMapper.getEvaluationBasByEvlNo(evlNo.longValue());
            if (evlBas == null
                    || evlBas.getProjectNo() == null
                    || evlBas.getLcorsNo() == null
                    || evlBas.getUqstnNo() == null) {
                return false;
            }
            String teamComposition = evaluationAppendMapper.getTeamComposition(
                    evlBas.getProjectNo(), evlBas.getLcorsNo(), evlBas.getUqstnNo());
            return teamComposition != null && !"00000".equals(teamComposition);
        } catch (RuntimeException ignore) {
            return false;
        }
    }

}
