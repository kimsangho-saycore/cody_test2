package kr.innoaca.usr.web.ev.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvalutionMbrDto
 * @date 2023-10-20
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-20        zeroj0706       최초 생성
 **/

@Data
public class EvalutionMbrDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 학습과정번호 */
    private Long lcorsNo;
    /* 학습회원ID 또는 멘토(전문가)ID (취소API 호출시 사용) */
    private String mbrId;
    /* 피드백_학습회원 */
    private String mbrNm;
    /* 시간갯수 (30분) */
    private Integer cn;
    /* 조회 기준 일자 */
    private String evlPsblYmd;
    /* 회원소속기관코드 또는 멘토(전문가) 소속기관코드 (취소API 호출시 사용) */
    private String instCd;
    /* 평가자 대상범위 코드 */
    private String evlrTrgtScopeCd;
    /* 팀 일련 번호 */
    private Long teamSn;
    /* 전문가 평가자 api 호출용 검색 시작일자 */
    private String bgngDt;
    /* 전문가 평가자 api 호출용 검색 종료일자 */
    private String endDt;
    /* 멘티 일정 취소에 사용 : 요청일련번호 */
    private String dmndSn;
    /* 취소 할 상호평가 일련번호 */
    private Long delMtlEvlSn;
    /* 상호평가상태코드 */
    private String mtlEvlStusCd;
    /* 평가요청취소사유코드 */
    private String evlDmndRtrcnRsnCd;
    /* 학습자id */
    private String lrnMbrId;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 멘토기관코드 */
    private String mtrMbrInstCd;
    /* 멘토회원아이디 */
    private String mtrMbrId;
    /* 멘토이름 */
    private String mtrMbrNm;
    /* 조회 시작일자(월요일) */
    private String monDay;
    /* 조회 종료일자(일요일) */
    private String sunDay;
    /* 일정일련번호 */
    private int scdlSn;
    private String bgngYmd;
    private String bgngTm;
    private String evLang;
    /*멘토ID 리스트*/
    private List<String> mtrMbrIds = new ArrayList<String>();
}
