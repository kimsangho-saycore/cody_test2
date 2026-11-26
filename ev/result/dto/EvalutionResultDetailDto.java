package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultDetailDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가결과 상세 정보
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/

@Data
public class EvalutionResultDetailDto {
    /** 멤버 ID **/
    private String mbrId;
    /** 기관코드 **/
    private String instCd;
    /** 프로젝트번호 **/
    private Integer projectNo;
    /** 프로젝트 명 **/
    private String projectNm;
    /** 프로젝트 시작기간 */
    private String bgngYmd;
    /** 프로젝트 종료기간 */
    private String endYmd;
    /** 평가결과 */
    private String evlResultYn;
    /** 평가점수 */
    private String evlResultScore;
    /** 학습자 */
    private String lrnrNm;
    /** 팀 일련번호 **/
    private Integer teamSn;
    /** 평가결과 프로젝트 리스트 */
    private List<EvalutionResultProjectDto> evalutionResultProjectList = new ArrayList<>();


}
