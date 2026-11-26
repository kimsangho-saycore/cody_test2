package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가결과
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/

@Data
public class EvalutionResultDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /** 프로젝트 명 **/
    private String projectNm;
    /** 평가여부 */
    private String evlYn;
    /** 학습진행상태코드 */
    private String lrnResultCd;
    /** 학습진행상태명 */
    private String lrnResultNm;
    /** 평가결과 */
    private String evlResultYn;
    /** 평가점수 */
    private String evlResultScore;
    /* 시작일자 */
    private String lrnBgngYmd;
    /* 종료일자 */
    private String lrnEndYmd;
    /* 평가상태태코드 */
    private String evlStusCd;

}
