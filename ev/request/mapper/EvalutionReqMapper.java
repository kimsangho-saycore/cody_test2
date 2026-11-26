package kr.innoaca.usr.web.ev.request.mapper;

import kr.innoaca.usr.web.ev.request.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @package kr.inno.usr.evalution.request.mapper
 * @name EvalutionMapper
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 *          description :
 *          ===========================================================
 *          DATE AUTHOR NOTE
 *          -----------------------------------------------------------
 *          2023-10-16 zeroj0706 최초 생성
 **/

@Mapper
public interface EvalutionReqMapper {

    /**
     * 학습자ID로 프로젝트명 목록 조회
     *
     * @param projectDto
     * @return List
     */
    List<ProjectDto> selectProjectNmList(ProjectDto projectDto);

    /**
     * 학습자ID로 프로젝트의 학습과정 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return List
     */
    List<LrnCorsNmUqstnNmDto> selectLrnCorsNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto);

    /**
     * 학습자ID로 학습과정의 단위문제 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return List
     */
    List<LrnCorsNmUqstnNmDto> selectLrnUqstnNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto);

    /**
     * 학습과정번호별 팀일련번호 조회
     *
     * @param evalDataTxnDto
     * @return long
     */
    long selectMaxTeamSnForMbrId(EvalutionDataTxnDto evalDataTxnDto);

    /**
     * 학습과정 결과 PASS 확인
     *
     * @param evalutionBasDto
     * @return String
     */
    String selectLcorsEvlPsblYn(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto);

    /**
     * 단위문제 결과 PASS 확인
     *
     * @param evalutionBasDto
     * @return String
     */
    String selectEvlPsblYn(EvalutionBasDto evalutionBasDto);

    /**
     * 단위문제 정보 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return LrnCorsNmUqstnNmDto
     */
    LrnCorsNmUqstnNmDto selectLrnUqstnNmYN(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto);

    /**
     * 기존 평가 내역 확인
     *
     * @param evalutionDataTxnDto
     * @return EvalutionBasDto
     */
    EvalutionBasDto selectEvlNo(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * 기존 평가 내역 PK로 조회
     *
     * @param evalutionBasDto
     * @return EvalutionBasDto
     */
    EvalutionBasDto selectEvlBasByPk(EvalutionBasDto evalutionBasDto);

    /**
     * 기존 평가 자료 내역 PK로 조회
     *
     * @param evalutionDataTxnDataTx
     * @return EvalutionDataTxnDto
     */
    EvalutionDataTxnDto selectEvlDataTxnByPk(EvalutionDataTxnDto evalutionDataTxnDataTx);

    /**
     * 자동평가 내역 단건 조회
     *
     * @param evalutionDataTxnDataTx
     * @return AutoEvlTxnDto
     */
    AutoEvlTxnDto selectAutoEvlTxnByPk(EvalutionDataTxnDto evalutionDataTxnDataTx);

    /**
     * 상호평가내역 목록 조회
     *
     * @param evalutionDataTxnDto
     * @return List<MtlEvlTxnDto>
     */
    List<MtlEvlTxnDto> mtlEvlTxnDtoByPkList(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * git 주소 선택할 멤버 조회
     *
     * @param lcorsMbrPtpTxnDto
     * @return List<LcorsMbrPtpTxnDto>
     */
    List<LcorsMbrPtpTxnDto> selectGitAddrMbrIdsList(LcorsMbrPtpTxnDto lcorsMbrPtpTxnDto);

    /**
     * 평가자 지정 : 학습과정별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return List<EvalutionStdDto>
     */
    List<EvalutionStdDto> selectLcorsEvlStdList(EvalutionStdDto evalutionStdDto);

    /**
     * 평가자 지정 : 단위문제별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return ResponseMessage
     */
    List<EvalutionStdDto> selectUqstnEvlStdList(EvalutionStdDto evalutionStdDto);

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return List<EvalutionMbrDto>
     */
    List<EvalutionMbrDto> selectCase1MbrIdList(EvalutionMbrDto evalutionMbrDto);

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return List<EvalutionMbrDto>
     */
    List<EvalutionMbrDto> selectCase2MbrIdList(EvalutionMbrDto evalutionMbrDto);

    /**
     * 평가자 지정 : 평가자 대상범위가 학습과정 수강자 일 경우 동료 평가자 평가가능시간 목록 조회
     *
     * @param enbldEvlTimeDto
     * @return List<EnbldEvlTimeDto>
     */
    List<EnbldEvlTimeDto> selectCase1EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto);

    /**
     * 평가자 지정 : 평가자 대상범위가 학습자 회원 일 경우 동료 평가자 평가가능시간 목록 조회
     *
     * @param enbldEvlTimeDto
     * @return List<EnbldEvlTimeDto>
     */
    List<EnbldEvlTimeDto> selectCase2EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto);

    /**
     * 동료평가 슬롯 조회 (학습과정 수강자) - 시간+인원수만 반환
     */
    List<PeerEvlSlotResultDto> selectPeerEvlSlotCase1(PeerEvlSlotDto peerEvlSlotDto);

    /**
     * 동료평가 슬롯 조회 (전체 수강생) - 시간+인원수만 반환
     */
    List<PeerEvlSlotResultDto> selectPeerEvlSlotCase2(PeerEvlSlotDto peerEvlSlotDto);

    /**
     * 자동평가 요청 전 이전평가 결과 fail인경우 재평가 기간 조회
     *
     * @param reEvalChkDto
     * @return ReEvalChkDto
     */
    ReEvalChkDto selectReEvalTimechk(ReEvalChkDto reEvalChkDto);

    /**
     * 현재 총 포인트
     *
     * @param exprPointDto
     * @return ExprPointDto
     */
    ExprPointDto selectPointSum(ExprPointDto exprPointDto);

    /**
     * 포인트 총계 정보 연계(평가자 지정 popup 화면에서 평가 요청 하기 전 확인)
     *
     * @param evalutionDataTxnDto
     * @return List<EvalutionDataTxnDto>
     */
    List<EvalutionDataTxnDto> apiCmmnGetPointNowScr(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * 평가요청 차감 포인트 조회 NULL이면 0
     *
     * @param crtrScrDto
     * @return Float
     */
    Float selectPointNowScr(CrtrScrDto crtrScrDto);

    /**
     * 평가자 지정 : 평가기본 조회
     *
     * @param regReqEvalDto
     * @return List<RegReqEvalDto>
     */
    List<RegReqEvalDto> selectEvlBas(RegReqEvalDto regReqEvalDto);

    /**
     * 평가요청등록
     *
     * @param evalutionBasDto
     * @return int
     */
    int insertNewEvlBas(EvalutionBasDto evalutionBasDto);

    /**
     * 평가요청등록
     *
     * @param evalutionBasDto
     * @return int
     */
    int insertEvlBasEvlDegr(EvalutionBasDto evalutionBasDto);

    /**
     * 상호평가내역 입력
     *
     * @param regReqEvalDto
     * @return int
     */
    int insertNewMtlEvlTxn(RegReqEvalDto regReqEvalDto);

    /**
     * 새롭게 입력된 상호평가번호 조회
     *
     * @param regReqEvalDto
     * @return List<RegReqEvalDto>
     */
    List<RegReqEvalDto> selectNewMtlEvlTxn(RegReqEvalDto regReqEvalDto);

    /**
     * 평가가능일정 수정
     *
     * @param regReqEvalDto
     * @return int
     */
    int updateEvlPsblScdlBas(RegReqEvalDto regReqEvalDto);

    /**
     * 알림게시판기본 게시물일련번호 조회
     *
     * @param regReqEvalDto
     * @return Long
     */
    Long selectNtcBbsBasByMdfcnDt(RegReqEvalDto regReqEvalDto);

    /**
     * 평가자료내역 등록 : 기존 평가요청자료에 자료등록일련번호를 증가시켜 insert
     *
     * @param evalutionDataTxnDto
     * @return int
     */
    int evlDataTxnByUqstnNoSave(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * 상호평가내역 목록 조회
     *
     * @param evalutionDataTxnDto
     * @return EvalutionDataTxnDto
     */
    EvalutionDataTxnDto selectMtlEvlTxnDataRegSn(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * 평가자지정 후 Git Lab 프로젝트 기존 멤버 삭제, 변경 멤버 추가 API 호출에 사용
     *
     * @param chgSrccUrlAddrDto
     * @return List<ChgSrccUrlAddrDto>
     */
    List<ChgSrccUrlAddrDto> selectChangeSrccdUrlAddr(ChgSrccUrlAddrDto chgSrccUrlAddrDto);

    /**
     * 자료기본 자료일련번호 수정
     *
     * @param evalutionBasDto
     * @return int
     */
    int updateEvlBasDataRegSn(EvalutionBasDto evalutionBasDto);

    /**
     * 평가자 정보 조회
     *
     * @param lcorsBasDto
     * @return List<LcorsBasDto>
     */
    List<LcorsBasDto> selectLpMbrBasList(LcorsBasDto lcorsBasDto);

    /**
     * 멘토회원 정보 조회
     *
     * @param evalutionMbrDto
     * @return List<EvalutionMbrDto>
     */
    List<EvalutionMbrDto> mentoEvalList(EvalutionMbrDto evalutionMbrDto);

    /**
     * 전문가 평가자 평가 가능시간 목록 조회
     *
     * @param evalutionMbrDto
     * @return List<EnbldEvlTimeDto>
     */
    List<EvalutionMbrDto> selectScheduleList(EvalutionMbrDto evalutionMbrDto);

    /**
     * 요청일련번호 채번
     *
     * @param menteeReqDto
     * @return int
     */
    int selectDmndSn(MenteeReqDto menteeReqDto);

    /**
     * 멘토 일시 조회
     *
     * @param menteeReqDto
     * @return String
     */
    String selectMentoOrgTime(MenteeReqDto menteeReqDto);

    /**
     * 멘토 요일 조회
     *
     * @param menteeReqDto
     * @return String
     */
    String selectMentoReqTime(MenteeReqDto menteeReqDto);

    /**
     * 멘토 일정 등록
     *
     * @param menteeReqDto
     * @return int
     */
    int insertMentoSchedule(MenteeReqDto menteeReqDto);

    /**
     * 멘토 일정 사용여부 수정
     *
     * @param menteeReqDto
     * @return int
     */
    int updateMentoUseYn(MenteeReqDto menteeReqDto);

    /**
     * 요청 일련번호 조회
     *
     * @param menteeReqDto
     * @return int
     */
    int selectMentoScdlSn(MenteeReqDto menteeReqDto);

    /**
     * 멘티 일정 등록
     *
     * @param menteeReqDto
     * @return int
     */
    int insertMenteeSchedule(MenteeReqDto menteeReqDto);

    /**
     * 포인트 히스토리 누적(수정)
     *
     * @param exprPointDto
     * @return int
     */
    int insertPointHst(ExprPointDto exprPointDto);

    /**
     * 경험치 히스토리 누적(수정)
     *
     * @param exprPointDto
     * @return int
     */
    int insertExprHst(ExprPointDto exprPointDto);

    /**
     * 멘토평가일정기본 입력
     *
     * @param regReqEvalDto
     * @return int
     */
    int insertNewMtrEvlScdlBas(RegReqEvalDto regReqEvalDto);

    /**
     * 멘토평가일정기본 조회
     *
     * @param mtrEvlScdlBasByMteConDto
     * @return List<MtrEvlScdlBasByMteConDto>
     */
    List<MtrEvlScdlBasByMteConDto> selectMtrEvlScdlBasByMteCon(MtrEvlScdlBasByMteConDto mtrEvlScdlBasByMteConDto);

    /**
     * 멘토평가 요청후 멘토평가일정기본 수정
     *
     * @param regReqEvalDto
     * @return int
     */
    int updateMtrEvlScdlBasByPk(RegReqEvalDto regReqEvalDto);

    /**
     * 멘토평가 요청후 상호평가내역 수정
     *
     * @param regReqEvalDto
     * @return int
     */
    int updateMtlEvlTxnByPk(RegReqEvalDto regReqEvalDto);

    /**
     * 회원 조회
     *
     * @param mbrBasDto
     * @return int
     */
    List<MbrBasDto> selectMbrBasList(MbrBasDto mbrBasDto);

    /**
     * 평가자지정 후 Git Lab 프로젝트 멤버 추가 API 호출에 사용
     *
     * @param evalutionBasDto
     * @return String
     */
    String selectSrccdUrlAddr(EvalutionBasDto evalutionBasDto);

    /**
     * 상호평가 평가취소 수정
     *
     * @param mtlEvlTxnDto
     * @return int
     */
    int cancelMtlEvlTxnUpdate(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 평가가능 일정 취소 수정
     *
     * @param mtlEvlTxnDto
     * @return int
     */
    int updateEvlPsblScdlBasCncl(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 평가가능 일정 취소 수정
     *
     * @param mtlEvlTxnDto
     * @return int
     */
    int updateNoShowEvlPsblScdlBasCncl(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 평가자지정 후 Git Lab 프로젝트 멤버 삭제 API 호출에 사용
     *
     * @param evlTxnDto
     * @return String
     */
    String selectDelSrccdUrlAddr(MtlEvlTxnDto evlTxnDto);

    /**
     * 멘티일정취소
     *
     * @param menteeReqDto
     * @return int
     */
    int deleteMenteeSchedule(MenteeReqDto menteeReqDto);

    /**
     * 멘토평가 취소 요청후 멘토평가일정기본 수정
     *
     * @param regReqEvalDto
     * @return int
     */
    int updateMtrEvlScdlBasByPk2(RegReqEvalDto regReqEvalDto);

    /**
     * 평가기본 상태코드 수정
     *
     * @param regReqEvalDto
     * @return int
     */
    int updateEvlBasAfterAutoEvlReq(RegReqEvalDto regReqEvalDto);

    /**
     * 평가요청 목록 조회
     *
     * @param evalutionListDto
     * @return List<EvalutionListDto>
     */
    List<EvalutionListDto> searchList(EvalutionListDto evalutionListDto);

    /**
     * 평가요청 목록 조회 총건수
     *
     * @param evalutionListDto
     * @return
     */
    int searchListAllCnt(EvalutionListDto evalutionListDto);

    /**
     * 자동평가 내역 등록
     *
     * @param autoEvlTxnDto
     * @return
     */
    int insertAutoEvlTxn(AutoEvlTxnDto autoEvlTxnDto);

    /**
     * 멘토 평가 가능시간 조회
     * 
     * @param evalutionMbrDto
     * @return
     */
    List<EvalutionMbrDto> mentoScheduleList(EvalutionMbrDto evalutionMbrDto);

    int getReservedEvaluationRequestCount(RegReqEvalDto regReqEvalDto);

    /**
     * 평가 취소 전 상호평가 내역 상태 조회
     *
     * @param mtlEvlTxnDto
     * @return
     */
    String getPreEvlStatusDetail(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 회원이 프로젝트 부여자격을 보유하고 있는지 확인
     *
     * @param mbrId 회원 ID
     * @param projectNo 프로젝트 번호
     * @param instCd 기관 코드
     * @return 보유 여부 (Y/N)
     */
    String checkMbrHasProjectQualGrnt(@Param("mbrId") String mbrId,
                                       @Param("projectNo") Integer projectNo,
                                       @Param("instCd") String instCd);

    /**
     * 학습과정 유형코드 조회
     *
     * @param projectNo 프로젝트 번호
     * @param lcorsNo 학습과정 번호
     * @return 학습과정 유형코드
     */
    String selectLcorsTypeCd(@Param("projectNo") Integer projectNo,
                              @Param("lcorsNo") Integer lcorsNo);

    /**
     * 미션팀 회원의 리더 여부 조회
     *
     * @param mbrId 회원 ID
     * @param projectNo 프로젝트 번호
     * @param lcorsNo 학습과정 번호
     * @param uqstnNo 단위문제 번호
     * @param teamSn 팀 일련번호
     * @return 리더 여부 (Y/N)
     */
    String selectMbrLeadrYn(@Param("mbrId") String mbrId,
                            @Param("projectNo") Integer projectNo,
                            @Param("lcorsNo") Integer lcorsNo,
                            @Param("uqstnNo") Long uqstnNo,
                            @Param("teamSn") Long teamSn);

    /**
     * 첫번째 평가자료내역 조회(project_no, lcors_no, uqstn_no 기준)
     *
     * @param evalutionDataTxnDto
     * @return
     */
    EvalutionDataTxnDto selectFirstEvlDataTxn(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * ai_api_queue 데이터 추가
     *
     * @param aiApiQueueDto
     * @return
     */
    Integer insertAiApiQueue(AiApiQueueDto aiApiQueueDto);

    /**
     * 학습자 검색 - 상호평가내역 목록 조회 (조회 전용, 민감정보 마스킹)
     *
     * @param evalutionDataTxnDto
     * @return List<MtlEvlTxnDto>
     */
    List<MtlEvlTxnDto> mtlEvlTxnDtoByPkListForView(EvalutionDataTxnDto evalutionDataTxnDto);

    /**
     * 상호평가 내역 등록자 ID 조회 (소유권 검증용)
     *
     * @param mtlEvlTxnDto
     * @return 등록자 ID (RGTR_ID)
     */
    String getMtlEvlTxnRgtrId(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 상호평가 내역의 평가자 ID(EVL_MBR_ID) 조회 — 노쇼 오픈 슬롯 삭제 대상 확정용
     *
     * @param mtlEvlTxnDto mtlEvlSn 포함
     * @return 평가자 ID (EVL_MBR_ID)
     */
    String getMtlEvlTxnEvlMbrId(MtlEvlTxnDto mtlEvlTxnDto);

    /**
     * 노쇼 평가자의 미래 오픈(00001, 미매칭) 슬롯을 삭제한다 (#39). 매칭(00002)건은 보존.
     *
     * @param evlMbrId 평가자 회원아이디
     * @return 삭제된 슬롯 수
     */
    int deleteNoShowEvlrOpenScdl(@Param("evlMbrId") String evlMbrId);
}
