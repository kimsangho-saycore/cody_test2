package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ApiCmmnPoint
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
public class ApiCmmnPoint {
    // INF-CM-103(포인트 총계 정보) 에서 사용
    private String memNames;
    // INF-CM-103(포인트 총계 정보) 에서 사용
    private Float pointScore;

    private String evLang;
    private Float nowPntScr;
}
