package kr.innoaca.usr.web.ev.request.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.exception.ValidCustomException;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.web.code.service.CodeService;
import kr.innoaca.usr.evaluation.MentorEvaluationAppendRequest;
import kr.innoaca.usr.evaluation.dto.AppendReviewValidateRequest;
import kr.innoaca.usr.web.ev.request.dto.*;
import kr.innoaca.usr.web.ev.request.service.EvaluationCommandService;
import kr.innoaca.usr.web.ev.request.service.EvalutionReqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import static kr.innoaca.core.common.response.ResponseMessage.ok;

/**
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/

@Slf4j
@RestController
@RequestMapping("/ev/request")
@RequiredArgsConstructor
public class EvalutionReqRestController {
    private final EvalutionReqService evalutionReqService;
    private final EvaluationCommandService evaluationCommandService;
    private final CodeService codeService;

    /**
     * 학습자ID로 프로젝트의 학습과정 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return ResponseMessage
     */
    @PostMapping("/selectLrnCorsNmList")
    public ResponseMessage selectLrnCorsNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto, @AuthenticationPrincipal Member member) {
        return evalutionReqService.selectLrnCorsNmList(lrnCorsNmUqstnNmDto, member);
    }

    /**
     * 학습자ID로 학습과정의 단위문제 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return ResponseMessage
     */
    @PostMapping("/selectLrnUqstnNmList")
    public ResponseMessage selectLrnUqstnNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto, @AuthenticationPrincipal Member member) {

        return evalutionReqService.selectLrnUqstnNmList(lrnCorsNmUqstnNmDto, member);
    }

    /**
     * 평가기본, 평가요청내역, 자동평가내역, 상호평가목록 조회
     *
     * @param evalDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/evlTotList")
    public ResponseMessage evlTotList(EvalutionDataTxnDto evalDataTxnDto) {
        return evalutionReqService.evlTotList(evalDataTxnDto);
    }

    /**
     * git 주소 선택할 멤버 조회
     *
     * @param lcorsMbrPtpTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/selectGitAddrMbrIdsList")
    public ResponseMessage selectGitAddrMbrIdsList(LcorsMbrPtpTxnDto lcorsMbrPtpTxnDto) {
        return evalutionReqService.selectGitAddrMbrIdsList(lcorsMbrPtpTxnDto);
    }

    /**
     * 평가요청현황목록 조회
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/mtlEvlTxnDtoByPkList")
    public ResponseMessage mtlEvlTxnDtoByPkList(EvalutionDataTxnDto evalutionDataTxnDto, @AuthenticationPrincipal Member member) {
        evalutionDataTxnDto.setMbrId(member.getMbrId());
        return evalutionReqService.mtlEvlTxnDtoByPkList(evalutionDataTxnDto);
    }

    /**
     * 평가자 지정 : 학습과정별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return ResponseMessage
     */
    @PostMapping("/selectLcorsEvlStdList")
    public ResponseMessage selectLcorsEvlStdList(EvalutionStdDto evalutionStdDto) {
        return evalutionReqService.selectLcorsEvlStdList(evalutionStdDto);
    }

    /**
     * 평가자 지정 : 단위문제별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return ResponseMessage
     */
    @PostMapping("/selectUqstnEvlStdList")
    public ResponseMessage selectUqstnEvlStdList(EvalutionStdDto evalutionStdDto) {
        return evalutionReqService.selectUqstnEvlStdList(evalutionStdDto);
    }

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @PostMapping("/selectCase1MbrIdList")
    public ResponseMessage selectCase1MbrIdList(EvalutionMbrDto evalutionMbrDto) {
        return evalutionReqService.selectCase1MbrIdList(evalutionMbrDto);
    }

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @PostMapping("/selectCase2MbrIdList")
    public ResponseMessage selectCase2MbrIdList(EvalutionMbrDto evalutionMbrDto) {
        return evalutionReqService.selectCase2MbrIdList(evalutionMbrDto);
    }

    /**
     * 평가자 지정 : 평가자 대상범위가 학습과정 수강자 일 경우 동료 평가자 평가가능시간 목록 조회
     *
     * @param enbldEvlTimeDto
     * @return List<EnbldEvlTimeDto>
     */
    @PostMapping("/selectCase1EnbldEvlTimeList")
    public ResponseMessage selectCase1EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto) {
        return evalutionReqService.selectCase1EnbldEvlTimeList(enbldEvlTimeDto);
    }

    /**
     * 평가자 지정 : 평가자 대상범위가 학습자 회원 일 경우 동료 평가자 평가가능시간 목록 조회
     * 2026-04-10 전체 수강생 기준 동료 평가자 평가가능시간 목록 조회
     * @param enbldEvlTimeDto
     * @return List<EnbldEvlTimeDto>
     */
    @PostMapping("/selectCase2EnbldEvlTimeList")
    public ResponseMessage selectCase2EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto) {
        return evalutionReqService.selectCase2EnbldEvlTimeList(enbldEvlTimeDto);
    }

    /**
     * 동료평가 슬롯 조회 (시간+인원수만 반환, 개인정보 미노출)
     */
    @PostMapping("/selectPeerEvlSlotList")
    public ResponseMessage selectPeerEvlSlotList(PeerEvlSlotDto peerEvlSlotDto,
                                                  @RequestParam(defaultValue = "00001") String scopeCd) {
        return evalutionReqService.selectPeerEvlSlotList(peerEvlSlotDto, scopeCd);
    }

    /**
     * 자동평가 요청 전 이전평가 결과 fail인경우 재평가 기간 조회
     *
     * @param reEvalChkDto
     * @return ResponseMessage
     */
    @PostMapping("/selectReEvalTimeList")
    public ResponseMessage selectReEvalTimeList(ReEvalChkDto reEvalChkDto) {
        return evalutionReqService.selectReEvalTimeList(reEvalChkDto);
    }

    /**
     * 자동평가 요청 api 호출
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/api/reqAutoEvalList")
    public ResponseMessage reqAutoEvalList(EvalutionDataTxnDto evalutionDataTxnDto,
            @AuthenticationPrincipal Member member) {
        return evalutionReqService.reqAutoEvalList(evalutionDataTxnDto, member);
    }

    /**
     * 포인트 총계 정보 연계(평가자 지정 popup 화면에서 평가 요청 하기 전 확인)
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/api/getPointNowScrDetail")
    public ResponseMessage apiCmmnGetPointNowScr(EvalutionDataTxnDto evalutionDataTxnDto) {
        return evalutionReqService.apiCmmnGetPointNowScr(evalutionDataTxnDto);
    }

//    @PostMapping("/regReqEvalSave")
//    public ResponseMessage regReqEvalSave(RegReqEvalDto regReqEvalDto, @AuthenticationPrincipal Member member) {
//        return evalutionReqService.regReqEvalSave(regReqEvalDto, member);
//    }

    /**
     * 동료평가 요청/등록
     * @param request
     * @param member
     * @return
     */
    @PostMapping("/regReqEvalSave")
    public ResponseMessage regReqEvalSave( @AuthenticationPrincipal Member member,
                                           @RequestBody MentorEvaluationAppendRequest request ) {
        return ok(evaluationCommandService.appendPartnerEvaluationRequest(member, request));
    }

    /**
     * 동료평가 요청/등록 (가중 랜덤 배정 - Phase 3)
     */
    @PostMapping("/regWeightedPartnerEvalSave")
    public ResponseMessage regWeightedPartnerEvalSave(
            @AuthenticationPrincipal Member member,
            @RequestBody MentorEvaluationAppendRequest request) {
        log.info("[regWeightedPartnerEvalSave] member={}, instCd={}, bgngDt={}, beginTimes={}, projectNo={}, lcorsNo={}, uqstnNo={}, teamSn={}, mbrId={}, cn={}",
                member.getMbrId(), request.getInstCd(), request.getBgngDt(), request.getBeginTimes(),
                request.getProjectNo(), request.getLcorsNo(), request.getUqstnNo(), request.getTeamSn(),
                request.getMbrId(), request.getCn());
        try {
            return ok(evaluationCommandService.appendWeightedPartnerEvaluationRequest(member, request));
        } catch (Exception e) {
            log.error("[regWeightedPartnerEvalSave] 에러 발생", e);
            throw e;
        }
    }

    /**
     * 재평가 요청 (통과 문제 재도전) — 평가 리셋만 수행
     */
    @PostMapping("/regReEvaluationSave")
    public ResponseMessage regReEvaluationSave(
            @AuthenticationPrincipal Member member,
            @RequestBody MentorEvaluationAppendRequest request) {
        log.info("[regReEvaluationSave] member={}, projectNo={}, lcorsNo={}, uqstnNo={}, teamSn={}",
                member.getMbrId(), request.getProjectNo(), request.getLcorsNo(),
                request.getUqstnNo(), request.getTeamSn());
        try {
            evaluationCommandService.resetForReEvaluation(member, request);
            return ok(null);
        } catch (Exception e) {
            log.error("[regReEvaluationSave] 에러 발생", e);
            throw e;
        }
    }

    /**
     *  자동평가 요청/등록
     */
    @PostMapping("/regAutoReqEvalSave")
    public ResponseMessage regAutoReqEvalSave( @AuthenticationPrincipal Member member,
                                           @RequestBody MentorEvaluationAppendRequest request ) {
        return ok(evaluationCommandService.appendAutoEvaluationRequest(member, request));
    }

    /**
     * KAN-210 입학선발 -시험제출 요청/등록
     */
    @PostMapping("/regExamReqEvalSave")
    public ResponseMessage regExamReqEvalSave( @AuthenticationPrincipal Member member,
                                           @RequestBody MentorEvaluationAppendRequest request ) {
        return ok(evaluationCommandService.appendExamEvaluationRequest(member, request));
    }

    /**
     * 선택된 멤버(학습자)의 git 주소 api 호출
     *
     * @param gitlabPrjCreator
     * @return ResponseMessage
     */
    @PostMapping("/api/gitlab/project/list")
    public ResponseMessage apiListProjectList(GitlabPrjCreator gitlabPrjCreator)  {
        return evalutionReqService.apiListProjectList(gitlabPrjCreator);
    }

    /**
     * 평가자료내역 등록 : 기존 평가요청자료에 자료등록일련번호를 증가시켜 insert
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/evlDataTxnByUqstnNoSave")
    public ResponseMessage evlDataTxnByUqstnNoSave(EvalutionDataTxnDto evalutionDataTxnDto) throws UnsupportedEncodingException {
        return evalutionReqService.evlDataTxnByUqstnNoSave(evalutionDataTxnDto);
    }

    /**
     * 평가기본, 평가자료내역 신규 insert
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/newEvlBasToDataTxnByUqstnNoSave")
    public ResponseMessage newEvlBasToDataTxnByUqstnNoSave(EvalutionDataTxnDto evalutionDataTxnDto) {
        return evalutionReqService.newEvlBasToDataTxnByUqstnNoSave(evalutionDataTxnDto);
    }

    /**
     * 평가자지정 후 Git Lab 프로젝트 기존 멤버 삭제, 변경 멤버 추가 API 호출에 사용
     *
     * @param chgSrccUrlAddrDto
     * @return ResponseMessage
     */
    @PostMapping("/api/gitlab/proxy/project/member/srccdUrlAddrUpdate")
    public ResponseMessage srccdUrlAddrUpdate(ChgSrccUrlAddrDto chgSrccUrlAddrDto)  {
        return evalutionReqService.srccdUrlAddrUpdate(chgSrccUrlAddrDto);
    }

    /**
     * 멘토회원 정보 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @PostMapping("/selectMentoList")
    public ResponseMessage selectMentoList(EvalutionMbrDto evalutionMbrDto) {
        return evalutionReqService.selectMentoList(evalutionMbrDto);
    }

    /**
     * 전문가 평가자 평가 가능시간 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @PostMapping("/selectScheduleList")
    public ResponseMessage selectScheduleList(EvalutionMbrDto evalutionMbrDto) {
        return evalutionReqService.selectScheduleList(evalutionMbrDto);
    }

//    @PostMapping("/selectMentiReqSave")
//    public ResponseMessage selectMentiReqSave(@RequestBody ArrayList<MenteeReqDto> menteeReqDtoList, @AuthenticationPrincipal Member member) {
//        return evalutionReqService.selectMentiReqSave(menteeReqDtoList, member);
//    }

    /**
     * 멘토 평가 요청등록
     *
     * @return ResponseMessage
     */
    @PostMapping("/selectMentiReqSave")
    public ResponseMessage selectMentiReqSave(@AuthenticationPrincipal Member member,
                                              @RequestBody MentorEvaluationAppendRequest request) {
        return ok(evaluationCommandService.appendMentorEvaluationRequest(member, request));
    }

    /**
     * 포인트 히스토리 누적(수정)
     *
     * @param member
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/experienceScrUpdate")
    public ResponseMessage experienceScrUpdate(@AuthenticationPrincipal Member member, EvalutionDataTxnDto evalutionDataTxnDto) {
        return null;
    }

    /**
     * 평가 취소 패널티 처리
     * 
     * @param member
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/deductPointsUpdate")
    public ResponseMessage deductPointsUpdate(@AuthenticationPrincipal Member member, EvalutionDataTxnDto evalutionDataTxnDto) {
        return ResponseMessage.ok();
    }

    /**
     * 동료평가, 전문가평가 요청 시 호출되는 엔드포인트
     * @param member
     * @return
     */
    @PostMapping("/restorationPntUpdate")
    public ResponseMessage restorationPntUpdate(@AuthenticationPrincipal Member member) {
        return ResponseMessage.builder()
                .build();
    }

    /**
     * 평가자지정 후 Git Lab 프로젝트 멤버 추가 API 호출
     * @param evalutionBasDto
     * @return ResponseMessage

     */
    @PostMapping("/api/gitlab/proxy/project/member/save")
    public ResponseMessage apiProxyProjectMemberSave(EvalutionBasDto evalutionBasDto)  {
        return evalutionReqService.apiProxyProjectMemberSave(evalutionBasDto);
    }

    /**
     * 1. 평가요청으로 인해 포인트 적립/차감 연계
     * 2. 경험치 적립/차감 연계(성공 실패 상관 없이 다음 진행)
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/setPointScrByCnclReqUpdate")
    public ResponseMessage setPointScrByCnclReqUpdate(@AuthenticationPrincipal Member member, EvalutionDataTxnDto evalutionDataTxnDto) {
        return evalutionReqService.apiCmmnsetPointScrByCnclReq(member, evalutionDataTxnDto);
    }

    /**
     * 상호평가 요청취소
     *
     * @param mtlEvlTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/cancelMtlEvlTxnUpdate")
    public ResponseMessage cancelMtlEvlTxnUpdate(@AuthenticationPrincipal Member member, MtlEvlTxnDto mtlEvlTxnDto) {
        return evalutionReqService.cancelMtlEvlTxnUpdate(member, mtlEvlTxnDto);
    }

    /**
     * Git Lab 프로젝트 멤버 삭제
     *
     * @param mtlEvlTxnDto
     * @return ResponseMessage
     */
    @PostMapping("/api/gitlab/proxy/project/member/delete")
    public ResponseMessage gitlabDeleteProjectMember(MtlEvlTxnDto mtlEvlTxnDto)  {
        return evalutionReqService.gitlabDeleteProjectMember(mtlEvlTxnDto);
    }

    /**
     * 전문가 평가 요청 취소
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @PostMapping("/getMentiCancelUpdate")
    public ResponseMessage getMentiCancelUpdate(@AuthenticationPrincipal Member member, EvalutionMbrDto evalutionMbrDto) {
        return evalutionReqService.getMentiCancelUpdate(member, evalutionMbrDto);
    }

    /**
     * 평가요청 목록 조회
     * @param evalutionListDto
     * @return
     */
    @PostMapping("/searchList")
    public ResponseMessage searchList(@AuthenticationPrincipal Member member, EvalutionListDto evalutionListDto) {
        return evalutionReqService.searchList(member, evalutionListDto);
    }

    /**
     * 사용자 검색 - 평가요청 목록 조회
     * @param evalutionListDto
     * @return
     */
    @PostMapping("/mbrSearch/searchList")
    public ResponseMessage mbrSearchList(@AuthenticationPrincipal Member member, EvalutionListDto evalutionListDto) {

        return evalutionReqService.searchList(null, evalutionListDto);
    }

    /**
     * 학습자 검색 - 평가 상세 조회 (조회 전용, 민감정보 마스킹)
     * MbrSearchDetail에서 다른 학습자의 평가 현황을 조회할 때 사용
     *
     * @param member 인증된 사용자 (조회 요청자)
     * @param evalDataTxnDto 조회 대상 파라미터
     * @return ResponseMessage (민감정보 마스킹된 응답)
     */
    @PostMapping("/mbrSearch/evlDetail")
    public ResponseMessage mbrSearchEvlDetail(@AuthenticationPrincipal Member member, EvalutionDataTxnDto evalDataTxnDto) {
        if (member == null) {
            throw new ValidCustomException("Unauthorized");
        }
        return evalutionReqService.evlDetailForView(evalDataTxnDto);
    }

    /**
     * 파일 업로드
     *
     * @param evalutionDataTxnDto
     * @return
     */
    @PostMapping("/fileSave")
    public ResponseMessage fileSave(EvalutionDataTxnDto evalutionDataTxnDto) throws java.io.IOException {
        return evalutionReqService.fileSave(evalutionDataTxnDto);
    }

    @PostMapping("/validationPreSave")
    public ResponseMessage evaluationRequestValidate(@AuthenticationPrincipal Member member,
                                                     @RequestBody AppendReviewValidateRequest request) {
        evaluationCommandService.validate(member, request);
        return ok(null);
    }

    /**
     * 평가 요청 전 자기자신의 스케줄 충돌 체크
     */
    @PostMapping("/checkOwnScheduleConflictsList")
    public ResponseMessage checkOwnScheduleConflicts(@AuthenticationPrincipal Member member,
                                                     @RequestBody MentorEvaluationAppendRequest request) {
        return ok(evaluationCommandService.checkOwnScheduleConflicts(member, request));
    }


    /**
     * 평가 상태
     * @return
     */
    @GetMapping("/evlStusCdList")
    public ResponseMessage getEvlStusCdList(@RequestParam(required = false) String groupCd) {
        return ResponseMessage.ok(codeService.getCodeInfo(groupCd != null ? groupCd : "LP012"));
    }

    /**
     * 프로젝트명 목록 조회
     * @param projectDto
     * @param member
     * @return
     */
    @PostMapping("/selectProjectNmList")
    public ResponseMessage selectProjectNmList(@AuthenticationPrincipal Member member, ProjectDto projectDto) {
        if (projectDto.getMbrId() == null) {
            projectDto.setMbrId(member.getMbrId());
        }
        if (projectDto.getInstCd() == null) {
            projectDto.setInstCd(member.getInstCd());
        }
        return ResponseMessage.ok(evalutionReqService.selectProjectNmList(projectDto));
    }

    /**
     * 마지막 평가 항목 조회
     * @param member
     * @return
     */
    @GetMapping("/lastEvList")
    public ResponseMessage selectLastEvalutionList(@AuthenticationPrincipal Member member) {
        HashMap<String, Object> resultMap = new HashMap<>();
        EvalutionListDto evalutionListDto = new EvalutionListDto();
        evalutionListDto.setPage(1);
        evalutionListDto.setPagePerRows(1);

        String lastProjectNo = null, lastUpProjectNo = null, lastLcorsNo = null, lastUqstnNo = null;
        List<EvalutionListDto> list = evalutionReqService.getLastEvalutionList(member, evalutionListDto);
        if (list.size() > 0) {
            lastProjectNo = list.get(0).getProjectNo().toString();
            lastLcorsNo = list.get(0).getLcorsNo().toString();
            lastUqstnNo = list.get(0).getUqstnNo().toString();
        }

        resultMap.put("lastProjectNo", lastProjectNo);
        resultMap.put("lastUpProjectNo", lastUpProjectNo);
        resultMap.put("lastLcorsNo", lastLcorsNo);
        resultMap.put("lastUqstnNo", lastUqstnNo);

        return ResponseMessage.ok(resultMap);
    }

}
