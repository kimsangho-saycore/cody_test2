package kr.innoaca.usr.web.ev.request.dto;

import kr.innoaca.usr.web.cr.dto.MbrExprcHstDto;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvalutionDataTxnDto
 * @date 2023-10-18
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-18        zeroj0706       최초 생성
 **/

@Data
public class EvalutionDataTxnDto {
    /* 프로젝트번호 */
    private Integer projectNo;
    /* 자료등록일련번호 */
    private Long dataRegSn;
    /* 등록자아이디 */
    private String rgtrId;
    /* 등록일시 */
    private String regDt;
    /* 수정자아이디 */
    private String mdfrId;
    /* 수정일시 */
    private String mdfcnDt;
    /* 소스코드URL주소 */
    private String srccdUrlAddr;
    /* 학습자명 */
    private String lrnrNm;
    /* 프로젝트명 */
    private String pjtNm;
    /* 브랜치명 */
    private String brnchNm;
    /* GITLAB액세스토큰값 */
    private String glabAcesToknVal;
    /* 설계자료URL주소 */
    private String dgnDataUrlAddr;
    /* 설계자료파일명 */
    private String dgnDataFileNm;
    /* 설계자료시스템파일명 */
    private String 	dgnDataSysFileNm; //DGN_DATA_SYS_FILE_NM
    /* 설계자료파일위치 */
    private String dgnDataFileLo;
    /* 학습과정번호 */
    private Integer lcorsNo;
    /* 단위문제번호 */
    private Long uqstnNo;
    /* 팀일련번호 */
    private Long teamSn;
    /* 멘토일정일련번호 */
    private Long mtrScdlSn;
    /* 멘티요청일련번호 */
    private Long mteDmndSn;
    /* 아래는 조회 조건으로 들고 다님, 값이 없을 수 있음 */
    /* 학습회원 ID : 등록자id, 수정자id */
    private String mbrId;
    /* 학습회원기관코드 */
    private String instCd;
    /* 평가번호 */
    private Integer evlNo;
    /* 평가차수 */
    private Long evlDegr;
    /* 평가자명 */
    private String evlMbrNm;
    /* 학습차수 */
    private Long lrnTmcnt;
    /* 평가상태코드 */
    private String evlStusCd;
    /* 평가결과코드 */
    private String evlResltCd;
    /* 평가자기관코드 */
    private String evlMbrInstCd;
    /* 평가자id */
    private String evlMbrId;
    /* 프로젝트명 */
    private String projectNm;
    /* 학습과정명 */
    private String lcorsNoNm;
    /* 단위문제명 */
    private String uqstnNoNm;
    private String evlDmndRtrcnRsnCd;
    /* 상호평가일련번호 */
    private Long mtlEvlSn;
    /* 평가자유형타입코드 */
    private String evlrTypeCd;
    private String kwajungStr;
    private String projEvlStr;
    private MultipartFile fileInput;
    private String prlngNm;
    private String evLang;
    private String mtlEvlPamBgngDt;
    /* 회원경험치이력 */
    private MbrExprcHstDto mbrExprcHstDto;

    // KAN-210 입학선발
    /* 시험제출 자료 파일명 */
    private  String testPresentnFileNm;
    /* 시험제출 자료 시스템 파일명 */
    private  String testPresentnSysFileNm;
    /* 시험제출 자료 파일 위치 */
    private  String testPresentnFileLo;

    private String language;	/** 다국어 */

    // 팀미션 여부
    private String misTeamYn;
}
