package kr.innoaca.usr.web.ev.participation.dto;

import kr.innoaca.usr.common.utils.Criteria;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ParticipationDto
 * @date 2023-11-10
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-10        zeroj0706       최초 생성
 **/
@Data
public class ParticipationDto extends ParticipationListDto{
    /** 총 갯수 */
    private int total;

    /** 열 번호  */
    private int rowNumb;

    /** 시작일 */
    private String startDt;

    /** 종료일 */
    private String endDt;

    /** 정렬 */
    private String orderBy;

    /** 현재 페이지 */
    private int page = 1;

    /** 기본 최대 페이지 */
    private int pagePerRows = 10;

    /** 페이징 처리 */
    private Criteria criteria;

    private String projectNm;
    private String lrnCorsNm;
    private String uqstnNm;
    private String teamNm;
    private String mtlEvlPamBgngDt;
    private String mtlEvlBgngDt;
    private String mtlEvlPamBgngDted;
    private String mtlEvlBgngDted;
    private String mtlEvlStusCd;
    private String mtlEvlStusNm;
    private String mtlEvlResltCd;
    private String mtlEvlResltNm;
    private String projectNo;
    private String lcorsNo;
    private String uqstnNo;
    private String teamSn;
    private Integer evlDegr;
    private String mtlEvlSn;
    private String mtlMbrId;
    private String evlMbrId;
    private String evlMbrNm;
    private String evlInstCd;
    private String reqMbrId;
    private String reqMbrNm;
    private String evlStdtFirst;
    private String evlStdtLast;
    private String evlScr;
    private BigDecimal evlNo;
    private String cancelMbrId;
    private String cancelMbrNm;
    private String cancelCn;
    private String mbrId;
    private String evLang;
    private String mtlEvlScr;


    /**
     * 이메일 주소
     */
    private String emlAddr;

    /**
     * 팀 멤버 아이디
     */
    private String teamMbrId;

    /**
     * 학습자 명
     */
    private String lrnrNm;

    private String instCd;

    private String language;	/** 다국어 */

    /** 시간슬롯 유지 여부 (Y: 유지, N: 삭제) */
    private String keepTimeSlot;
}
