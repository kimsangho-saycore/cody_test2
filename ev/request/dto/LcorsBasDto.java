package kr.innoaca.usr.web.ev.request.dto;

import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name LcorsBasDto
 * @date 2023-10-25
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-25        zeroj0706       최초 생성
 **/
@Data
public class LcorsBasDto {
    private Integer projectNo;
    private String instCd;
    private String lrnCorsNm;
    private String lcorsNm;
    private String uqstnNm;
    private String bgngYmd;
    private String endYmd;
    private int teamCnt;
    private int maxTmbrNope;
    private int minTmbrNope;
    private String spctyFldNm;
    private int uqstnCnt;
    private String ctgNm;
    private Integer pjtFileNo;
    private String pjtFileDesc;
    private Integer pjtDefFileNo;
    private Integer pjtCpblyDefFileNo;
    private Integer pjtTechFileNo;
    private Integer pjtEvlGuideFileNo;
    private String useYn;
    private int uqstnSqnt;

    private String uqstnCn;
    private int useTmcnt;
    private String teamJoinYn;

    private String pjtFileNm;
    private String strgPjtFileNm;
    private String pjtDefFileNm;
    private String pjtCpblyDefFileNm;
    private String pjtTechFileNm;
    private String pjtEvlGuideFileNm;

    private String pjtDefStrgFileNm;
    private String pjtCpblyDefStrgFileNm;
    private String pjtTechStrgFileNm;
    private String pjtEvlGuideStrgFileNm;

    private String lrnMbrId;
    private String mbrId;
    private String mbrNm;
    private int teamSn;
    private String lrnMbrInstCd;
    private String recntCntnDt;
    private String instNm;
    private String emlAddr;
    private String sprMngrYn;
    private String glabAcntId;

    private Integer periodWk;
}
