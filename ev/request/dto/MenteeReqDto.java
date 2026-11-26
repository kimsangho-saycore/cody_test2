package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name MenteeReqDto
 * @date 2023-11-06
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-06        zeroj0706       최초 생성
 **/

@Data
public class MenteeReqDto {
    /* 멘토회원기관코드 */
    private String mtrMbrInstCd;
    /* 멘토회원아이디 */
    private String mtrMbrId;
    /* 일정일련번호 */
    private String scdlSn;
    /* 멘티(학습자)회원기관코드 */
    private String mteMbrInstCd;
    /* 멘티(학습자)회원아이디 */
    private String mteMbrId;
    /* 학습과정명-단위문제명 */
    private String mtDmndCn;
    /* 시작일자 */
    private String bgngYmd;
    /* 시작일시 */
    private String bgngDt;
    /* 종료일자 */
    private String endYmd;
    /* 종료일시 */
    private String endDt;
    /* 일정 */
    private String schedule;
    /* 상태코드 */
    private String stusCd;
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 프로젝트명 */
    private Integer projectNm;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;

    private List<MenteeReqDto> list;

    private int dmndSn;
    /* 학습과정명 */
    private String lcorsNoNm;
    /* 단위문제명 */
    private String uqstnNoNm;
    /* 멘토역할코드 */
    private String mtrRoleCd;
    /* 멘티역할코드 */
    private String mteRoleCd;

}
