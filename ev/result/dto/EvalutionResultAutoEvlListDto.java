package kr.innoaca.usr.web.ev.result.dto;
import lombok.Data;
/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultAutoEvlListDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 자동평가 내역 리스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/
@Data
public class EvalutionResultAutoEvlListDto {

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
    /** 자동평가일련번호 **/
    private Integer autoEvlSn;
    /** 자동평가결과 */
    private String autoEvlResult;
    /** 자동평가시작일시 */
    private String autoEvlBgngDt;
    /** 자동평가종료일시 */
    private String autoEvlEndDt;
    /** 자동평가상태코드 */
    private String autoEvlStusCd;
    /** 자동평가상태코드명 */
   private String autoEvlStusCdNm;
    /** 자동평가결과코드 */
    private String autoEvlResltCd;
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
    /** 상호평가일련번호 */
    private Integer mtlEvlSn;

}
