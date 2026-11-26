package kr.innoaca.usr.web.ev.result.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.usr.web.ev.request.dto.LrnCorsNmUqstnNmDto;
import kr.innoaca.usr.web.ev.result.dto.EvalutionResultListDto;
import kr.innoaca.usr.web.ev.result.service.EvalutionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/ev/result")
@RequiredArgsConstructor
public class EvalutionResultRestController {
    private final EvalutionResultService evalutionResultService;

    /**
     * 평가결과 목록 조회
     *
     * @param evalutionResultListDto
     * @return ResponseMessage
     */
    @PostMapping("/evalutionSelectEvaResultList")
    public ResponseMessage selectEvaResultList(EvalutionResultListDto evalutionResultListDto, @AuthenticationPrincipal Member member) {
        evalutionResultListDto.setMbrId(member.getMbrId());
        evalutionResultListDto.setInstCd(member.getInstCd());
        return evalutionResultService.selectEvaResultList(evalutionResultListDto);
    }


}
