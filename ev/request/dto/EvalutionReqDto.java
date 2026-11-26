package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvalutionDto
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/

@Data
public class EvalutionReqDto {
    /** 평가번호 */
    private String evlNo;
    /** 평가차수 */
    private String evlDegr;
    /** 등록자아이디 */
    private String rgtrId;
    /** 등록일시 */
    private String regDt;
    /** 수정자아이디 */
    private String mdfrId;
    /** 수정일시 */
    private String mdfcnDt;
    /** 평가시작일시 */
    private String evlBgngDt;
    /** 평가종료일시 */
    private String evlEndDt;
    /** 평가상태코드 */
    private String evlStusCd;
    /** 평가결과코드 */
    private String evlResltCd;
    /** 평가점수 */
    private String evlScr;
    /** 경험점수 */
    private String exprcScr;
    /** 학습과정번호 */
    private String lcorsNo;
    /** 단위문제번호 */
    private String uqstnNo;
    /** 팀일련번호 */
    private String teamSn;
    /** 자료등록일련번호 */
    private String dataRegSn;
}
