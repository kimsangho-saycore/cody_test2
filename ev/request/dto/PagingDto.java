package kr.innoaca.usr.web.ev.request.dto;

import kr.innoaca.core.common.utils.Criteria;
import lombok.Data;
import lombok.ToString;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name PagingDto
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-16        zeroj0706       최초 생성
 **/

@Data
@ToString(callSuper = true)
public class PagingDto extends EvalutionReqDto {
    /** 현재페이지 번호 */
    private int page;
    /** 페이지당 로우 수 */
    private int pagePerRows;
    /** 페이징 공통 모델 */
    private Criteria criteria;
}
