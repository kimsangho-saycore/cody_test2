package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ChgSrccUrlAddrDto
 * @date 2023-10-31
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-31        zeroj0706       최초 생성
 **/
@Data
public class ChgSrccUrlAddrDto {
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;
    /* 평가자ID */
    private String evlMbrId;
    /* 자료등록번호 */
    private String dataRegSn;
    /* 소스코드URL주소 */
    private String srccdUrlAddr;
    /* 바로 전 자료등록번호 */
    private String dataRegSnPrev;
    /* 바로 전 소스코드URL주소 */
    private String srccdUrlAddrPrev;
    private String evLang;
}
