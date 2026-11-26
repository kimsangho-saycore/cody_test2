package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name LrnCorsNmUqstnNmDto
 * @date 2023-10-23
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-23        zeroj0706       최초 생성
 **/

@Data
public class LrnCorsNmUqstnNmDto {
    /** 개인 회원 ID */
    private String mbrId;
    /** 학습회원기관코드 */
    private String instCd;
    /** 프로젝트번호 */
    private Integer projectNo;
    /** 프로젝트명 */
    private String projectNm;
    /** 프로젝트 하위번호 */
    private Integer posProjectNo;
    /** 학습과정번호 */
    private Integer lcorsNo;
    /** 학습과정번호_학습차수 */
    private String lcorsNoStr;
    /** 학습과정명 */
    private String lrnCorsNm;
    /** 단위문제번호 */
    private Long uqstnNo;
    /** 단위문제명 */
    private String uqstnNm;
    /** 팀일련번호 */
    private String teamSn;
    /** 자동평가여부 */
    private String autoEvlYn;
    /** 자동평가여부 */
    private String dockerEvlYn;
    /** 동료평가여부 */
    private String collEvlYn;
    /** 전문가평가여부 */
    private String profEvlYn;
    /** 시험평가여부 */
    private String testEvlYn;
    /** 동료평가수 */
    private Integer peerEvlrNope;
    /** 전문가평가수 */
    private Integer spcltEvlrNope;
    /** 시험평가자수 **/
    private Integer testEvlrNope;
    /** 회차붙일때 사용 */
    private String cntNo;
    /** 학습차수 */
    private Long lrnTmcnt;
    /** 과정평가 한글 사용 */
    private String kwajungStr;
    /** 프로젝트평가 한글 사용 */
    private String projEvlStr;
    /** 학습과정수 */
    private String lcorsCnt;
    /** 프로그래밍코드 */
    private String prlngCd;
    /** 프로그래밍언어명 */
    private String prlngNm;
    private String evLang;

    private String language;	/** 다국어 */


    /** 평가 선택/필수 */
    private String evlMandatoryYn;
}
