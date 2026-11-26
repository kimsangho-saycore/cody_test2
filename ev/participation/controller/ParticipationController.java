package kr.innoaca.usr.web.ev.participation.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.web.code.service.CodeService;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDto;
import kr.innoaca.usr.web.ev.participation.mapper.ParticipationMapper;
import kr.innoaca.usr.web.ev.participation.service.ParticipationService;
import kr.innoaca.usr.web.ev.request.mapper.EvalutionReqMapper;
import kr.innoaca.usr.web.ev.result.dto.EvalutionResultAutoEvlListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @package kr.inno.usr.evalution.request.controller
 * @name ParticipationController
 * @date 2023-11-03
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-03        zeroj0706       최초 생성
 **/
@Slf4j
@Controller
@RequestMapping("/ev/participation")
@RequiredArgsConstructor
public class ParticipationController {
    private final CodeService codeService;

    private final ParticipationMapper participationMapper;

    private static final String COL_MEMBER  = "member";

    /**
     * 평가참여 검색 페이지로 이동
     *
     * @param member
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/evalutionParticipationList", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionParticipationList(@AuthenticationPrincipal Member member, HttpServletRequest request, Model model) {
        String loadEvlStusCd = request.getParameter("evlStusCd");

        model.addAttribute(COL_MEMBER, member);                                          // 로그인 정보
        model.addAttribute("evlStusCdList", codeService.getCodeInfo("LP019"));  // 상호평가상태코드
        model.addAttribute("evlResltCdList", codeService.getCodeInfo("LP010")); // 상호평가결과코드
        model.addAttribute("loadEvlStusCd", loadEvlStusCd);                            // 파라미터로 넘어온 상호평가상태값
        return "ev/participation/evalutionParticipationList";
    }

    /**
     * 평가참여 등록
     *
     * @param member
     * @param participationDto
     * @param model
     * @return
     */
    @RequestMapping(value = "/evalutionParticipationSave", method = {RequestMethod.GET, RequestMethod.POST})
    public String evaltuionParticipationView(@AuthenticationPrincipal Member member, ParticipationDto participationDto, Model model) {
        model.addAttribute(COL_MEMBER, member);                                          // 로그인 정보
        model.addAttribute("loadMtlEvlStusCd", participationDto.getMtlEvlStusCd());
        model.addAttribute("loadProjectNo", participationDto.getProjectNo());
        model.addAttribute("loadLcorsno", participationDto.getLcorsNo());
        model.addAttribute("loadTeamsn", participationDto.getTeamSn());
        model.addAttribute("loadUqstnno", participationDto.getUqstnNo());
        model.addAttribute("loadMtlEvlSn", participationDto.getMtlEvlSn());
        model.addAttribute("loadProjectNm", participationDto.getProjectNm());
        model.addAttribute("loadLcorsNm", participationDto.getLrnCorsNm());
        model.addAttribute("loadUqstnNm", participationDto.getUqstnNm());

        String language = "en".equals( LocaleContextHolder.getLocale().toString() ) ? "en" : "ko"; // Locale 객체 가져오기
        participationDto.setLanguage( language );

        EvalutionResultAutoEvlListDto evalutionResultAutoEvl = participationMapper.selectUqstnNoAutoEvlList(participationDto);
        model.addAttribute("autoEvl", evalutionResultAutoEvl);
        return "ev/participation/evalutionParticipationSave";
    }

    /**
     * 평가참여 상세
     * 
     * @param member
     * @param participationDto
     * @param model
     * @return
     */
    @RequestMapping(value = "/evalutionParticipationDetail", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionParticipationDetail(@AuthenticationPrincipal Member member, ParticipationDto participationDto, Model model) {
        model.addAttribute(COL_MEMBER, member);                                          // 로그인 정보
        model.addAttribute("loadMtlEvlStusCd", participationDto.getMtlEvlStusCd());
        model.addAttribute("loadProjectNo", participationDto.getProjectNo());
        model.addAttribute("loadLcorsno", participationDto.getLcorsNo());
        model.addAttribute("loadTeamsn", participationDto.getTeamSn());
        model.addAttribute("loadUqstnno", participationDto.getUqstnNo());
        model.addAttribute("loadMtlEvlSn", participationDto.getMtlEvlSn());
        model.addAttribute("loadProjectNm", participationDto.getProjectNm());
        model.addAttribute("loadLcorsNm", participationDto.getLrnCorsNm());
        model.addAttribute("loadUqstnNm", participationDto.getUqstnNm());
        return "ev/participation/evalutionParticipationDetail";
    }

}
