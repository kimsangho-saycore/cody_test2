package kr.innoaca.usr.web.ev.request.dto;

import kr.innoaca.usr.common.utils.Criteria;
import lombok.Data;

/**
 * @package kr.inno.usr.evalution.request.dto
 * @name EvalutionMbrDto
 * @date 2023-10-20
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-10-20        zeroj0706       최초 생성
 **/
@Data
public class EvalutionListDto {
    /* 평가번호 */
    private String evlNo;
    /* 평가차수(max차수) */
    private String evlDegr;
    /* 평가결과 코드 */
    private String evlStusCd;
    /* 평가결과 코드명 */
    private String evlStusNm;
    /* 프로젝트 번호 */
    private String projectNo;
    /* 학습과정 번호 */
    private String lcorsNo;
    /* 단위문제 번호 */
    private String uqstnNo;
    /* 프로젝트명 */
    private String projectNm;
    /* 학습과정명 */
    private String lcorsNm;
    /* 단위문제명 */
    private String uqstnNm;
    /* 평가시작일자 */
    private String evlBgngDt;
    /* 평가종료일자 */
    private String evlEndDt;
    /* 기관코드 */
    private String instCd;
    /* 학습자아이디 */
    private String mbrId;
    /* 평가 결과 코드명 */
    private String evlResltNm;
    /* 평가 결과 코드 */
    private String evlResltCd;
    /* 과정평가 */
    private String kwajungStr;
    /* 평가점수 */
    private String evlScr;
    /* 통과 이력 여부(전 차수 기준) - 재평가 중이거나 재평가가 불합격이어도 통과를 함께 표기하려고 내린다 */
    private String passedYn;
    /* 통과했던 차수의 점수(가장 최근 통과분) - 재평가 점수와 나란히 표기하려고 내린다 */
    private String passedScr;

    /** 총 갯수 */
    private int total;

    /** 열 번호  */
    private int rowNumb;

    /** 시작일 */
    private String startDt;

    /** 종료일 */
    private String endDt;

    /** 정렬 */
    private String orderBy;

    /** 현재 페이지 */
    private int page = 1;

    /** 기본 최대 페이지 */
    private int pagePerRows = 10;

    /** 페이징 처리 */
    private Criteria criteria;

    private String language;	/** 다국어 */
}
