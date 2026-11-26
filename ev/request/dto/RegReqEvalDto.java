package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name RegReqEvalDto
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
public class RegReqEvalDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 프로젝트이름 */
    private String projectNm;
    /* 시스템구분코드 */
    private String sysDivCd;
    /* 평가상태코드 */
    private String evlStusCd;
    /* 평가결과코드 */
    private String evlResltCd;
    /* 접속 회원 아이디 */
    private String mbrId;
    /* 회원기관코드 */
    private String mbrInstCd;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;
    /* 멘토평가일시 */
    private String mtrEvlDtm;
    /* 평가가능일자 */
    private String evlPsblYmd;
    /* 평가가능시간 */
    private String evlPsblTm;
    /* 상호평가예정시작일시 */
    private String mtlEvlPamBgngDt;
    /* 상호평가예정종료일시 */
    private String mtlEvlPamEndDt;
    /* '00001' -- 동료 -- 평가자유형코드 */
    private String evlrTypeCd;
    /* '00002' -- 평가대기중 -- 상호평가상태코드 */
    private String mtlEvlStusCd;
    /* 평가번호 Integer */
    private Integer evlNo;
    /* 평가차수 Long */
    private Long evlDegr;
    /* 평가회원기관코드 */
    private String evlInstCd;
    /* 평가회원아이디 */
    private String evlMbrId;
    /* 상호평가 일련번호 */
    private Long mtlEvlSn;
    /* 멘토일정일련번호 */
    private Long mtrScdlSn;
    /* 멘티요청일련번호 */
    private Long mteDmndSn;
    /* 수정일시 : INSERT 후 특정 ROW FETCH에 사용 */
    private String mdfcnDt;
    /* 평가일정 일련번호 */
    private List<Long> itemSnList;
    /* return code 담을 String */
    private String rtnStr;
    /* 멘토링 api 호출 후 return 받은 멘티요청일련번호*/
    private String dmndSn;
    /* 멘토일정상태코드 */
    private String mtrScdlStusCd;
//	instCd : 평가회원기관코드 => evlMbrInstCd
//	mbrId  : 평가자 => evlMbrId
//	rgtrId : 학습자 => mbrId
//	pstartTitlNm : 게시물제목명
//	pstartCn : 게시물내용
//	ntcDivCd : 알림구분코드 => 00008

    /* 게시물제목명 */
    private String pstartTitlNm;
    /* 게시물내용 */
    private String pstartCn;
    /* 알림구분코드 */
    private String ntcDivCd;
    /* 게시물일련번호 */
    private Long pstartSn;
    /* 학습과정명 */
    private String lcorsNoNm;
    /* 단위문제명 */
    private String uqstnNoNm;
    /* 평가가능일정기본 일련번호 */
    private String evlScdlSn;

    private String evLang;

    private String language;	/** 다국어 */
}
