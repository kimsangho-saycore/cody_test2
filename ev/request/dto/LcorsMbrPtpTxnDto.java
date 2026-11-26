package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name LcorsMbrPtpTxnDto
 * @date 2023-10-23
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-23        zeroj0706       최초 생성
 **/

@Data
public class LcorsMbrPtpTxnDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 팀일련번호 */
    private Integer teamSn;
    /* 학습회원기관코드 */
    private String lrnMbrInstCd;
    /* 학습회원아이디 */
    private String mbrId;
    /* 학습자명 */
    private String lrnrNm;
    /* 학습과정명 */
    private String lrnCorsNm;
    /* 단위문제수 */
    private Integer uqstnCnt;
    /* 학습횟수 */
    private Long lrnTmcnt;
    private String evLang;
}
