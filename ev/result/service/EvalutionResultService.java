package kr.innoaca.usr.web.ev.result.service;

import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.common.utils.*;
import kr.innoaca.usr.web.ev.result.dto.*;
import kr.innoaca.usr.web.ev.result.mapper.EvalutionResultMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @package kr.innoaca.usr.web.ev.result.service
 * @name EvalutionResultService
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/

@Service
@RequiredArgsConstructor
public class EvalutionResultService {
    @Value("${domain-info.ams-url}")
    String domainInfoAmsUrl;
    private static final Logger log = LoggerFactory.getLogger(EvalutionResultService.class);

    private final MessageSource messageSource;

    private final EmailUtil emailUtil;

    private final AwsS3Util awsS3Util;

    private final ApiUtil apiUtil;

    private final EvalutionResultMapper evalutionResultMapper;

    public List<Map<String, Object>>  selectStusList(String grupCd) {

        String language = "en".equals( LocaleContextHolder.getLocale().toString() ) ? "en" : "ko"; // Locale 객체 가져오기

        return evalutionResultMapper.selectStusList(grupCd, language);
    }


    public ResponseMessage selectEvaResultList(EvalutionResultListDto evalutionResultListDto) {
        ResponseMessage responseMessage = new ResponseMessage();

        // Paging Start
        Criteria criteria = new Criteria();
        criteria.setPageNum(evalutionResultListDto.getPage());
        criteria.setPagePerRows(evalutionResultListDto.getPagePerRows());
        evalutionResultListDto.setCriteria(criteria);

        String language = "en".equals( LocaleContextHolder.getLocale().toString() ) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionResultListDto.setLanguage( language );

        List<EvalutionResultListDto> list = evalutionResultMapper.selectEvaResultList(evalutionResultListDto);
        int allCount = evalutionResultMapper.selectEvaResultListCnt(evalutionResultListDto);

        int total = 0;

        try {
            if (list.size() > 0) {
                total = list.get(0).getTotal();
            }
        } catch (RuntimeException e) {
            total = list.size();
        }

        Paginator paginator = new Paginator(criteria, total);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("paginator", paginator);
        resultMap.put("allCount", allCount);

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가결과 상세 조회
     *
     * @param evalutionResultDetailDto
     * @return EvalutionResultDetailDto
     */
    public EvalutionResultDetailDto selectEvalutionResultDetail(EvalutionResultDetailDto evalutionResultDetailDto) {
        return evalutionResultMapper.selectEvalutionResultDetail(evalutionResultDetailDto);
    }

    /**
     * 평가결과 상세 하위 프로젝트 목록 조회
     *
     * @param evalutionResultDetailDto
     * @return EvalutionResultProjectDto
     */
    public List<EvalutionResultProjectDto> selectEvalutionResultProjectList(EvalutionResultDetailDto evalutionResultDetailDto) {
        return evalutionResultMapper.selectEvalutionResultProjectList(evalutionResultDetailDto);
    }

    /**
     * 평가결과 상세 프로젝트 학습과정 목록 조회
     *
     * @param evalutionResultProjectDto
     * @return EvalutionResultLcorsDto
     */
    public List<EvalutionResultLcorsDto> selectEvalutionResultLcorsList(EvalutionResultProjectDto evalutionResultProjectDto) {
        return evalutionResultMapper.selectEvalutionResultLcorsList(evalutionResultProjectDto);
    }

    /**
     * 평가결과 상세 프로젝트 학습단위문제 목록 조회
     *
     * @param evalutionResultLcorsDto
     * @return EvalutionResultUqstnDto
     */
    public List<EvalutionResultUqstnDto> selectEvalutionResultUqstnList(EvalutionResultLcorsDto evalutionResultLcorsDto) {
        return evalutionResultMapper.selectEvalutionResultUqstnList(evalutionResultLcorsDto);
    }

    /**
     * 평가결과 상세 프로젝트 학습단위문제 개별 조회
     * @param evalutionResultUqstnDto
     * @return EvalutionResultUqstnDto
     */
    public EvalutionResultUqstnDto selectEvaluationResultUqstn(EvalutionResultUqstnDto evalutionResultUqstnDto) {
        return evalutionResultMapper.selectEvalutionResultUqstn(evalutionResultUqstnDto);
    }

    /**
     * 평가결과 상세 단위문제 각각의 자동평가자료 목록 조회
     *
     * @param evalutionResultUqstnDto
     * @return EvalutionResultAutoEvlListDto
     */
    public List<EvalutionResultAutoEvlListDto> selectUqstnNoAutoEvlList(EvalutionResultUqstnDto evalutionResultUqstnDto) {
        String language = "en".equals( LocaleContextHolder.getLocale().toString() ) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionResultUqstnDto.setLanguage( language );
        return evalutionResultMapper.selectUqstnNoAutoEvlList(evalutionResultUqstnDto);
    }

    /**
     * 평가결과 상세 단위문제 각각의 상호평가(전문가, 동료) 목록 조회
     *
     * @param evalutionResultUqstnDto
     * @return EvalutionResultAutoEvlListDto
     */
    public List<EvalutionResultMtlEvlListDto> selectUqstnNoMtlEvlList(EvalutionResultUqstnDto evalutionResultUqstnDto) {
        String language = "en".equals( LocaleContextHolder.getLocale().toString() ) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionResultUqstnDto.setLanguage( language );
        return evalutionResultMapper.selectUqstnNoMtlEvlList(evalutionResultUqstnDto);
    }

    /**
     * 평가결과 상세 학습과정번호의 상호평가 단위의 역량평가 내역 목록 조회
     *
     * @param evalutionResultMtlEvlListDto
     * @return EvalutionCpblyEvlTxnListDto
     */
    public List<EvalutionCpblyEvlTxnListDto> selectNullCpblyEvlTxnList(EvalutionResultMtlEvlListDto evalutionResultMtlEvlListDto) {
        return evalutionResultMapper.selectNullCpblyEvlTxnList(evalutionResultMtlEvlListDto);
    }

    /**
     * 평가결과 상세 학습과정번호의 각 단위문제번호의 상호평가  단위의 역량평가 내역 목록 조회
     *
     * @param evalutionResultMtlEvlListDto
     * @return EvalutionCpblyEvlTxnListDto
     */
    public List<EvalutionCpblyEvlTxnListDto> selectCpblyEvlTxnList(EvalutionResultMtlEvlListDto evalutionResultMtlEvlListDto) {
        return evalutionResultMapper.selectCpblyEvlTxnList(evalutionResultMtlEvlListDto);
    }


}
