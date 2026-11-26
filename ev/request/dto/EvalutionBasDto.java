package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvalutionBasDto
 * @date 2023-10-18
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-18        zeroj0706       최초 생성
 **/

@Data
public class EvalutionBasDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 평가번호 */
    private Integer evlNo;
    /* 평가번호 */
    private Long evlDegr;
    /* 등록자아이디 */
    private String rgtrId;
    /* 등록일시 */
    private String regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private String mdfcnDt;
    /* 평가시작일시 */
    private String evlBgngDt;
    /* 평가종료일시 */
    private String evlEndDt;
    /* 평가상태코드 */
    private String evlStusCd;
    /* 평가상태코드명 */
    private String evlStusNm;
    /* 평가결과코드 */
    private String evlResltCd;
    /* 평가결과코드명 */
    private String evlResltNm;
    /* 평가점수 */
    private Integer evlScr;
    /* 경험점수 */
    private Integer exprcScr;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;
    /* 자료등록일련번호 */
    private Long dataRegSn;
    /* 개인 회원 ID : 등록자id, 수정자id */
    private String mbrId;
    /* 학습회원 기관코드 */
    private String instCd;

    private String evLang;

    private String language;	/** 다국어 */
}
