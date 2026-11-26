package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultLcorsDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가 결과 학습과정 목록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/

@Data
public class EvalutionResultLcorsDto{

    /** 멤버 ID **/
    private String mbrId;
    /** 기관코드 **/
    private String instCd;
    /** 프로젝트번호 **/
    private Integer projectNo;
    /** 학습과정번호 **/
    private Integer lcorsNo;
    /** 학습과정명 **/
    private String lcorsNm;
    /** 평가결과 */
    private String evlResultYn;
    /** 팀 일련번호 **/
    private Integer teamSn;
    /** 단위 프로젝트 리스트 */
    private List<EvalutionResultUqstnDto> evalutionResultUqstn = new ArrayList<>();
}
