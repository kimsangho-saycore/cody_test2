package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name MtlEvlTxnDto
 * @date 2023-10-18
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-18        zeroj0706       최초 생성
 **/

@Data
public class MtlEvlTxnDto {
    /* 상호평가일련번호 */
    private Long mtlEvlSn;
    /* 등록자ID */
    private String rgtrId;
    /* 등록일시 */
    private String regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private String mdfcnDt;
    /* 상호평가예정시작일시 */
    private String mtlEvlPamBgngDt;
    private String mtlEvlPamBgngDtTxt;
    /* 상호평가예정종료일시 */
    private String mtlEvlPamEndDt;
    /* 평가자유형코드 */
    private String evlrTypeCd;
    /* 평가자유형코드명 */
    private String evlrTypeNm;
    /* 상호평가상태코드 */
    private String mtlEvlStusCd;
    /* 상호평가상태코드명 */
    private String mtlEvlStusNm;
    /* 반려사유내용 */
    private String rjctRsnCn;
    /* 평가요청취소사유코드 */
    private String evlDmndRtrcnRsnCd;
    /* 평가요청취소사유코드명 */
    private String evlDmndRtrcnRsnNm;
    /* 상호평가시작일시 */
    private String mtlEvlBgngDt;
    /* 상호평가종료일시 */
    private String mtlEvlEndDt;
    /* 상호평가결과코드 */
    private String mtlEvlResltCd;
    /* 상호평가점수 */
    private String mtlEvlScr;
    /* 상호평가추가점수 */
    private String mtlEvlAdtnlScr;
    /* 상호평가추가점수제공여부 */
    private String mtlEvlAdtnlScrPvsnYn;
    /* 경험정도코드 */
    private String exprcDgrCd;
    /* 평가번호 */
    private Integer evlNo;
    /* 평가차수 */
    private Long evlDegr;
    /* 평가회원기관코드 */
    private String evlInstCd;
    /* 평가회원ID */
    private String evlMbrId;
    /* 멘토일정일련번호 */
//	private Long mtrScdlSn;
    /* 멘티요청일련번호 */
    private Long mteDmndSn;
    /* 평가자명 */
    private String evlMbrNm;
    /* 프로젝트명 */
    private String projectNm;
    /* 학습과정명 */
    private String lcorsNoNm;
    /* 단위문제명 */
    private String uqstnNoNm;
    /* 상호평가결과코드(PASS, FAIL) */
    private String mtlEvlResltNm;
    private String evLang;
    /* 평가자email */
    private String evlMbrEmail;
    /* 평가 피드백 내용 */
    private String evlFdbkCn;
    /* 평가 피드백 작성 일시 */
    private String evlFdbkWrtDt;
    /* #351 동료평가 규칙 확인 시각(평가건당 최초 확인) */
    private String ruleCnfrmDttm;
    /* 평가자 프로필 경로 */
    private String profImgPath;
}
