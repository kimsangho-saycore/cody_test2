package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

@Data
public class AiApiQueueDto {
    private String batchType;           // 배치 종류( 여기서는 suggestion)
    private String apiParam;            // api 요청 파라미터(JSON)
    private Long dataRegSn;             // evl_data_txn 테이블 seq
    private String apiStatus;           // api 상태(여기서는 W)
    private String updateTable;         // 결과 반영 대상 테이블(여기서는 evl_data_txn)
    private String regDt;               // 등록일시
    private String regId;               // 등록자
}
