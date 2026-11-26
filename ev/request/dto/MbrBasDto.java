package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name MbrBasDto
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
public class MbrBasDto {
    private String instCd;
    private String mbrId;
    private String rgtrId;
    private String regDt;
    private String mdfrId;
    private String mdfcnDt;
    private String RecntCntnDt;
    private String useYn;
    private String mbrNm;
    private String instNm;
    private String emlAddr;
    private String sprMngrYn;
    private String roleCd;
    private String glabAcntId;
}
