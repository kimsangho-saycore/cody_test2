package kr.innoaca.usr.web.ev.participation.dto;

import kr.innoaca.usr.common.utils.Criteria;
import lombok.Data;

@Data
public class ParticipationListDto{
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
}
