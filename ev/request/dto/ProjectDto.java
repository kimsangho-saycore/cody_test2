package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ProjectDto
 * @date 2023-10-30
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-30        zeroj0706       최초 생성
 **/

@Data
public class ProjectDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 프로젝트번호_학습차수 */
    private String projectNoStr;
    /* 등록자아이디 */
    private String rgtrId;
    /* 등록일시 */
    private String regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private String mdfcnDt;
    /* 프로젝트명 */
    private String projectNm;
    /* 기관코드 */
    private String instCd;
    /* 시작일자 */
    private String bgngYmd;
    /* 종료일자 */
    private String endYmd;
    /* 학습과정수 */
    private String lcorsCnt;
    /* 팀수 */
    private String teamCnt;
    /* 최대팀원인원수 */
    private String maxTmbrNope;
    /* 최소팀원인원수 */
    private String minTmbrNope;
    /* 사용여부 */
    private String useYn;
    /* 프로젝트파일일련번호 */
    private String pjtFileSn;
    /* 프로젝트파일설명 */
    private String pjtFileDesc;
    /* 프로젝트정의파일일련번호 */
    private String pjtDefFileSn;
    /* 프로젝트역량정의파일일련번호 */
    private String pjtCpblyDefFileSn;
    /* 프로젝트기술파일일련번호 */
    private String pjtTechFileSn;
    /* 프로젝트평가가이드파일일련번호 */
    private String pjtEvlGuideFileSn;
    /* 학습과정구분코드 */
    private String lcorsDivCd;
    /* 학습과정분야코드 */
    private String lcorsFildCd;
    /* 선발과정적용여부 */
    private String rcrtAplyYn;
    /* 배포여부 */
    private String distrbYn;
    /* 개인 회원 ID */
    private String mbrId;
    /* 회차붙일때 사용 */
    private String cntNo;
}
