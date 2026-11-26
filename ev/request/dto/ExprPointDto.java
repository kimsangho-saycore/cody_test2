package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ExprPointDto
 * @date 2023-11-03
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-03        zeroj0706       최초 생성
 **/
@Data
public class ExprPointDto {
    private String pointScore;
    private String mbrId;
    private String instCd;
    private String memberId;
    private String institutionCode;
    private String roleCd;
    private BigDecimal pntSn;
    private BigDecimal exprcSn;

    private String workjobDivisionCode;
    private String systemDivisionCode;
    private String criteriaTypeCode;
    private long criteriaTypeNumber;
    private String criteriaTypeName;
    private String criteriaAccumulationDeductionCode;
    private double criteriaScore;
    private String criteriaDetailDescrption;
    private double criteriaPointScore;
    private double criteriaExperienceScore;
    private String criteriaUseYn;

    private long experienceTypeNumber;
    private String experienceRegistrationDatetime;
    private String experienceDetailDescription;
    private double experienceScore;
    private String experienceCrtrAccumDdcCd;
    private String experienceCrtrTypeNm;

    private String classificationCode;
    private long pointTypeNumber;
    private String pointRegistrationDatetime;
    private String pointDetailDescription;
    private String engPointDetailDescription;
    private double flPointScore;
    private String pointCrtrAccumDdcCd;
    private String pointCrtrTypeNm;
    private String crtrTypeCd;
    private String crtrTypeNo;
}
