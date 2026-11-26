package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;


/**
 * 동료평가 슬롯 조회용 DTO
 * - 요청 파라미터: instCd, projectNo, lcorsNo, uqstnNo, teamSn, cn, mbrId, evlPsblYmd
 * - 응답: slotTime (평가가능시간 HH:mm), openCount (해당 시간에 슬롯 오픈한 평가자 수)
 */
@Data
public class PeerEvlSlotDto {

    /* === 요청 파라미터 === */
    private String instCd;
    private Integer projectNo;
    private Long lcorsNo;
    private Long uqstnNo;
    private Long teamSn;
    private Integer cn;
    private String mbrId;
    private String evlPsblYmd;
    private String monDay;
    private String sunDay;
    /** 지정 모드에서 특정 평가자만 필터링할 ID 목록 (콤마 구분 문자열) */
    private String mbrIds;

    /* === 응답 필드 === */
    /** 평가가능시간 (HH:mm:ss) */
    private String slotTime;
    /** 해당 시간에 슬롯을 오픈한 평가자 수 */
    private Integer openCount;
    /** 평가가능일자 (YYYY-MM-DD) */
    private String slotDate;
}
