package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

import java.util.Date;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name AutoEvlTxnDto
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
public class AutoEvlTxnDto {
    /* 자동평가일련번호 */
    private Long   autoEvlSn;
    /* 자동평가시작일시 */
    private String autoEvlBgngDt;
    /* 자동평가종료일시 */
    private String autoEvlEndDt;
    /* 자동평가상태코드 */
    private String autoEvlStusCd;
    /* 자동평가결과코드 */
    private String autoEvlResltCd;
    /* 소나큐브결과코드 */
    private String sonarResltCd;
    /* 소나큐브결과코드명 */
    private String sonarResltNm;
    /* 버그수 */
    private Long   bugCnt;
    /* 버그Blocker수 */
    private Long   bugBlckrCnt;
    /* 버그Crital수 */
    private Long   bugCrtclCnt;
    /* 버그Major수 */
    private Long   bugMajrCnt;
    /* 버그Minor수 */
    private Long   bugMinrCnt;
    /* 버그정보수 */
    private Long   bugInfoCnt;
    /* 테스트커버리지비율 */
    private Float  tstCvrgRate;
    /* 코드유사도비율 */
    private Float  cdSmlrtRate;
    /* 소나큐브결과URL주소 */
    private String sonarResltUrlAddr;
    /* 빌드배포결과코드 */
    private String bldDstrResltCd;
    /* 빌드배포결과코드명 */
    private String bldDstrResltNm;
    /* 빌드배포결과URL주소 */
    private String bldDstrResltUrlAddr;
    /* 실행결과코드 */
    private String excnResltCd;
    /* 실행결과코드명 */
    private String excnResltNm;
    /* 실행결과내용 */
    private String excnResltCn;
    /* 서비스URL주소 */
    private String svcUrlAddr;
    /* 시스템오류상태코드 */
    private String sysErrStusCd;
    /* 상호평가일련번호 */
    private Long   mtlEvlSn;
    /* 평가번호 */
    private Integer evlNo;
    /* 평가차수 */
    private String evlDegr;
    /* 자동평가 api 호출 결과 코드 */
    private String returnCode;
    private String evLang;
    /* 등록자아이디 */
    private String rgtrId;
    /* 등록일시 */
    private Date regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private Date mdfcnDt;
}
