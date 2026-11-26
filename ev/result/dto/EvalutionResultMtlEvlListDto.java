package kr.innoaca.usr.web.ev.result.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultMtlEvlListDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가 결과 상호평가(전문가, 동료) 내역 목록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/
@Data
public class EvalutionResultMtlEvlListDto {

    /** 평가번호 **/
    private Integer evlNo;
    /** 평가차수 **/
    private Integer evlDegr;
    /** 평가시작일시 **/
    private String evlBgngDt;
    /** 학습과정일련번호 **/
    private Integer lcorsNo;
    /** 단위문제번호 **/
    private Integer uqstnNo;
    /** 팀 일련번호 **/
    private Integer teamSn;
    /** 상호평가일련번호 **/
    private Integer mtlEvlSn;
    /** 상호평가결과코드명 **/
    private String mtlEvlResultCd;
    /** 상호평가예정시작일시 **/
    private String mtlEvlPamBgngDt;
    /** 상호평가예정종료일시 **/
    private String mtlEvlPamEndDt;
    /** 평가자유형코드 **/
    private String evlrTypeCd;
    /** 평가자유형코드 **/
    private String mtlEvlStusCd;
    /** 반려사유내용 **/
    private String rjctRsnCn;
    /**평가요청취소사유코드**/
    private String evlDmndRtrcnRsnCd;
    /**상호평가시작일시**/
    private String mtlEvlBgngDt;
    /**상호평가종료일시**/
    private String mtlEvlEndDt;
    /**상호평가결과코드**/
    private String mtlEvlResltCd;
    /**상호평가점수**/
    private Integer mtlEvlScr;
    /**상호평가추가점수**/
    private Integer mtlEvlAdtnlScr;
    /**상호평가추가점수제공여부**/
    private String mtlEvlAdtnlScrPvsnYn;
    /**경험정도코드**/
    private String exprcDgrCd;
    /**평가기관코드**/
    private String evlInstCd;
    /**평가회원아이디**/
    private String evlMbrId;
    /**자동평가결과**/
    private String autoEvlResult;
    /**자동평가시작일시**/
    private String autoEvlBgngDt;
    /**자동평가종료일시**/
    private String autoEvlEndDt;
    /**자동평가종료일시**/
    private String autoEvlStusCd;
    /**자동평가종료일시**/
    private String autoEvlResltCd;
    /** 자동평가일련번호 **/
    private Integer autoEvlSn;
    /** 소나큐브결과코드 */
    private String sonarResltCd;
    /** 소나큐브결과코드명 */
    private String sonarResltCdNm;
    /** 버그수 */
    private Integer bugCnt;
    /** 버그Blocker수 */
    private Integer bugBlckrCnt;
    /** 자동평가상태코드 */
    private Integer bugCrtclCnt;
    /** 버그Major수 */
    private Integer bugMajrCnt;
    /** 버그Minor수 */
    private Integer bugMinrCnt;
    /** 버그정보수 */
    private Integer bugInfoCnt;
    /** 테스트커버리지비율 */
    private Integer tstCvrgRate;
    /** 코드유사도비율 */
    private Integer cdSmlrtRate;
    /** 소나큐브결과URL주소 */
    private String sonarResltUrlAddr;
    /** 빌드배포결과코드 */
    private String bldDstrResltCd;
    /** 빌드배포결과URL주소 */
    private String bldDstrResltUrlAddr;
    /** 배포결과코드명 */
    private String bldDstrResltCdNm;
    /** 실행결과코드 */
    private String excnResltCd;
    /** 실행결과내용 */
    private String excnResltCn;
    /** 실행결과코드명 */
    private String excnResltCdNm;
    /** 서비스URL주소 */
    private String svcUrlAddr;
    /** 시스템오류상태코드 */
    private String sysErrStusCd;
    /** 평가자유형명 */
    private String evlrTypeCdNm;
    /** 상호평가결과 */
    private String mtlEvlResltCdNm;
    /** 평가피드백일련번호 */
    private Integer evlFdbkSn;
    /** 평가피드백작성일시 */
    private String evlFdbkWrtDt;
    /** 평가피드백내용 */
    private String evlFdbkCn;
    /** 댓글피드백점수 */
    private String evlFdbkScr;
    /** 댓글피드백작성일시 */
    private String cmntFdbkWrtDt;
    /** 댓글피드백작성일시 */
    private String cmntFdbkCn;
    /** 댓글피드백내용 */
    private String cmntFdbkScr;
    /** 피드백_평가회원명 */
    private String evlMbr;
    /** 피드백_학습회원명 */
    private String lrnMbr;
    /** 피드백_학습회아이디 */
    private String lrnMbrId;

    /** 학습과정 프로젝트 리스트 */
    private List<EvalutionCpblyEvlTxnListDto> cpblyEvlTxnList  = new ArrayList<>();;





}
