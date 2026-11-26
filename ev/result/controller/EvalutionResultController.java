package kr.innoaca.usr.web.ev.result.controller;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.usr.web.ev.request.service.EvalutionReqService;
import kr.innoaca.usr.web.ev.result.dto.*;
import kr.innoaca.usr.web.ev.result.service.EvalutionResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @package kr.innoaca.usr.web.ev.result.controller
 * @name EvalutionResultController
 * @date 2023-11-11
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-11        zeroj0706       최초 생성
 **/

@Slf4j
@Controller
@RequestMapping("/ev/result")
@RequiredArgsConstructor
public class EvalutionResultController {
    private final EvalutionResultService evalutionResultService;
    private final MessageSource messageSource;

    /**
     * 평가결과 목록 화면 조회
     *
     * @param model
     * @return String
     */

    @RequestMapping(value = "/evalutionResultList", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionResultList(Model model) {
        String grupCd = "";

        //평가 결과 공통코드
        grupCd = "LP010";
        List<Map<String, Object>> test = evalutionResultService.selectStusList(grupCd);
        model.addAttribute("evaResultStatus",evalutionResultService.selectStusList(grupCd));

        //학습진행상태 결과코드
        grupCd = "LP005";
        model.addAttribute("learnProgresstStatus",evalutionResultService.selectStusList(grupCd));

        return "ev/result/evalutionResultList";
    }

    /**
     * 평가결과 상세 조회
     *
     * @param evalutionResultDetailDto
     * @return String
     */

    @RequestMapping(value = "/evalutionResultDetail", method = {RequestMethod.GET, RequestMethod.POST})
    public String evalutionResultDetail(Model model, EvalutionResultDetailDto evalutionResultDetailDto, @AuthenticationPrincipal Member member) {
        //로그인한 사용자의 회원아이디
        evalutionResultDetailDto.setMbrId(member.getMbrId());
        //로그인한 사용자의 기관코드
        evalutionResultDetailDto.setInstCd(member.getInstCd());

        //프로젝트 상세 정보
        evalutionResultDetailDto = evalutionResultService.selectEvalutionResultDetail(evalutionResultDetailDto);

        if(evalutionResultDetailDto.getProjectNo() != null) {
            //프로젝트 하위 프로젝트 목록 set
            evalutionResultDetailDto.setEvalutionResultProjectList(evalutionResultService.selectEvalutionResultProjectList(evalutionResultDetailDto));

            //프로젝트 하위 프로젝트 목록
            List<EvalutionResultProjectDto> projectList = evalutionResultDetailDto.getEvalutionResultProjectList();

            //프로젝트 상세 학습과정 목록 set
            for (EvalutionResultProjectDto projectInfo : projectList) {
                if (projectInfo.getProjectNo() != null) {
                    projectInfo.setEvalutionResultLcors(evalutionResultService.selectEvalutionResultLcorsList(projectInfo));
                }
            }

            //프로젝트 상세 학습과정 목록
            List<EvalutionResultLcorsDto> lcorsList = new ArrayList<>();
            //프로젝트 상세 단위문제 목록
            List<EvalutionResultUqstnDto> uqstnList = new ArrayList<>();
            //프로젝트 상세 단위문제별 상호평가 내역 목록
            List<EvalutionResultMtlEvlListDto> mtlEvlList = new ArrayList<>();

            for (int i = 0; i < projectList.size(); i++) {
                lcorsList = projectList.get(i).getEvalutionResultLcors();
                for (int j = 0; j < lcorsList.size(); j++) {
                    if (lcorsList.get(j).getLcorsNo() != null) {
                        //프로젝트 상세 학습단위문제 set
                        lcorsList.get(j).setEvalutionResultUqstn(evalutionResultService.selectEvalutionResultUqstnList(lcorsList.get(j)));
                        uqstnList = lcorsList.get(j).getEvalutionResultUqstn();

                        for (int k = 0; k < uqstnList.size(); k++) {
                            if (uqstnList.get(k).getUqstnNo() != null) {
                                //단위문제 각각에 자동평가자료 set
                                uqstnList.get(k).setEvalutionResultAutoEvl(evalutionResultService.selectUqstnNoAutoEvlList(uqstnList.get(k)));

                                //단위문제 각각에 상호평가(전문가, 동료) set
                                uqstnList.get(k).setEvalutionResultMtlEvl(evalutionResultService.selectUqstnNoMtlEvlList(uqstnList.get(k)));

                                mtlEvlList = uqstnList.get(k).getEvalutionResultMtlEvl();
                                for (int l = 0; l < mtlEvlList.size(); l++) {
                                    if (mtlEvlList.get(l).getMtlEvlSn() != null) {
                                        if(mtlEvlList.get(l).getUqstnNo() == 9999999){
                                            //평가결과 상세 학습과정번호의 상호평가 단위의 역량평가 내역  set
                                            mtlEvlList.get(l).setCpblyEvlTxnList(evalutionResultService.selectNullCpblyEvlTxnList(mtlEvlList.get(l)));
                                        }else{
                                            //평가결과 상세 학습과정번호의 각 단위문제번호의 상호평가  단위의 역량평가 내역  set
                                            mtlEvlList.get(l).setCpblyEvlTxnList(evalutionResultService.selectCpblyEvlTxnList(mtlEvlList.get(l)));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        model.addAttribute("detail", evalutionResultDetailDto);

        return "ev/result/evalutionResultDetail";
    }
}
