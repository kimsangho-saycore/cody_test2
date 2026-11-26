package kr.innoaca.usr.web.ev.participation.mapper;

import kr.innoaca.core.web.point.dto.PointDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDetailDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationDto;
import kr.innoaca.usr.web.ev.participation.dto.ParticipationListDto;
import kr.innoaca.usr.web.ev.request.dto.EvalutionBasDto;
import kr.innoaca.usr.web.ev.request.dto.ExprPointDto;
import kr.innoaca.usr.web.ev.request.dto.MtlEvlTxnDto;
import kr.innoaca.usr.web.ev.result.dto.EvalutionResultAutoEvlListDto;
import kr.innoaca.usr.web.ev.participation.dto.ProjectSuccessQualGrntDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @package kr.inno.usr.community.free.mapper
 * @name ParticipationMapper
 * @date 2023-11-10
 * @author zeroj0706
 * @version 1.0.0
 * description :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-11-10        zeroj0706       최초 생성
 **/
@Mapper
public interface ParticipationMapper {

    int rejectEvaluationRequestAndResetSchedule(ParticipationDto participationDto);
    /**
     * 평가요청 조회
     *
     * @param participationListDto
     * @return List<ParticipationDto>
     */
    List<ParticipationListDto> participationList(ParticipationListDto participationListDto);

    /**
     * 평가요청 전체 조회(페이징x)
     * @param participationListDto
     * @return
     */
    List<ParticipationListDto> participationListAll(ParticipationListDto participationListDto);

    /**
     * 평가요청 조회 총건수
     * @param participationDto
     * @return
     */
    int participationListAllCnt(ParticipationDto participationDto);

    /**
     * 평가요청 수락/거절처리
     *
     * @param participationDto
     * @return
     */
    int evalutionAcceptRefuseUpdate(ParticipationDto participationDto);

    /**
     * 평가요청자 정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto lrnMbrInfo(ParticipationDetailDto participationDetailDto);

    /**
     * 프로젝트학습정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto projectInfoDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto studyInfoDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 단위문제, 과정평가 평가가이드 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto queInfoDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 평가 참여 최소 시간 조회
     *
     * @return
     */
    List<String> getMinEvlTime();

    /**
     * 단위문제정보 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto uqstnInfoDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 상호평가상태코드 수정
     *
     * @param participationDto
     * @return
     */
    int mtlEvlStusCdUpdate(ParticipationDto participationDto);

    /**
     * 평가문항 조회
     *
     * @param participationDetailDto
     * @return
     */
    List<ParticipationDetailDto> uqstnQitemCnDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 평가결과 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto adtnlScrDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 역량점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    List<ParticipationDetailDto> cpblyDetail(ParticipationDetailDto participationDetailDto);


    /**
     * 피드백 조회
     *
     * @param participationDetailDto
     * @return
     */
    List<ParticipationDetailDto> evlFdbDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 피드백 등록
     *
     * @param participationDetailDto
     * @return
     */
    int fdbSave(ParticipationDetailDto participationDetailDto);

    /**
     * 평가문항 등록
     *
     * @param participationDetailDto
     * @return
     */
    int evlSave(ParticipationDetailDto participationDetailDto);

    /**
     * 역량평가 등록
     *
     * @param participationDetailDto
     * @return
     */
    int cpblySave(ParticipationDetailDto participationDetailDto);

    /**
     * 경험정도 점수, 경험정도 수정
     *
     * @param participationDetailDto
     * @return
     */
    int exprcDgrCdUpdate(ParticipationDetailDto participationDetailDto);

    /**
     * 평가확정 전 데이터 체크
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto saveChckDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 상호평가내역 종료처리
     *
     * @param participationDetailDto
     * @return
     */
    int evlEnd(ParticipationDetailDto participationDetailDto);

    /**
     * #80 평가완료(00006) 시 연결 오픈 슬롯을 종료(00003)로 전이 — 예약(00002) 방치 해소
     *
     * @param participationDetailDto
     * @return
     */
    int completeEvlPsblScdl(ParticipationDetailDto participationDetailDto);

