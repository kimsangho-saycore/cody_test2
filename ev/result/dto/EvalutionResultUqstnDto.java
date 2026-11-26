package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EvalutionResultUqstnDto{

    /** 멤버 ID **/
    private String mbrId;
    /** 기관코드 **/
    private String instCd;
    /** 프로젝트번호 **/
    private Integer projectNo;
    /** 학습과정번호 **/
    private Integer lcorsNo;
    /** 단위문제번호 **/
    private Integer uqstnNo;
    /** 단위문제명 **/
    private String uqstnNm;
    /** 평가결과 */
    private String evlResultYn;
    /** 팀 일련번호 **/
    private Integer teamSn;
    /** 학습과정 단위문제 각각에 자동평가자료 목록 */
    private List<EvalutionResultAutoEvlListDto> evalutionResultAutoEvl = new ArrayList<>();

    /** 학습과정 단위문제 각각에 상호평가, 자동평가자료 목록  */
    private List<EvalutionResultMtlEvlListDto> evalutionResultMtlEvl = new ArrayList<>();

    private String language;	/** 다국어 */
}
