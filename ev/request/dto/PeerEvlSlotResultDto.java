package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * 동료평가 슬롯 조회 응답 전용 DTO
 */
@Data
public class PeerEvlSlotResultDto {
    /** 평가가능시간 (HH:mm) */
    private String slotTime;
    /** 평가가능일자 (YYYY-MM-DD) */
    private String slotDate;
    /** 해당 시간에 슬롯을 오픈한 평가자 수 */
    private Integer openCount;
}
