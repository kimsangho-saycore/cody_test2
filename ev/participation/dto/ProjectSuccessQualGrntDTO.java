package kr.innoaca.usr.web.ev.participation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name ParticipationDetailDto
 * @date 2023-11-20
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-20        zeroj0706       최초 생성
 **/
@Data
public class ProjectSuccessQualGrntDTO {
    private String mbrId;
    private String qualId;
    private String qualGrntYn;
    private String instCd;
}
