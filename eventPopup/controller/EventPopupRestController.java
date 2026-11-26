package kr.innoaca.usr.web.eventPopup.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.web.eventPopup.service.EventPopupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이벤트 팝업 REST API 컨트롤러 (USR)
 *
 * @author system
 * @version 1.0.0
 */
@Slf4j
@RestController
public class EventPopupRestController {

    private final EventPopupService eventPopupService;

    public EventPopupRestController(@Qualifier("coreEventPopupService") EventPopupService eventPopupService) {
        this.eventPopupService = eventPopupService;
    }

    /**
     * 활성 이벤트 팝업 조회 API (학습자 포탈)
     * 조건에 맞는 활성 이벤트 팝업 목록을 조회합니다.
     * 조건:
     * - USE_YN = 'Y'
     * - 현재 날짜가 노출 기간 내
     * - 기관코드 일치 (미로그인 시 전체 노출 팝업만 조회)
     * - 노출시스템 코드 일치
     *
     * @param member 회원 정보 (세션에서 instCd, roleCd 추출). 미로그인 시 null
     * @param sysDivCd 시스템구분코드 (ADM: 운영자포탈, MAR: 포탈, MTR: 멘토포탈, STU: 학습자포탈). 제공되지 않으면 "STU" 사용
     * @param instCd 기관코드. 제공되지 않으면 member에서 추출
     * @return 활성 이벤트 팝업 목록
     */
    @GetMapping("/eventPopup/active/usrList")
    public ResponseMessage getActiveEventPopups(
        @RequestParam(required = false) String sysDivCd,
        @RequestParam(required = false) String instCd,
        @AuthenticationPrincipal Member member
    ) {
        // sysDivCd가 제공되지 않으면 기본값 "STU" 사용
        if (sysDivCd == null || sysDivCd.isEmpty()) {
            sysDivCd = "STU";
        }
        
        // instCd가 제공되지 않으면 member에서 추출
        if ((instCd == null || instCd.isEmpty())) {
            instCd = member != null ? member.getInstCd() : "00002";
            if (instCd == null) {
                instCd = "00002";
            }
        }

        return eventPopupService.getActiveEventPopups(sysDivCd, instCd);
    }
}
