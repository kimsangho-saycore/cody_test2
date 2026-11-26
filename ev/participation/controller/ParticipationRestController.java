package kr.innoaca.usr.web.ev.participation.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.web.point.service.PointService;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDetailDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDto;
import kr.innoaca.usr.web.ev.participation.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.net.UnknownHostException;

/**
 * @package kr.inno.usr.evalution.request.controller
 * @name ParticipationRestController
 * @date 2023-11-10
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-10        zeroj0706       최초 생성
 **/
@Slf4j
@RestController
@RequestMapping("/ev/participation")
@RequiredArgsConstructor
public class ParticipationRestController {
    private final ParticipationService participationService;
    private final PointService pointService;

    /**
     * 평가요청 조회
     * 
     * @param participationDto
     * @return
     */
    @PostMapping("/seachParticipationList")
    public ResponseMessage participationList(ParticipationDto participationDto) {
        return participationService.participationList(participationDto);
    }

    /**
     * 평가 요청 전체 조회
     *
     * @param participationDto
     * @return
     */
    @PostMapping("/searchParticipationAllList")
    public ResponseMessage participationAllList(ParticipationDto participationDto) {
        return participationService.participationListAll(participationDto);
    }

    /**
     * 평가요청 수락/거절처리
     *
     * @param participationDto
     * @return
     */
    @PostMapping("/evalutionAcceptRefuseUpdate")
    public ResponseMessage evalutionAcceptRefuseUpdate(ParticipationDto participationDto, @AuthenticationPrincipal Member member) {
        return participationService.evalutionAcceptRefuseUpdate(participationDto, member);
    }

    /**
     * 포인트 부여를 위해 거절 처리 Controller 분리
     */
    @PostMapping("/evaluationAcceptRefuseCancleUpdate")
    public ResponseMessage evaluationAcceptRefuseCancleUpdate(ParticipationDto participationDto, @AuthenticationPrincipal Member member) {
        return participationService.evalutionAcceptRefuseUpdate(participationDto, member);
    }

    /**
     * 프로젝트학습정보 조회
     * 
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/evlSaveDetail")
    public ResponseMessage projectInfoDetail(ParticipationDetailDto participationDetailDto) {
        return participationService.projectInfoDetail(participationDetailDto);
    }

    /**
     * 상호평가상태코드 수정
     *
     * @param participationDto
     * @return
     */
    @PostMapping("/mtlEvlStusCdUpdate")
    public ResponseMessage mtlEvlStusCdUpdate(ParticipationDto participationDto) {
        return participationService.mtlEvlStusCdUpdate(participationDto);
    }

    /**
     * 동료 평가 규칙 확인 이력 기록 (#351)
     * 평가 진행 시작 직전 규칙 게이트를 통과한 당사자가 [코디세이 동료 평가 규칙]을 확인한 시점을 기록한다.
     * 확인 주체·기관은 세션에서 도출한다.
     *
     * @param dto    mtlEvlSn(평가건), ruleVer(규칙 버전)
     * @param member 세션 회원(확인 당사자)
     */
    @PostMapping("/ruleConfirmSave")
    public ResponseMessage ruleConfirmSave(kr.innoaca.usr.web.ev.participation.dto.EvlRuleCnfrmDto dto,
                                           @AuthenticationPrincipal Member member) {
        return participationService.saveRuleConfirm(member, dto);
    }

    /**
     * 피드백 조회
     *
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/fdbList")
    public ResponseMessage fdbList(ParticipationDetailDto participationDetailDto) {
        return participationService.fdbList(participationDetailDto);
    }

    /**
     * 평가피드백 등록
     * 
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/fdbSave")
    public ResponseMessage fdbSave(ParticipationDetailDto participationDetailDto) {
        return participationService.fdbSave(participationDetailDto);
    }

    /**
     * 평가 저장
     *
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/evlSave")
    public ResponseMessage evlSave(@RequestBody ParticipationDetailDto participationDetailDto) {
        return participationService.evlSave(participationDetailDto);
    }

    /**
     * 평가확정 전 데이터 체크
     * 
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/saveChckDetail")
    public ResponseMessage saveChckDetail(ParticipationDetailDto participationDetailDto) {
        return participationService.saveChckDetail(participationDetailDto);
    }

    /**
     * 평가확정처리
     *
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/evlEndSave")
    public ResponseMessage evlEndSave(ParticipationDetailDto participationDetailDto, @AuthenticationPrincipal Member member) {
         return participationService.evlEndSave(participationDetailDto, member);
    }

    /**
     * 피드백 삭제
     * 
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/fdbDelete")
    public ResponseMessage fdbDelete(ParticipationDetailDto participationDetailDto) {
        return participationService.fdbDelete(participationDetailDto);
    }

    /**
     * 평가참여 상세
     *
     * @return
     */
    @PostMapping("/evlInfoDetail")
    public ResponseMessage evlInfoDetail(ParticipationDetailDto participationDetailDto) {
        return participationService.evlInfoDetail(participationDetailDto);
    }

    /**
     * 평가자료, 자동평가, 평가문항, 보너스점수/평가결과, 역량점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/evlDetailDataList")
    public ResponseMessage evlDetailDataList(ParticipationDetailDto participationDetailDto) {
        return participationService.evlDetailDataList(participationDetailDto);
    }

    /**
     * 평가확정처리
     *
     * @param participationDetailDto
     * @return
     */
    @PostMapping("/evlTestEndSave")
    public ResponseMessage evlTestEndSave(ParticipationDetailDto participationDetailDto) {
        return participationService.evlTestEndSave(participationDetailDto);
    }


    @GetMapping("/usr/paper/ipAddrList")
    public ResponseMessage participationIsAllowedIp(HttpServletRequest request, @AuthenticationPrincipal Member member) throws UnknownHostException {

        String clientIp = participationService.getClientIp(request);

        return ResponseMessage.ok(participationService.participationIsAllowedIpAddr(clientIp, member.getInstCd()));
    }

    @PostMapping(value = "/fdbkSatScr/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage updateFdbkSatScr(@RequestBody ParticipationDetailDto participationDetailDto, @AuthenticationPrincipal Member member) {
        participationDetailDto.setMbrId(member.getMbrId());
        participationDetailDto.setInstCd(member.getInstCd());
        return participationService.updateFdbkSatScr(participationDetailDto);
    }
}