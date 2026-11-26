package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvlutionStdDto
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
public class EvalutionStdDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 학습과정번호 */
    private Long lcorsNo;
    /* 문제번호 */
    private Long uqstnNo;
    /* 평가자유형 00002:동료, 00003:전문가 */
    private String evlTypeCd;
    /* 평가자수 */
    private Integer evlrNope;
    /* 평가수행시간 */
    private String evlImplHr;
    /* 동료평가자대상범위코드 */
    private String evlrTrgtScopeCd;
    /* 동료평가자대상범위코드명 전문가는 해당 없음 */
    private String evlrTrgtScope;
    /* 평가자지정방식코드 */
    private String rndmDsgnCd;
    /* 평가자지정방식명 */
    private String rndmDsgnNm;
    /* DB의 현재일자 */
    private String evlPsblYmd;
    /* 현재일자기준 이번주 월요일 일자 */
    private String monDay;
    /* 현재일자기준 이번주 일요일 일자 */
    private String sunDay;
    private String notaplcbl;
    private String evLang;
    private String language;	/** 다국어 */
    /* 단위문제 팀구성방식 (PJT_LCORS_UQSTN.TEAM_COMPOSITION). 00000:개인, 00001:랜덤, 00002:초대 */
    private String teamComposition;
}
