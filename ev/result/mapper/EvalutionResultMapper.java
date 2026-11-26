package kr.innoaca.usr.web.ev.result.mapper;

import kr.innoaca.usr.web.ev.request.dto.*;
import kr.innoaca.usr.web.ev.result.dto.*;
import kr.innoaca.usr.web.mt.dto.MenteeMentoringListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @package kr.innoaca.usr.web.ev.result.mapper
 * @name EvalutionResultMapper
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/

@Mapper
public interface EvalutionResultMapper {

    /**
     * 셀렉트 박스 공통코드 조회
     *
     * @param grupCd
     * @return String
     */
    List<Map<String, Object>> selectStusList(@Param("grupCd") String grupCd, @Param("language") String language);

    /**
     *
     * 메소드명 : selectEvaResultListCnt
     * 설명       : 평가 결과 목록 카운트
     */
    int selectEvaResultListCnt(EvalutionResultListDto evalutionResultListDto);

    /**
     *
     * 메소드명 : selectEvaResultList
     * 설명       : 평가 결과 목록 조회
     */
    List<EvalutionResultListDto> selectEvaResultList(EvalutionResultListDto evalutionResultListDto);

    /**
     * 셀렉트 박스 공통코드 조회
     *
     * @param evalutionResultDetailDto
     * @return EvalutionResultDetailDto
     */
    EvalutionResultDetailDto selectEvalutionResultDetail(EvalutionResultDetailDto evalutionResultDetailDto);

    /**
     * 평가결과 상세 하위 프로젝트 목록 조회
     *
     * @param evalutionResultDetailDto
     * @return EvalutionResultDetailDto
     */
    List<EvalutionResultProjectDto> selectEvalutionResultProjectList(EvalutionResultDetailDto evalutionResultDetailDto);

    /**
     * 평가결과 상세 프로젝트 학습과정 목록 조회
     *
     * @param evalutionResultProjectDto
     * @return EvalutionResultProjectDto
     */
    List<EvalutionResultLcorsDto> selectEvalutionResultLcorsList(EvalutionResultProjectDto evalutionResultProjectDto);

    /**
     * 평가결과 상세 프로젝트 학습단위문제 목록 조회
     *
     * @param evalutionResultLcorsDto
     * @return EvalutionResultUqstnDto
     */
    List<EvalutionResultUqstnDto> selectEvalutionResultUqstnList(EvalutionResultLcorsDto evalutionResultLcorsDto);

    /**
     * 평가결과 상세 프로젝트 학습단위문제 개별 조회
     * @param evalutionResultUqstnDto
     * @return EvalutionResultUqstnDto
     */
    EvalutionResultUqstnDto selectEvalutionResultUqstn(EvalutionResultUqstnDto evalutionResultUqstnDto);

    /**
     * 평가결과 상세 단위문제 각각의 자동평가자료 목록 조회
     *
     * @param evalutionResultUqstnDto
     * @return EvalutionResultAutoEvlListDto
     */
    List<EvalutionResultAutoEvlListDto> selectUqstnNoAutoEvlList(EvalutionResultUqstnDto evalutionResultUqstnDto);

    /**
     * 평가결과 상세 단위문제 각각의 상호평가(전문가, 동료) 목록 조회
     *
     * @param evalutionResultUqstnDto
     * @return EvalutionResultMtlEvlListDto
     */
    List<EvalutionResultMtlEvlListDto> selectUqstnNoMtlEvlList(EvalutionResultUqstnDto evalutionResultUqstnDto);

    /**
     * 평가결과 상세 학습과정번호의 상호평가 단위의 역량평가 내역 목록 조회
     *
     * @param evalutionResultMtlEvlListDto
     * @return EvalutionCpblyEvlTxnListDto
     */
    List<EvalutionCpblyEvlTxnListDto> selectNullCpblyEvlTxnList(EvalutionResultMtlEvlListDto evalutionResultMtlEvlListDto);

    /**
     * 평가결과 상세 학습과정번호의 각 단위문제번호의 상호평가  단위의 역량평가 내역 목록 조회
     *
     * @param evalutionResultMtlEvlListDto
     * @return EvalutionCpblyEvlTxnListDto
     */
    List<EvalutionCpblyEvlTxnListDto> selectCpblyEvlTxnList(EvalutionResultMtlEvlListDto evalutionResultMtlEvlListDto);



}
