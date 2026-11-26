package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name NtcBbsBasDto
 * @date 2023-11-07
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-07        zeroj0706       최초 생성
 **/

@Data
public class NtcBbsBasDto {
    /* 기관코드 */
    private String instCd;
    /* 회원아이디 */
    private String mbrId;
    /* 게시물일련번호 */
    private int pstartSn = 0;
    /* 등록자아이디 */
    private String rgtrId;
    /* 등록일시 */
    private String regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private String mdfcnDt;
    /* 게시물제목명 */
    private String pstartTitlNm;
    /* 게시물내용 */
    private String pstartCn;
    /* 알림구분코드 */
    private String ntcDivCd;
    /* 시스템구분코드 */
    private String sysDivCd;
    /* 호출게시물일련번호 */
    private int callPstartSn = 0;
}
