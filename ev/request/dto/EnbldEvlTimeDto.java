package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EnbldEvlTimeDto
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
public class EnbldEvlTimeDto {
    /* 평가일정일련번호 */
    private Long evlScdlSn;
    /* 화면에 보여줄 색 class 구분 */
    private String color;
    /* 회원기관코드 */
    private String instCd;
    /* 회원ID */
    private String mbrId;
    /* 피드백_학습회원 */
    private String mbrNm;
    /* 평가가능일자 */
    private String evlPsblYmd;
    /* 평가가능시간 */
    private String evlPsblTm;
    /* 평가가능 일시 */
    private String evlPsblYmdTm;
    /* 학습번호 */
    private String lcorsNo;
    /* 조회 시작일자(월요일) */
    private String monDay;
    /* 조회 종료일자(일요일) */
    private String sunDay;
    /* 시간갯수 (30분) */
    private Integer cn;
    /* 평가가능일자에 붙여서 사용 */
    private String evlPsblYmdEnd;
    /* 평가가능시간에 붙여서 사용 */
    private String evlPsblTmEnd;
    /* 팀 일련 번호 */
    private Long teamSn;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 프로젝트번호 */
    private Integer projectNo;
    private String evLang;
    /*회원ID 리스트*/
    private List<String> mbrIds = new ArrayList<String>();
}