    /**
     * 단위문제 마지막 평가자 확인 구분자
     *
     * @param participationDetailDto
     * @return
     */
    String getLastUqstn(ParticipationDetailDto participationDetailDto);

    /**
     * 실패로 추후 다른 평가자 평가요청 취소
     *
     * @param participationDetailDto
     * @return
     */
    int evlEndRtrcn(ParticipationDetailDto participationDetailDto);

    /**
     * EVL_BAS 테이블 평가결과 저장
     *
     * @param participationDetailDto
     * @return
     */
    int evlEndPOF(ParticipationDetailDto participationDetailDto);

    /**
     * 실패로 추후 다른 평가자 평가 스케출 취소
     *
     * @param participationDetailDto
     * @return
     */
    int evlEndScdl(ParticipationDetailDto participationDetailDto);

    /**
     * 실패로 추후 전문가 요청 취소
     *
     * @param participationDetailDto
     * @return
     */
    int evlMtCancel(ParticipationDetailDto participationDetailDto);

    /**
     * FAIL 시 차수 이동 대상(미진행 평가자) 수 조회
     *
     * @param participationDetailDto
     * @return
     */
    int selectRolloverTargetCount(ParticipationDetailDto participationDetailDto);

    /**
     * 평가기본입력 평가차수증가 입력
     *
     * @param participationDetailDto
     * @return
     */
    int insertEvlBasEvlDegr(ParticipationDetailDto participationDetailDto);

    /**
     * FAIL 시 미진행 평가자 신규 차수로 이동
     *
     * @param participationDetailDto
     * @return
     */
    int updateMtlEvlTxnRolloverDegr(ParticipationDetailDto participationDetailDto);

    /**
     * 단위문제 평가내역 저장
     *
     * @param participationDetailDto
     * @return
     */
    int lastUqstnExec1(ParticipationDetailDto participationDetailDto);

