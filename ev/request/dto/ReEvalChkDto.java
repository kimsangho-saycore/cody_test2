package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ReEvalChkDto
 * @date 2023-10-24
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-24        zeroj0706       최초 생성
 **/
@Data
public class ReEvalChkDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;
    /* 조회구분 1 : 현재일자 기준으로 조회, 2 : 조회기준일자(queryDate, 년월일시분) 기준으로 조회 */
    private String queryGubun;
    /* 조회기준일자 */
    private String queryDate;
    /* 재평가가능여부 */
    private String chkYn;
    /* 재평가가능기간 */
    private Long agnEvlRstrtPrd;
    private String evLang;
}
