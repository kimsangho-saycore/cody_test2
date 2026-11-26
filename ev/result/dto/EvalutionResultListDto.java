package kr.innoaca.usr.web.ev.result.dto;

import kr.innoaca.core.common.utils.Criteria;
import kr.innoaca.usr.web.mt.dto.MenteeMentoringDto;
import lombok.Data;
/**
 * @package kr.innoaca.usr.web.ev.result.dto
 * @name EvalutionResultLcorsDto
 * @date 2023-12-01
 * @author zeroj0706
 * @version 1.0.0
 * description : 평가 결과 목록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-12-01        zeroj0706       최초 생성
 **/
@Data
public class EvalutionResultListDto extends EvalutionResultDto {
    /* 멤버 ID */
    private String mbrId;
    /* 기관코드 */
    private String instCd;
    /** 총 갯수 */
    private int total;

    /** 열 번호  */
    private int rowNumb;

    /** 시작일 */
    private String startDt;

    /** 종료일 */
    private String endDt;

    /** 현재 페이지 */
    private int page = 1;

    /** 기본 최대 페이지 */
    private int pagePerRows = 10;

    /** 페이징 처리 */
    private Criteria criteria;

    private String language;	/** 다국어 */
}