    /**
     * 역량문제 평가내역 저장
     *
     * @param participationDetailDto
     * @return
     */
    int lastUqstnExec2(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정단위문제팀별강제종료 업데이트
     *
     * @param participationDetailDto
     * @return
     */
    int lastLcorsExec2p5(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정 마지막 확인 구분자
     *
     * @param participationDetailDto
     * @return
     */
    String getLastLcors(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정평가내역 저장
     *
     * @param participationDetailDto
     * @return
     */
    int lastLcorsExec1(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정역량평가내역 저장
     *
     * @param participationDetailDto
     * @return
     */
    int lastLcorsExec2(ParticipationDetailDto participationDetailDto);

    /**
     * 팀상태코드 업데이트
     *
     * @param participationDetailDto
     * @return
     */
    int lastLcorsExec3(ParticipationDetailDto participationDetailDto);

    /**
     * 팀상태코드 업데이트
     * @return
     */
    int lastUqstnExe3(ParticipationDetailDto participationDetailDto);

    /**
     * 경험치/포인트 기준점수 조회
     *
     * @param exprPointDto
     * @return
     */
    PointDto getPoint(ExprPointDto exprPointDto);

    /**
     * 취소요청 할 평가자 리스트 조회
     *
     * @param participationDetailDto
     * @return
     */
    List<MtlEvlTxnDto> getMtCancelList(ParticipationDetailDto participationDetailDto);

    /**
     * 깃주소 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getGitAddr(ParticipationDetailDto participationDetailDto);

    /**
     * 평가 PASS 경험치 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getPassExpScore(ParticipationDetailDto participationDetailDto);

    /**
     * 알림 수신자 조회
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto getLrnMbrs(ParticipationDetailDto participationDetailDto);

    /**
     * 평가결과 상세
     *
     * @param participationDetailDto
     * @return
     */
    ParticipationDetailDto goQueryEvalRsltDtlInfo(ParticipationDetailDto participationDetailDto);

    /**
     * 피드백 삭제
     *
     * @param participationDetailDto
     * @return
     */
    int fdbDelete(ParticipationDetailDto participationDetailDto);

    /**
     * 평가상세이력 내용 조회
     *
     * @param participationDetailDto
     * @return
     */
    List<ParticipationDetailDto> evlInfo(ParticipationDetailDto participationDetailDto);

    /**
     * 자동평가 조회
     *
     * @param participationDetailDto
     * @return
     */
    EvalutionResultAutoEvlListDto autoEvlDetail(ParticipationDetailDto participationDetailDto);

    /**
     * 평가기본상태코드 수정
     *
     * @param evalutionBasDto
     * @return
     */
    int evlBasStusCdUpdate(EvalutionBasDto evalutionBasDto);

    /**
     * 프로젝트 학습과정 마지막 확인 구분자
     * @return
     */
    String getLastPjtLcors(ParticipationDetailDto participationDetailDto);

    /**
     * 평가결과 상세 단위문제 각각의 자동평가자료 목록 조회
     *
     * @param participationDto
     * @return EvalutionResultAutoEvlListDto
     */
    EvalutionResultAutoEvlListDto selectUqstnNoAutoEvlList(ParticipationDto participationDto);

    /**
     * EVL_BAS 테이블 평가결과 저장
     *
     * @param participationDetailDto
     * @return
     */
    int evlTestEndPOF(ParticipationDetailDto participationDetailDto);


    /**
     * EVL_BAS 테이블 평가결과 저장
     *
     * @param participationDetailDto
     * @return
     */
    int setARSubsidyClassisMember(ParticipationDetailDto participationDetailDto);

    /**
     * 평가 처리 전 상호평가 내역 상태 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getPreEvlStatus(ParticipationDto participationDto);


    /**
     * 평가 허용 IP 주소 조회
     *
     * @param instCd
     * @return
     */
    List<String> getParticipationIpAddrList(String instCd);


    /**
     * 프로젝트 최종 PASS 확인
     *
     * @param participationDetailDto
     * @return
     */
    String getAllPjtSuccess(ParticipationDetailDto participationDetailDto);


    /**
     * 학습자 자격 보유 여부 체크
     * 
     * @param participationDetailDto
     * @return
     */
    List<ProjectSuccessQualGrntDTO> getTeamMbrOwnsQualYn(ParticipationDetailDto participationDetailDto);



    /**
     * 특정 프로젝트 해결 시 학습자에게 자격 보유 
     * 
     * @param participationDetailDto
     * @return
     */
    int insertMbrOwnQual(ProjectSuccessQualGrntDTO projectSuccessQualGrntDTO);

    /**
     * 단위문제 크레딧 점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getUqstnCreditScore(ParticipationDetailDto participationDetailDto);

    /**
     * 단위문제 보너스 크레딧 점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getBonusCreditScore(ParticipationDetailDto participationDetailDto);

    /**
     * 모든 평가자 추가점수 제공 여부 확인
     *
     * @param participationDetailDto
     * @return
     */
    String checkAllEvlrAdtnlScrPvsnYn(ParticipationDetailDto participationDetailDto);

    /**
     * 학습과정 크레딧 점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getLcorsCreditScore(ParticipationDetailDto participationDetailDto);

    /**
     * 프로젝트 크레딧 점수 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getProjectCreditScore(ParticipationDetailDto participationDetailDto);

    /**
     * 단위문제 필수 여부 조회
     *
     * @param participationDetailDto
     * @return
     */
    String getUqstnRequireYn(ParticipationDetailDto participationDetailDto);

    /**
     * 평가 피드백 만족도 점수 업데이트
     *
     * @param participationDetailDto
     */
    void updateFdbkSatScr(ParticipationDetailDto participationDetailDto);

    /**
     * 상호평가 상태 조회
     * @param participationDetailDto
     * @return
     */
    String selectMtlEvlStusCd(ParticipationDetailDto participationDetailDto);

}
