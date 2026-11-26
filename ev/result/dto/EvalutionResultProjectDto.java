package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultProjectDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가 결과 프로젝트 및 하위프로젝트 목록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/

@Data
public class EvalutionResultProjectDto{
    /** 멤버 ID **/
    private String mbrId;
    /** 기관코드 **/
    private String instCd;
    /** 프로젝트번호 **/
    private Integer projectNo;
    /** 프로젝트 명 **/
    private String projectNm;
    /** 평가결과 */
    private String evlResultYn;
    /** 팀 일련번호 **/
    private Integer teamSn;
    /** 학습과정 프로젝트 리스트 */
    private List<EvalutionResultLcorsDto> evalutionResultLcors  = new ArrayList<>();;



}
