package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name MtrEvlScdlBasByMteConDto
 * @date 2023-11-07
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-07        zeroj0706       최초 생성
 **/
@Data
public class MtrEvlScdlBasByMteConDto {
    /* 멘토기관코드 */
    private String mtrInstCd;
    /* 멘토회원id */
    private String mtrMbrId;
    /* 멘토일정일련번호 */
    private Long mtrScdlSn;
    /* 학습자회원기관코드 */
    private String instCd;
    /* 학습자회원아이디 */
    private String mbrId;
    /* 멘토평가예정시작일자 */
    private String mtrEvlBgngYmd;
    /* 멘토평가예정시작시간 */
    private String mtrEvlBgngTm;
    /* 멘토평가예정종료일자 */
    private String mtrEvlEndYmd;
    /* 멘토평가예정종료시간 */
    private String mtrEvlEndTm;
    /* 상호평가일련번호 */
    private Long mtlEvlSn;
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 팀일련번호 */
    private Long teamSn;
    private String evLang;
}
