package kr.innoaca.usr.web.ev.participation.dto;

import lombok.Data;

/**
 * 동료 평가 규칙 확인 이력 (#351)
 * 평가 진행 시작 직전 게이트를 통과한 당사자가 규칙을 확인한 시점을 매 평가건(mtlEvlSn)마다 기록한다.
 */
@Data
public class EvlRuleCnfrmDto {

    private Long evlRuleCnfrmSn;   // 평가규칙확인일련번호(채번)
    private String mtlEvlSn;       // 상호평가일련번호(평가건)
    private String instCd;         // 기관코드(세션)
    private String mbrId;          // 확인 회원(세션 = 게이트 통과 당사자)
    private String ruleVer;        // 확인 시점 규칙 버전(프론트 정적 파일 버전)
}
