package kr.innoaca.usr.web.ev.request.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.web.code.service.CodeService;
import kr.innoaca.usr.common.utils.Criteria;
import kr.innoaca.usr.web.cr.dto.CodeReviewListDto;
import kr.innoaca.usr.web.cr.service.CodeReviewService;
import kr.innoaca.usr.web.ev.request.dto.EvalutionListDto;
import kr.innoaca.usr.web.ev.request.dto.ProjectDto;
import kr.innoaca.usr.web.ev.request.service.EvalutionReqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * @package kr.inno.usr.evalution.request.controller
 * @name EvalutionController
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/
@Slf4j
@Controller
@RequestMapping("/ev/request")
@RequiredArgsConstructor
public class EvalutionReqController {
    private final MessageSource messageSource;
    private final CodeService codeService;
    private final EvalutionReqService evalutionReqService;
    private final CodeReviewService codeReviewService;

    /**
     * 평가요청 검색 페이지로 이동
     *
     * @param model
     * @param request
     * @return String
     */
    @RequestMapping(value = "/evalutionRequestList", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionRequest(HttpServletRequest request, Model model) {
        // main 화면에서 rending 시 상태 값
        String evlStusCd = request.getParameter("evlStusCd");

        model.addAttribute("loadEvlStusCd", evlStusCd);
        model.addAttribute("code", codeService.getCodeInfo("LP012"));   // 평가상태코드
        return "ev/request/evalutionRequestList";
    }

    /**
     * 평가요청 페이지로 이동
     *
     * @param model
     * @param locale
     * @param member
     * @param request
     * @return String
     */

    @RequestMapping(value = "/evalutionRequestWrite", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionRequestWrite(Model model, Locale locale, @AuthenticationPrincipal Member member, HttpServletRequest request) {
        // evalutionRequestList 화면, main에서 진입 시 파라미터 값
        String projectNo = request.getParameter("projectNo");
        String lcorsNo = request.getParameter("lcorsNo");
        String uqstnNo = request.getParameter("uqstnNo");

        ProjectDto projectDto = new ProjectDto();
        projectDto.setMbrId(member.getMbrId());
        projectDto.setInstCd(member.getInstCd());
        projectDto.setCntNo(messageSource.getMessage("evalreq.body.cntNo", null, locale));

        model.addAttribute("member", member);               // 로그인정보
        model.addAttribute("loadProjectNo", projectNo);     // 프로젝트번호(상위)
        model.addAttribute("loadLcorsNo", lcorsNo);         // 학습과정번호
        model.addAttribute("loadUqstnNo", uqstnNo);         // 단위문제번호
        model.addAttribute("projectNmList", evalutionReqService.selectProjectNmList(projectDto)); // 프로젝트명 목록 조회


        // 마지막 평가 정보를 조회한다.
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

        model.addAttribute("lastProjectNo", lastProjectNo);
        model.addAttribute("lastUpProjectNo", lastUpProjectNo);
        model.addAttribute("lastLcorsNo", lastLcorsNo);
        model.addAttribute("lastUqstnNo", lastUqstnNo);

        return "ev/request/evalutionRequestWrite";
    }
}
