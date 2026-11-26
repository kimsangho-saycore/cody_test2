package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name CrtrScrDto
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
public class CrtrScrDto {
    /* 기준점수 */
    private Float crtrScr;
    /* 'P' - POINT 	'E' - EXPERIENCE */
    private String crtrTypeCd;
    /* 기준유형번호 */
    private Long crtrTypeNo;
    /* 기준사용여부 */
    private String crtrUseYn;
    private String evLang;
}
