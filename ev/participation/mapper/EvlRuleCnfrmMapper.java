package kr.innoaca.usr.web.ev.participation.mapper;

import kr.innoaca.usr.web.ev.participation.dto.EvlRuleCnfrmDto;
import org.apache.ibatis.annotations.Mapper;

/**
 * 동료 평가 규칙 확인 이력 매퍼 (#351)
 */
@Mapper
public interface EvlRuleCnfrmMapper {

    /** 규칙 확인 이력 1건 기록 (매 평가건마다) */
    int insertEvlRuleCnfrm(EvlRuleCnfrmDto dto);
}
