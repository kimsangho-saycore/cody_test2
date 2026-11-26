
package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;
/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionCpblyEvlTxnListDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평역량평가 내역 리스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/
@Data
public class EvalutionCpblyEvlTxnListDto {

    /** 단위문제역량번호 **/
    private Integer uqstnCpblyNo;
    /** 역량명 **/
    private String cpblyNm;
    /** 역량기준점수 **/
    private Integer cpblyBaseScr;
    /** 역량점수 **/
    private String cpblyScr;
    /** 상호평가일련번호 **/
    private Integer mtlEvlSn;
}

