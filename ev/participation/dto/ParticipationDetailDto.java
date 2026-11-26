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
public class ParticipationDetailDto {
    /* 프로젝트 정보 */
    private String projectNo;
    private String posProjectNo;
    private String projectNm;
    private String bgngYmd;
    private String endYmd;

    /* 학습과정 정보 */
    private String lcorsNo;
    private String lcorsNm;
    private String lrnCorsNm;

    /* 단위문제 정보 */
    private String uqstnNo;
    private String uqstnNm;
    private String uqstnSqnt;

    /* 공통 */
    private String mbrId;
    private String instCd;
    private String lrnMbrId;
    private String lrnInstCd;
    private String lrnMbrNm;
    private String lrnTmcnt;
    private String evLang;
    private String teamSn;
    private String evlScr;
    private String peerEvlImplHr;
    private String evlAdtnlScr;
    private String mbrNm;
    private String skllDesc;
    private String dvlpDesc;
    private String rstrtCondDesc;
    private String tcaseDesc;
    private String mtlEvlSn;
    private String lcorsCnt;
    private String dtlCdNm;
    private String profImgPath;
    private String kwajungStr;

    /* 평가자료 */
    /* 제출자료 행 키 — 첨부 다운로드 소유권 판정이 이 값으로 대상 행을 지목한다 */
    private String dataRegSn;
    private String srccdUrlAddr;
    private String dgnDataUrlAddr;
    private String dgnDataFileNm;
    private String dgnDataSysFileNm;
    private String dgnDataFileLo;
    private String brnchNm;

    /* 평가문항 */
    private String uqstnQitem;
    private String rn;
    private String qitemNo;
    private String qitemEvlResltCd;

    /* 역량점수 */
    private String cpblyNo;
    private String cpblyNm;
    private String cpblyBaseScr;
    private String cpblyScr;

    /* 평가피드백 */
    private String evlMbrNm;
    private String evlFdbkSn;
    private String evlFdbkWrtDt;
    private String evlFdbkCn;
    private String evlFdbkScr;
    private String cmntFdbkWrtDt;
    private String cmntFdbkCn;
    private String cmntFdbkScr;
    private String roleCd;
    private String emlAddr;

    /** #351 동료평가 규칙 확인 시각(평가건당 최초 확인). 미확인이면 null */
    private String ruleCnfrmDttm;

    /* 평가기본 */
    private String evlResltNm;
    private BigDecimal evlNo;
    private Integer evlDegr;
    /* 신규 평가차수 (FAIL 시 미진행 평가자 차수 이동용) */
    private Integer newEvlDegr;
    private String evlResult;

    /* 상호평가 */
    private String mtlEvlBgngDt;
    private String mtlEvlEndDt;
    private String mtlEvlScr;
    private String evlMbrId;
    private String evlInstCd;
    private String mtlEvlResltNm;
    private String mtlEvlResltCd;

    /* 알림 수신자 */
    private String lrnrNm;

    private List<ParticipationDetailDto> qitemNoList;
    private List<ParticipationDetailDto> cpblyList;

    private int fdbkCnt;
    private int teamCnt;
    private int scrCnt;
    private int timeCheckCnt;
    private String exprcDgrCd;
    private String mtlEvlAdtnlScr;
    private String mtlEvlAdtnlScrYn;

    private String language;	/** 다국어 */

    /* 평가 피드백 만족도 */
    private Integer fdbkSatScr;
}
