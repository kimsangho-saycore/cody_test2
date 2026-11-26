package kr.innoaca.usr.web.ev.request.service;

import kr.innoaca.core.common.auth.Member;
import kr.innoaca.core.common.dto.Constants;
import kr.innoaca.core.common.dto.EmailDTO;
import kr.innoaca.core.common.exception.ValidCustomException;
import kr.innoaca.core.common.notification.event.NotificationEvent;
import kr.innoaca.core.common.notification.event.NotificationMessage;
import kr.innoaca.core.common.notification.event.evaluation.CancelEvaluationEvent;
import kr.innoaca.core.common.response.ResponseMessage;
import kr.innoaca.core.common.utils.*;
import kr.innoaca.core.domain.bbs.NtcBbsBasMapper;
import kr.innoaca.core.web.alarm.domain.constant.EventBoundary;
import kr.innoaca.core.web.alarm.domain.constant.EventDetail;
import kr.innoaca.core.web.alarm.dto.NtcBbsBasDTO;
import kr.innoaca.core.web.github.dto.GitHubInfoDto;
import kr.innoaca.core.web.github.service.GithubService;
import kr.innoaca.core.web.member.mapper.MemberMapper;
import kr.innoaca.core.web.point.dto.PointDto;
import kr.innoaca.core.web.point.service.PointService;
import kr.innoaca.usr.common.utils.Criteria;
import kr.innoaca.core.common.notification.builder.CommonNotificationMessageBuilder;
import kr.innoaca.core.common.notification.dto.NotificationMtlEvlTxn;
import kr.innoaca.core.common.notification.domain.NotificationTemplate;
import kr.innoaca.core.common.notification.mapper.NotificationTemplateMapper;
import kr.innoaca.usr.common.utils.Paginator;
import kr.innoaca.usr.domain.MtlEvlTxnMapper;
import kr.innoaca.usr.evaluation.EvaluationAppendMapper;
import kr.innoaca.usr.evaluation.EvaluationBas;
import kr.innoaca.usr.evaluation.MtlEvlTxn;
import kr.innoaca.usr.evaluation.dto.query.AppendedEvaluationResponse;
import kr.innoaca.usr.web.ev.request.dto.*;
import kr.innoaca.usr.web.ev.request.mapper.EvalutionReqMapper;
import kr.innoaca.usr.web.learning.dto.LearningDto;
import kr.innoaca.usr.web.learning.dto.UqstnModel;
import kr.innoaca.usr.web.learning.dto.UqstnTeam;
import kr.innoaca.usr.web.learning.mapper.LearningMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.innoaca.core.web.point.dto.MbrPntHstDto;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static kr.innoaca.core.common.dto.Constants.*;

/**
 * @package kr.inno.usr.evalution.request.service
 * @name EvalutionService
 * @date 2023-10-16
 * @author zeroj0706
 * @version 1.0.0
 *          description :
 *          ===========================================================
 *          DATE AUTHOR NOTE
 *          -----------------------------------------------------------
 *          2023-10-16 zeroj0706 최초 생성
 **/

@Service
@RequiredArgsConstructor
public class EvalutionReqService {
    @Value("${domain-info.ams-url}")
    String domainInfoAmsUrl;

    @Autowired
    PointService pointService;

    private static final Logger log = LoggerFactory.getLogger(EvalutionReqService.class);

    private final MessageSource messageSource;

    private final EmailUtil emailUtil;

    private final AwsS3Util awsS3Util;

    private final ApiUtil apiUtil;

    private final EvalutionReqMapper evalutionMapper;

    private final NtcBbsBasMapper ntcBbsBasMapper;

    private static final String HTML_BR = "<br/>";

    private static final String REGEX_DT_1 = "yyyyMMdd";
    private static final String REGEX_DT_2 = "yyyy-MM-dd HH:mm:ss";

    private static final String COL_EVL_PSBL_YN = "evlPsblYn";
    private static final String COL_LIST = "list_";
    private static final String COL_RET_CODE = "retCode";
    private static final String COL_RETURN_CODE = "returnCode";
    private static final String COL_USER_NAME = "userName";
    private static final String COL_RESULT = "result";
    private static final String COL_GIT_ADDRESS = "gitAddress";
    private static final String COL_DMND_SN = "dmndSn";

    private static final String MSG_EVREQ_MSG_GITLABPJTADDERROR = "usr.evreq.msg.gitlabpjtadderror";
    private static final String MSG_EVREQ_MSG_POINTERROR = "usr.evreq.msg.pointerror";

    private final MtlEvlTxnMapper mtlEvlTxnMapper;
    private final MemberMapper memberMapper;
    private final EvaluationAppendMapper evaluationAppendMapper;
    private final kr.innoaca.core.mapper.evaluation.ReEvaluationMapper reEvaluationMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CommonNotificationMessageBuilder commonNotificationMessageBuilder;
    private final LearningMapper learningMapper;
    private final NotificationTemplateMapper notificationTemplateMapper;

    private final GithubService githubService;
    private final AES256Util aes256Util;
    private final kr.innoaca.core.mapper.evaluation.PassJudgmentMapper passJudgmentMapper; // #159 통과방식 조회(판정과 같은 매퍼)

    /**
     * 학습자ID로 프로젝트명 목록 조회
     *
     * @param projectDto
     * @return List
     */
    public List<ProjectDto> selectProjectNmList(ProjectDto projectDto) {
        return evalutionMapper.selectProjectNmList(projectDto);
    }

    /**
     * 학습자ID로 프로젝트의 학습과정 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return List
     */
    public ResponseMessage selectLrnCorsNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto,
            @AuthenticationPrincipal Member member) {
        ResponseMessage responseMessage = new ResponseMessage();
        if (lrnCorsNmUqstnNmDto.getMbrId() == null) {
            lrnCorsNmUqstnNmDto.setMbrId(member.getMbrId());
        }
        if (lrnCorsNmUqstnNmDto.getInstCd() == null) {
            lrnCorsNmUqstnNmDto.setInstCd(member.getInstCd());
        }
        responseMessage.setResult(evalutionMapper.selectLrnCorsNmList(lrnCorsNmUqstnNmDto));
        return responseMessage;
    }

    /**
     * 학습자ID로 학습과정의 단위문제 목록 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @param member
     * @return ResponseMessage
     */
    public ResponseMessage selectLrnUqstnNmList(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto, Member member) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        if (lrnCorsNmUqstnNmDto.getMbrId() == null) {
            lrnCorsNmUqstnNmDto.setMbrId(member.getMbrId());
        }
        paramMap.put("lrnUqstnNmList", evalutionMapper.selectLrnUqstnNmList(lrnCorsNmUqstnNmDto));
        paramMap.put("lcorsEvlPsblYn", evalutionMapper.selectLcorsEvlPsblYn(lrnCorsNmUqstnNmDto));
        responseMessage.setResult(paramMap);
        return responseMessage;
    }

    /**
     * 학습과정번호별 팀일련번호 조회
     *
     * @param evalDataTxnDto
     * @return long
     */
    public long selectTeamSn(EvalutionDataTxnDto evalDataTxnDto) {
        return evalutionMapper.selectMaxTeamSnForMbrId(evalDataTxnDto);
    }

    /**
     * 단위문제 결과 PASS 확인
     *
     * @param evalutionBasDto
     * @return String
     */
    public String selectEvlPsblYn(EvalutionBasDto evalutionBasDto) {
        return evalutionMapper.selectEvlPsblYn(evalutionBasDto);
    }

    /**
     * 문제번호의 자동평가, 동료평가, 전문가평가여부, 인원수 조회
     *
     * @param lrnCorsNmUqstnNmDto
     * @return LrnCorsNmUqstnNmDto
     */
    public ResponseMessage selectLrnUqstnNmYN(LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        lrnCorsNmUqstnNmDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectLrnUqstnNmYN(lrnCorsNmUqstnNmDto));
        return responseMessage;
    }

    /**
     * 평가기본의 마지막 차수 확인
     *
     * @param evalutionDataTxnDto
     * @return EvalutionBasDto
     */
    public ResponseMessage selectEvlNo(EvalutionDataTxnDto evalutionDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectEvlNo(evalutionDataTxnDto));
        return responseMessage;
    }

    /**
     * 평가기본 조회
     *
     * @param evalutionBasDto
     * @return EvalutionBasDto
     */
    public ResponseMessage selectEvlBasByPk(EvalutionBasDto evalutionBasDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionBasDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectEvlBasByPk(evalutionBasDto));
        return responseMessage;
    }

    /**
     * 평가자료 조회
     *
     * @param evalutionDataTxnDto
     * @return EvalutionDataTxnDto
     */
    public ResponseMessage selectEvlDataTxnByPk(EvalutionDataTxnDto evalutionDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectEvlDataTxnByPk(evalutionDataTxnDto));
        return responseMessage;
    }

    /**
     * 자동평가내역 조회
     *
     * @param evalutionDataTxnDto
     * @return AutoEvlTxnDto
     */
    public ResponseMessage selectAutoEvlTxnByPk(EvalutionDataTxnDto evalutionDataTxnDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionDataTxnDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectAutoEvlTxnByPk(evalutionDataTxnDto));
        return responseMessage;
    }

    /**
     * 상호평가목록 조회
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage mtlEvlTxnDtoByPkList(EvalutionDataTxnDto evalutionDataTxnDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionDataTxnDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.mtlEvlTxnDtoByPkList(evalutionDataTxnDto));
        return responseMessage;
    }

    /**
     * 평가기본, 평가요청내역, 자동평가내역, 상호평가목록 조회
     *
     * @param evalDataTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage evlTotList(EvalutionDataTxnDto evalDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        // 학습과정번호별 팀일련번호 조회
        long teamSn = this.selectTeamSn(evalDataTxnDto);

        resultMap.put("teamSn", teamSn);

        if (ObjectUtils.isEmpty(evalDataTxnDto.getUqstnNo())) {
            resultMap.put(COL_EVL_PSBL_YN, Constants.USE_YN_N);
            responseMessage.setResult(resultMap);
            return responseMessage;
        }
        EvalutionBasDto evalutionBasDto = new EvalutionBasDto();
        evalutionBasDto.setTeamSn(teamSn);
        evalutionBasDto.setUqstnNo(evalDataTxnDto.getUqstnNo());
        evalutionBasDto.setLcorsNo(evalDataTxnDto.getLcorsNo());
        evalutionBasDto.setProjectNo(evalDataTxnDto.getProjectNo());

        // 미션팀 여부
        LearningDto learningDto = new LearningDto();
        learningDto.setProjectNo(evalDataTxnDto.getProjectNo());
        LearningDto projectInfo = learningMapper.selectProjectDefaultInfo(learningDto);
        resultMap.put("misTeamYn", projectInfo != null ? projectInfo.getMisTeamYn() : null);

        String evlPsblYn = Constants.USE_YN_N;
        // 단위문제 결과 PASS 확인
        // 바로 위에서 projectInfo 의 null 을 이미 인정했는데 여기서는 그대로 역참조해,
        // 프로젝트 기본정보가 없으면 아래 평가설정 가드에 닿기도 전에 NPE 로 죽었다.
        // misTeamYn 자체도 NULL 일 수 있어 상수를 앞에 두고 비교한다.
        if (Constants.USE_YN_Y.equals(projectInfo != null ? projectInfo.getMisTeamYn() : null)) {
            // 미션팀인 경우: 학습과정 유형코드 확인
            String lcorsTypeCd = evalutionMapper.selectLcorsTypeCd(
                evalDataTxnDto.getProjectNo(),
                evalDataTxnDto.getLcorsNo()
            );
            // lcors_type_cd가 '00001'일 때만(=텀프로젝트) 부여자격 확인
            if ("00001".equals(lcorsTypeCd)) {
                evlPsblYn = evalutionMapper.checkMbrHasProjectQualGrnt(
                    evalDataTxnDto.getMbrId(),
                    evalDataTxnDto.getProjectNo(),
                    evalDataTxnDto.getInstCd()
                );
            } else {
                evlPsblYn = Constants.USE_YN_Y;
            }
            // 미션팀인 경우: 리더 여부 조회
            String leadrYn = evalutionMapper.selectMbrLeadrYn(
                evalDataTxnDto.getMbrId(),
                evalDataTxnDto.getProjectNo(),
                evalDataTxnDto.getLcorsNo(),
                evalDataTxnDto.getUqstnNo(),
                teamSn
            );
            resultMap.put("leadrYn", leadrYn != null ? leadrYn : Constants.USE_YN_N);
        } else {
            evlPsblYn = this.selectEvlPsblYn(evalutionBasDto);
        }

        if (Constants.USE_YN_Y.equals(evlPsblYn)) {
            EvalutionBasDto tmpEvalutionBasDto = new EvalutionBasDto();
            EvalutionDataTxnDto tmpEvalutionDataTxnDto = new EvalutionDataTxnDto();
            AutoEvlTxnDto tmpAutoEvlDataTxnDto = new AutoEvlTxnDto();
            List<MtlEvlTxnDto> tmpMtlEvlDataTxnDto = new ArrayList<MtlEvlTxnDto>();

            // 문제번호의 자동평가, 동료평가, 전문가평가여부, 인원수 조회
            LrnCorsNmUqstnNmDto lrnCorsNmUqstnNmDto = new LrnCorsNmUqstnNmDto();
            lrnCorsNmUqstnNmDto.setKwajungStr(evalDataTxnDto.getKwajungStr());
            lrnCorsNmUqstnNmDto.setLcorsNo(evalDataTxnDto.getLcorsNo());
            lrnCorsNmUqstnNmDto.setUqstnNo(evalDataTxnDto.getUqstnNo());
            lrnCorsNmUqstnNmDto.setProjectNo(evalDataTxnDto.getProjectNo());
            LrnCorsNmUqstnNmDto resultCorsDto = (LrnCorsNmUqstnNmDto) this.selectLrnUqstnNmYN(lrnCorsNmUqstnNmDto)
                    .getResult();

            // 평가설정 조회가 0건이면 아래에서 그대로 역참조해 NPE 로 죽었다.
            // 0건이 되는 경로는 둘이다.
            //   ⑴ 화면이 학습단계를 바꾼 직후 이전 학습주제·미션 번호를 그대로 보내 조합이 어긋난 경우
            //   ⑵ 학습과정에 평가타입(PJT_LCORS_EVL_TYPE)이 없어 과정평가 분기가 비는 경우
            //
            // evlPsblYn 을 'N' 으로 내리지 않는다 — 화면이 그 값을 "이전 단위문제 PASS 후 진행 가능"
            // 안내로 해석해 사실과 다른 문구를 띄운다. 조용히 기본값(0·'N')을 채우는 것도 안 된다.
            // 설정이 없는 미션이 평가 가능한 것처럼 보이기 때문이다. 조회 실패로 분명히 응답한다.
            if (resultCorsDto == null) {
                log.warn("평가설정 조회 0건 - projectNo={}, lcorsNo={}, uqstnNo={}",
                        evalDataTxnDto.getProjectNo(), evalDataTxnDto.getLcorsNo(),
                        evalDataTxnDto.getUqstnNo());
                // result 를 비우지 않는다 — 화면은 여기서 teamSn·misTeamYn·leadrYn 을 받아
                // 팀미션·팀장 가드를 세우므로, 비우면 그 가드가 조용히 통과된다.
                // 평가는 불가하므로 evlPsblYn 은 'N' 이다.
                resultMap.put(COL_EVL_PSBL_YN, Constants.USE_YN_N);
                responseMessage.setResult(resultMap);
                responseMessage.setCode(HttpStatus.BAD_REQUEST.value());
                responseMessage.setMessage(messageSource.getMessage(
                        "usr.evreq.msg.noEvlGuide", null, LocaleContextHolder.getLocale()));
                return responseMessage;
            }

            // [#159] 동료평가 통과방식 — 누적(CUMULATIVE)이면 front 요청 버튼이 횟수 소진이 아니라
            //        진행 중 건 유무로 활성화를 판단해야 하므로 응답에 내려준다. 미설정·과정평가는 null(=연속).
            Map<String, Object> passMethodParams = new HashMap<>();
            passMethodParams.put("projectNo", evalDataTxnDto.getProjectNo());
            passMethodParams.put("lcorsNo", evalDataTxnDto.getLcorsNo());
            passMethodParams.put("uqstnNo", evalDataTxnDto.getUqstnNo());
            resultMap.put("peerPassAccmCd", passJudgmentMapper.selectPeerPassAccmCd(passMethodParams));

            // 평가기본의 마지막 차수 조회
            tmpEvalutionDataTxnDto.setTeamSn(teamSn);
            tmpEvalutionDataTxnDto.setMbrId(evalDataTxnDto.getMbrId());
            tmpEvalutionDataTxnDto.setEvLang(evalDataTxnDto.getEvLang());
            tmpEvalutionDataTxnDto.setInstCd(evalDataTxnDto.getInstCd());
            tmpEvalutionDataTxnDto.setProjectNo(evalDataTxnDto.getProjectNo());
            tmpEvalutionDataTxnDto.setLcorsNo(evalDataTxnDto.getLcorsNo());
            tmpEvalutionDataTxnDto.setUqstnNo(evalDataTxnDto.getUqstnNo());
            tmpEvalutionBasDto = (EvalutionBasDto) this.selectEvlNo(tmpEvalutionDataTxnDto).getResult();

            if (tmpEvalutionBasDto == null) {
                tmpEvalutionBasDto = new EvalutionBasDto();
                // 기존 평가기본 자료 없음
                tmpEvalutionBasDto.setEvlNo(-2);
                resultMap.put(COL_EVL_PSBL_YN, evlPsblYn);
                resultMap.put("peerEvlrNope", resultCorsDto.getPeerEvlrNope());
                resultMap.put("spcltEvlrNope", resultCorsDto.getSpcltEvlrNope());
                resultMap.put("autoEvlYn", resultCorsDto.getAutoEvlYn());
                resultMap.put("dockerEvlYn", resultCorsDto.getDockerEvlYn());
                resultMap.put("collEvlYn", resultCorsDto.getCollEvlYn());
                resultMap.put("profEvlYn", resultCorsDto.getProfEvlYn());
                resultMap.put("testEvlYn", resultCorsDto.getTestEvlYn());
                resultMap.put("prlngNm", resultCorsDto.getPrlngNm());
                resultMap.put("evalBasDto", tmpEvalutionBasDto);

                responseMessage.setResult(resultMap);
                return responseMessage;
            }
            tmpEvalutionBasDto.setEvLang(evalDataTxnDto.getEvLang());

            // 평가기본 조회
            tmpEvalutionBasDto = (EvalutionBasDto) this.selectEvlBasByPk(tmpEvalutionBasDto).getResult();

            // 평가자료 조회
            tmpEvalutionDataTxnDto.setEvlNo(tmpEvalutionBasDto.getEvlNo());
            tmpEvalutionDataTxnDto.setEvlDegr(tmpEvalutionBasDto.getEvlDegr());
            EvalutionDataTxnDto resultTxnDto = (EvalutionDataTxnDto) this.selectEvlDataTxnByPk(tmpEvalutionDataTxnDto)
                    .getResult();

            // 자동평가내역 조회
            tmpAutoEvlDataTxnDto = (AutoEvlTxnDto) this.selectAutoEvlTxnByPk(tmpEvalutionDataTxnDto).getResult();

            // 상호평가목록 조회
            tmpMtlEvlDataTxnDto = (List<MtlEvlTxnDto>) this.mtlEvlTxnDtoByPkList(tmpEvalutionDataTxnDto).getResult();

            resultMap.put("peerEvlrNope", resultCorsDto.getPeerEvlrNope());
            resultMap.put("spcltEvlrNope", resultCorsDto.getSpcltEvlrNope());
            resultMap.put("autoEvlYn", resultCorsDto.getAutoEvlYn());
            resultMap.put("dockerEvlYn", resultCorsDto.getDockerEvlYn());
            resultMap.put("collEvlYn", resultCorsDto.getCollEvlYn());
            resultMap.put("profEvlYn", resultCorsDto.getProfEvlYn());
            resultMap.put("testEvlYn", resultCorsDto.getTestEvlYn());
            resultMap.put("prlngNm", resultCorsDto.getPrlngNm());
            resultMap.put("evalBasDto", tmpEvalutionBasDto);
            resultMap.put("evalDataTxnDto", resultTxnDto);
            resultMap.put("autoEvlDataTxnDto", tmpAutoEvlDataTxnDto);
            resultMap.put("mtlEvlDataTxnDtoList", tmpMtlEvlDataTxnDto);
        }

        resultMap.put(COL_EVL_PSBL_YN, evlPsblYn);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 학습자 검색 - 평가 상세 조회 (조회 전용, 민감정보 마스킹)
     * 기존 evlTotList와 동일한 구조이나 상호평가 목록에서 개인정보를 마스킹
     *
     * @param evalDataTxnDto 조회 대상 파라미터
     * @return ResponseMessage (민감정보 마스킹된 응답)
     */
    @SuppressWarnings("unchecked")
    public ResponseMessage evlDetailForView(EvalutionDataTxnDto evalDataTxnDto) {
        // 기존 evlTotList 로직으로 평가 기본 정보 조회
        ResponseMessage responseMessage = evlTotList(evalDataTxnDto);

        // 결과에서 상호평가 목록을 마스킹된 버전으로 교체
        if (responseMessage.getResult() instanceof Map) {
            Map<String, Object> resultMap = (Map<String, Object>) responseMessage.getResult();

            // mtlEvlDataTxnDtoList가 있는 경우에만 마스킹된 데이터로 교체
            if (resultMap.containsKey("mtlEvlDataTxnDtoList")) {
                String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko";
                evalDataTxnDto.setLanguage(language);

                // evlNo, evlDegr를 평가기본 정보에서 가져와서 설정
                Object evalBasObj = resultMap.get("evalBasDto");
                if (evalBasObj instanceof EvalutionBasDto) {
                    EvalutionBasDto evalBasDto = (EvalutionBasDto) evalBasObj;
                    evalDataTxnDto.setEvlNo(evalBasDto.getEvlNo());
                    evalDataTxnDto.setEvlDegr(evalBasDto.getEvlDegr());
                }

                List<MtlEvlTxnDto> maskedList = evalutionMapper.mtlEvlTxnDtoByPkListForView(evalDataTxnDto);
                resultMap.put("mtlEvlDataTxnDtoList", maskedList);
            }
        }

        return responseMessage;
    }

    /**
     * git 주소 선택할 멤버 조회
     *
     * @param lcorsMbrPtpTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage selectGitAddrMbrIdsList(LcorsMbrPtpTxnDto lcorsMbrPtpTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectGitAddrMbrIdsList(lcorsMbrPtpTxnDto));
        return responseMessage;
    }

    /**
     * 평가자 지정 : 학습과정별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return ResponseMessage
     */
    public ResponseMessage selectLcorsEvlStdList(EvalutionStdDto evalutionStdDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionStdDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectLcorsEvlStdList(evalutionStdDto));
        return responseMessage;
    }

    /**
     * 평가자 지정 : 단위문제별 평가기준 조회
     *
     * @param evalutionStdDto
     * @return ResponseMessage
     */
    public ResponseMessage selectUqstnEvlStdList(EvalutionStdDto evalutionStdDto) {

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionStdDto.setLanguage(language);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectUqstnEvlStdList(evalutionStdDto));
        return responseMessage;
    }

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    public ResponseMessage selectCase1MbrIdList(EvalutionMbrDto evalutionMbrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();
        List<EvalutionMbrDto> resultList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(evalutionMbrDto.getMonDay(), formatter);
        LocalDate endDate = LocalDate.parse(evalutionMbrDto.getSunDay(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EvalutionMbrDto paramDto = new EvalutionMbrDto();
            paramDto.setEvlPsblYmd(betweenDateList.get(i).toString());
            paramDto.setInstCd(evalutionMbrDto.getInstCd());
            paramDto.setProjectNo(evalutionMbrDto.getProjectNo());
            paramDto.setLcorsNo(evalutionMbrDto.getLcorsNo());
            paramDto.setUqstnNo(evalutionMbrDto.getUqstnNo());
            paramDto.setTeamSn(evalutionMbrDto.getTeamSn());
            paramDto.setCn(evalutionMbrDto.getCn());
            paramDto.setMbrId(evalutionMbrDto.getMbrId());
            resultMap.put(COL_LIST + i, evalutionMapper.selectCase1MbrIdList(paramDto));
        }

        // resultList로 resultMap에 있는 List 전부 옮김
        for (int i = 0; i < resultMap.size(); i++) {
            List<EvalutionMbrDto> tmpList = (List<EvalutionMbrDto>) resultMap.get(COL_LIST + i);
            for (int j = 0; j < tmpList.size(); j++) {
                resultList.add(tmpList.get(j));
            }
        }

        responseMessage.setResult(resultList.stream().distinct().collect(Collectors.toList())); // 중복제거
        return responseMessage;
    }

    /**
     * 평가자 지정 : 동료평가자 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    public ResponseMessage selectCase2MbrIdList(EvalutionMbrDto evalutionMbrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();
        List<EvalutionMbrDto> resultList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(evalutionMbrDto.getMonDay(), formatter);
        LocalDate endDate = LocalDate.parse(evalutionMbrDto.getSunDay(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EvalutionMbrDto paramDto = new EvalutionMbrDto();
            paramDto.setEvlPsblYmd(betweenDateList.get(i).toString());
            paramDto.setInstCd(evalutionMbrDto.getInstCd());
            paramDto.setProjectNo(evalutionMbrDto.getProjectNo());
            paramDto.setLcorsNo(evalutionMbrDto.getLcorsNo());
            paramDto.setUqstnNo(evalutionMbrDto.getUqstnNo());
            paramDto.setTeamSn(evalutionMbrDto.getTeamSn());
            paramDto.setCn(evalutionMbrDto.getCn());
            resultMap.put(COL_LIST + i, evalutionMapper.selectCase2MbrIdList(paramDto));
        }

        // resultList로 resultMap에 있는 List 전부 옮김
        for (int i = 0; i < resultMap.size(); i++) {
            List<EvalutionMbrDto> tmpList = (List<EvalutionMbrDto>) resultMap.get(COL_LIST + i);
            for (int j = 0; j < tmpList.size(); j++) {
                resultList.add(tmpList.get(j));
            }
        }

        responseMessage.setResult(resultList.stream().distinct().collect(Collectors.toList())); // 중복제거
        return responseMessage;
    }

    /**
     * 평가자 지정 : 평가자 대상범위가 학습과정 수강자 일 경우 동료 평가자 평가가능시간 목록 조회
     *
     * @param enbldEvlTimeDto
     * @return ResponseMessage
     */
    public ResponseMessage selectCase1EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(enbldEvlTimeDto.getMonDay(), formatter);
        LocalDate endDate = LocalDate.parse(enbldEvlTimeDto.getSunDay(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);
        log.info("betweenDateList : " + betweenDateList);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EnbldEvlTimeDto paramDto = new EnbldEvlTimeDto();
            paramDto.setEvlPsblYmd(betweenDateList.get(i).toString());
            paramDto.setInstCd(enbldEvlTimeDto.getInstCd());
            paramDto.setProjectNo(enbldEvlTimeDto.getProjectNo());
            paramDto.setLcorsNo(enbldEvlTimeDto.getLcorsNo());
            paramDto.setUqstnNo(enbldEvlTimeDto.getUqstnNo());
            paramDto.setTeamSn(enbldEvlTimeDto.getTeamSn());
            paramDto.setCn(enbldEvlTimeDto.getCn());
            paramDto.setMbrId(enbldEvlTimeDto.getMbrId());
            paramDto.setMbrIds(enbldEvlTimeDto.getMbrIds());
            resultMap.put("day" + (i + 1), evalutionMapper.selectCase1EnbldEvlTimeList(paramDto));
        }

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가자 지정 : 평가자 대상범위가 학습자 회원 일 경우 동료 평가자 평가가능시간 목록 조회
     *
     * @param enbldEvlTimeDto
     * @return ResponseMessage
     */
    public ResponseMessage selectCase2EnbldEvlTimeList(EnbldEvlTimeDto enbldEvlTimeDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(enbldEvlTimeDto.getMonDay(), formatter);
        LocalDate endDate = LocalDate.parse(enbldEvlTimeDto.getSunDay(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EnbldEvlTimeDto paramDto = new EnbldEvlTimeDto();
            paramDto.setEvlPsblYmd(betweenDateList.get(i).toString());
            paramDto.setInstCd(enbldEvlTimeDto.getInstCd());
            paramDto.setProjectNo(enbldEvlTimeDto.getProjectNo());
            paramDto.setLcorsNo(enbldEvlTimeDto.getLcorsNo());
            paramDto.setUqstnNo(enbldEvlTimeDto.getUqstnNo());
            paramDto.setTeamSn(enbldEvlTimeDto.getTeamSn());
            paramDto.setCn(enbldEvlTimeDto.getCn());
            paramDto.setMbrId(enbldEvlTimeDto.getMbrId());
            paramDto.setMbrIds(enbldEvlTimeDto.getMbrIds());
            resultMap.put("day" + (i + 1), evalutionMapper.selectCase2EnbldEvlTimeList(paramDto));
        }

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 자동평가 요청 전 이전평가 결과 fail인경우 재평가 기간 조회
     *
     * @param reEvalChkDto
     * @return ResponseMessage
     */
    public ResponseMessage selectReEvalTimeList(ReEvalChkDto reEvalChkDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectReEvalTimechk(reEvalChkDto));
        return responseMessage;
    }

    /**
     * 동료평가 슬롯 조회 (시간+인원수만 반환, 개인정보 미노출)
     *
     * @param peerEvlSlotDto  instCd, projectNo, lcorsNo, uqstnNo, teamSn, cn, mbrId, monDay, sunDay
     * @param scopeCd         평가자 대상범위 코드 ("00001" = 학습과정 수강자, "00002" = 전체 수강생)
     * @return ResponseMessage { day1: [...], day2: [...], ... day7: [...] }
     */
    public ResponseMessage selectPeerEvlSlotList(PeerEvlSlotDto peerEvlSlotDto, String scopeCd) {
        ResponseMessage responseMessage = new ResponseMessage();
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(peerEvlSlotDto.getMonDay(), formatter);
        LocalDate endDate = LocalDate.parse(peerEvlSlotDto.getSunDay(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            PeerEvlSlotDto paramDto = new PeerEvlSlotDto();
            paramDto.setEvlPsblYmd(betweenDateList.get(i).toString());
            paramDto.setInstCd(peerEvlSlotDto.getInstCd());
            paramDto.setProjectNo(peerEvlSlotDto.getProjectNo());
            paramDto.setLcorsNo(peerEvlSlotDto.getLcorsNo());
            paramDto.setUqstnNo(peerEvlSlotDto.getUqstnNo());
            paramDto.setTeamSn(peerEvlSlotDto.getTeamSn());
            paramDto.setCn(peerEvlSlotDto.getCn());
            paramDto.setMbrId(peerEvlSlotDto.getMbrId());
            paramDto.setMbrIds(peerEvlSlotDto.getMbrIds());

            if ("00001".equals(scopeCd)) {
                resultMap.put("day" + (i + 1), evalutionMapper.selectPeerEvlSlotCase1(paramDto));
            } else {
                resultMap.put("day" + (i + 1), evalutionMapper.selectPeerEvlSlotCase2(paramDto));
            }
        }

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 자동평가 생성
     *
     * @param evalutionDataTxnDto
     * @param member 로그인 사용자 — 요청 파라미터의 회원ID 대신 이 값으로 소유권을 판정하고 기록한다
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage reqAutoEvalList(EvalutionDataTxnDto evalutionDataTxnDto, Member member) {

        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기

        // 대상 차수 조회 + 소유권 검증 — 어떤 쓰기보다 먼저 한다.
        EvalutionBasDto targetKey = new EvalutionBasDto();
        targetKey.setEvlNo(evalutionDataTxnDto.getEvlNo());
        targetKey.setEvlDegr(evalutionDataTxnDto.getEvlDegr());
        targetKey.setLanguage(language);
        validateAutoEvlOwnership(member, evalutionMapper.selectEvlBasByPk(targetKey));

        RegReqEvalDto regReqEvalDto = new RegReqEvalDto();

        // 평가 기본 수정 — 자동평가 이력보다 먼저 한다.
        // 평가번호·차수가 요청 파라미터로 그대로 들어오므로 통과 차수를 지정하면 통과 기록이 지워졌다.
        // 매퍼가 통과 차수를 제외하니 0건이면 대상이 아닌 차수를 지정한 것이라 여기서 멈춘다.
        // (이력을 먼저 넣으면 롤백에 기대야 한다)
        regReqEvalDto.setEvlStusCd(Constants.EVL_STUS_CD_2);
        regReqEvalDto.setMbrId(member.getMbrId());
        regReqEvalDto.setEvlNo(evalutionDataTxnDto.getEvlNo());
        regReqEvalDto.setEvlDegr(evalutionDataTxnDto.getEvlDegr());
        if (evalutionMapper.updateEvlBasAfterAutoEvlReq(regReqEvalDto) == 0) {
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.msg.autoevlnottarget", null, LocaleContextHolder.getLocale()));
        }

        // 자동평가 생성 후, 배치에서 처리
        AutoEvlTxnDto autoEvlTxn = new AutoEvlTxnDto();
        autoEvlTxn.setEvlNo(evalutionDataTxnDto.getEvlNo());
        autoEvlTxn.setEvlDegr(String.valueOf(evalutionDataTxnDto.getEvlDegr()));
        autoEvlTxn.setAutoEvlStusCd(Constants.WAIT);
        autoEvlTxn.setRgtrId(member.getMbrId());
        autoEvlTxn.setMdfrId(member.getMbrId());
        evalutionMapper.insertAutoEvlTxn(autoEvlTxn);

        // 평가 기본 조회
        EvalutionBasDto evalutionBasDto = new EvalutionBasDto();
        evalutionBasDto.setEvlNo(evalutionDataTxnDto.getEvlNo());
        evalutionBasDto.setEvlDegr(evalutionDataTxnDto.getEvlDegr());
        evalutionBasDto.setEvLang(evalutionDataTxnDto.getEvLang());
        evalutionBasDto.setLanguage(language);
        EvalutionBasDto evalBas = evalutionMapper.selectEvlBasByPk(evalutionBasDto);

        resultMap.put("evlMdfcnDt", evalBas.getMdfcnDt());
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 자동평가 요청 대상 차수가 요청자 본인의 미션인지 확인한다.
     *
     * 이 API 는 평가번호·차수·회원ID 를 요청 파라미터로 그대로 받는다. 값이 유효하기만 하면
     * 남의 평가차수도 지정되므로(진행중·FAIL 차수를 진행중으로 리셋하고 자동평가 이력을 꽂을 수 있다),
     * 대상 EVL_BAS 행에서 미션 키를 되읽어 소속을 확인한다.
     * 판정은 #220 의 countTeamMembership 을 그대로 쓴다 — 미션팀·일반 프로젝트 분기의 SSOT 다.
     */
    private void validateAutoEvlOwnership(Member member, EvalutionBasDto targetBas) {
        if (targetBas == null) {
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.msg.autoevlnottarget", null, LocaleContextHolder.getLocale()));
        }

        // 학습과정 단위 평가(9999999)는 팀원 테이블에 대응 행이 없어 소속을 물을 수 없다 —
        // validateTeamMembership 과 같은 예외 처리를 따른다.
        if (targetBas.getProjectNo() == null || targetBas.getLcorsNo() == null
                || targetBas.getUqstnNo() == null || targetBas.getTeamSn() == null
                || Long.valueOf(9999999L).equals(targetBas.getUqstnNo())) {
            return;
        }

        int membership = evaluationAppendMapper.countTeamMembership(
                targetBas.getProjectNo().longValue(),
                targetBas.getLcorsNo().longValue(),
                targetBas.getUqstnNo(),
                targetBas.getTeamSn().intValue(),
                member.getMbrId(),
                member.getInstCd());

        if (membership == 0) {
            log.warn("자동평가 요청 소속 불일치 차단 evlNo={} evlDegr={} projectNo={} lcorsNo={} uqstnNo={} teamSn={}",
                    targetBas.getEvlNo(), targetBas.getEvlDegr(), targetBas.getProjectNo(),
                    targetBas.getLcorsNo(), targetBas.getUqstnNo(), targetBas.getTeamSn());
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.msg.autoevlnotowner", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * 현재 총 포인트
     *
     * @param exprPointDto
     * @return ResponseMessage
     */
    public ResponseMessage selectPointSum(ExprPointDto exprPointDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResult(evalutionMapper.selectPointSum(exprPointDto));
        return responseMessage;
    }

    /**
     * 포인트 총계 정보 연계(평가자 지정 popup 화면에서 평가 요청 하기 전 확인)
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage apiCmmnGetPointNowScr(EvalutionDataTxnDto evalutionDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        ApiCmmnPoint apiCmmnPoint = new ApiCmmnPoint();

        String rtnStr = "", mbrId = "", lrnrNm = "", returnCode = "", tmpStr = "";

        int i = 0, j = 0;

        CrtrScrDto crtrScrDto = new CrtrScrDto();
        crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
        crtrScrDto.setCrtrTypeNo(11L);
        crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);

        // 차감할 포인트 ex) -100
        Float fl1 = evalutionMapper.selectPointNowScr(crtrScrDto) * -1;
        Float fl2 = 0.f;

        // 팀원 전체 목록 조회
        List<EvalutionDataTxnDto> rtnList = evalutionMapper.apiCmmnGetPointNowScr(evalutionDataTxnDto);

        if (rtnList.size() > 0) {
            ExprPointDto exprPointDto = new ExprPointDto();

            for (i = 0; i < rtnList.size(); i++) {
                exprPointDto.setMbrId(rtnList.get(i).getMbrId());
                exprPointDto.setInstCd(evalutionDataTxnDto.getInstCd());
                lrnrNm = rtnList.get(i).getLrnrNm();

                ExprPointDto resultDto = evalutionMapper.selectPointSum(exprPointDto);

                if (ObjectUtils.isEmpty(resultDto.getPointScore())) {
                    apiCmmnPoint.setMemNames(returnCode);
                    responseMessage.setResult(apiCmmnPoint);
                    return responseMessage;
                }

                fl2 = Float.parseFloat(resultDto.getPointScore().toString());

                // 차감 포인트 점수가 부족한 팀원 수집
                if (fl1 > fl2) {
                    tmpStr = (j == 0) ? "" : ", ";
                    rtnStr += tmpStr + lrnrNm;
                    j++;
                }

                apiCmmnPoint.setMemNames(rtnStr);
                apiCmmnPoint.setPointScore(fl2);
            }
        } else {
            apiCmmnPoint.setMemNames(StringUtils.EMPTY);
        }

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("apiCmmnPoint", apiCmmnPoint);
        resultMap.put("teamMember", rtnList);

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 동료 평가자 지정 등록
     *
     * @param regReqEvalDto
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage regReqEvalSave(RegReqEvalDto regReqEvalDto, Member member) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        EvalutionBasDto evalutionBasDto = new EvalutionBasDto();

        lockTeamMission(regReqEvalDto);

        int getReservedEvaluationRequestCount = evalutionMapper.getReservedEvaluationRequestCount(regReqEvalDto);

        if (getReservedEvaluationRequestCount > 0) {
            responseMessage.setCode(400);
            responseMessage.setMessage("중복된 요청입니다. 화면을 새로고침 하여 다시 진행해주세요.");
            return responseMessage;
        }

        // 1. 평가기본이 있으면 평가번호/평가차수 가져오기, 평가기본이 없는 경우 INSERT
        List<RegReqEvalDto> regReqEvalDtoList = evalutionMapper.selectEvlBas(regReqEvalDto);

        if (regReqEvalDtoList.size() == 0) {
            // 평가 기본 등록
            evalutionBasDto.setProjectNo(regReqEvalDto.getProjectNo());
            evalutionBasDto.setLcorsNo(regReqEvalDto.getLcorsNo());
            evalutionBasDto.setUqstnNo(regReqEvalDto.getUqstnNo() == null ? 9999999L : regReqEvalDto.getUqstnNo());
            evalutionBasDto.setTeamSn(regReqEvalDto.getTeamSn());
            evalutionBasDto.setMbrId(regReqEvalDto.getMbrId());

            // 평가 기본 등록
            evalutionMapper.insertNewEvlBas(evalutionBasDto);

            // 평가 기본을 다시 찾아 온다.
            regReqEvalDtoList = evalutionMapper.selectEvlBas(regReqEvalDto);

            // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
            if (evalutionBasDto.getUqstnNo() != null && evalutionBasDto.getUqstnNo() != 9999999L) {
                UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                uqstnTeamUpdate.setProjectNo((long) evalutionBasDto.getProjectNo());
                uqstnTeamUpdate.setLcorsNo((long) evalutionBasDto.getLcorsNo());
                uqstnTeamUpdate.setUqstnNo(evalutionBasDto.getUqstnNo());
                uqstnTeamUpdate.setMbrId(evalutionBasDto.getMbrId());
                uqstnTeamUpdate.setTeamSn(Math.toIntExact(evalutionBasDto.getTeamSn()));
                uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(regReqEvalDtoList.get(0).getEvlNo()));
                learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);
            }

            regReqEvalDto.setEvlNo(regReqEvalDtoList.get(0).getEvlNo());
            regReqEvalDto.setEvlDegr(regReqEvalDtoList.get(0).getEvlDegr());
        } else {
            if (Constants.EVL_RESLT_CD_FAIL.equals(regReqEvalDtoList.get(0).getEvlResltCd())) {
                // 조회 결과 EVL_RESLT_CD = '00002'(FAIL) 이면
                // EVL_BAS(평가기본) INSERT (평가차수 = MAX + 1)
                evalutionBasDto.setEvlNo(regReqEvalDtoList.get(0).getEvlNo());
                evalutionBasDto.setEvlDegr(regReqEvalDtoList.get(0).getEvlDegr());
                evalutionBasDto.setProjectNo(regReqEvalDto.getProjectNo());
                evalutionBasDto.setLcorsNo(regReqEvalDto.getLcorsNo());
                evalutionBasDto.setUqstnNo(regReqEvalDto.getUqstnNo() == null ? 9999999L : regReqEvalDto.getUqstnNo());
                evalutionBasDto.setTeamSn(regReqEvalDto.getTeamSn());
                evalutionBasDto.setMbrId(regReqEvalDto.getMbrId());

                // 평가 기본 차수 증가하여 등록
                evalutionMapper.insertEvlBasEvlDegr(evalutionBasDto);

                // 평가 기본을 다시 찾아 온다.
                regReqEvalDtoList = evalutionMapper.selectEvlBas(regReqEvalDto);

                // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
                if (evalutionBasDto.getUqstnNo() != null && evalutionBasDto.getUqstnNo() != 9999999L) {
                    UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                    uqstnTeamUpdate.setProjectNo((long) evalutionBasDto.getProjectNo());
                    uqstnTeamUpdate.setLcorsNo((long) evalutionBasDto.getLcorsNo());
                    uqstnTeamUpdate.setUqstnNo(evalutionBasDto.getUqstnNo());
                    uqstnTeamUpdate.setMbrId(evalutionBasDto.getMbrId());
                    uqstnTeamUpdate.setTeamSn(Math.toIntExact(evalutionBasDto.getTeamSn()));
                    uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(regReqEvalDtoList.get(0).getEvlNo()));
                    learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);
                }

            } else if (Constants.EVL_RESLT_CD_PASS.equals(regReqEvalDtoList.get(0).getEvlResltCd())) {
                /* 조회 결과가 EVL_RESLT_CD = '00001'(PASS) 이면오류 */
                regReqEvalDto.setEvlNo(-1);
                responseMessage.setResult(regReqEvalDto);
                return responseMessage;
            }

            regReqEvalDto.setEvlNo(regReqEvalDtoList.get(0).getEvlNo());
            regReqEvalDto.setEvlDegr(regReqEvalDtoList.get(0).getEvlDegr());
        }

        Long nowTime = System.currentTimeMillis();

        SimpleDateFormat timeFormat = new SimpleDateFormat(REGEX_DT_2);
        String mdfcnDt = timeFormat.format(new Date(nowTime));

        // 2. 상호평가 신규 입력
        regReqEvalDto.setMdfcnDt(mdfcnDt);
        regReqEvalDto.setEvlScdlSn(regReqEvalDto.getItemSnList().get(0).toString());
        evalutionMapper.insertNewMtlEvlTxn(regReqEvalDto);

        regReqEvalDtoList = evalutionMapper.selectNewMtlEvlTxn(regReqEvalDto);

        regReqEvalDto.setMtlEvlSn(regReqEvalDtoList.get(0).getMtlEvlSn());

        // 3. 평가가능일정 테이블의 해당 일정의 SCDL_STUS_CD(일정상태코드) = '00002'로 변경 (00001:등록, 00002:확정,
        // 00003:취소)
        evalutionMapper.updateEvlPsblScdlBas(regReqEvalDto);

        String msg1 = messageSource.getMessage("usr.evreq.msg.regsched1", null, LocaleContextHolder.getLocale());
        String msg2 = messageSource.getMessage("usr.evreq.msg.regsched2", null, LocaleContextHolder.getLocale());
        String msg3 = messageSource.getMessage("usr.evreq.msg.regsched3", null, LocaleContextHolder.getLocale());
        String msg4 = messageSource.getMessage("usr.evreq.msg.regsched4", null, LocaleContextHolder.getLocale());
        String msg5 = messageSource.getMessage("usr.evreq.msg.regsched5", null, LocaleContextHolder.getLocale());
        String msg6 = messageSource.getMessage("usr.evreq.msg.regsched6", null, LocaleContextHolder.getLocale());
        String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null, LocaleContextHolder.getLocale());

        // 4. 알림게시판기본에 INSERT 처리
        String pstartCn = msg1 + HTML_BR +
                msg2 + HTML_BR +
                msg3 + HTML_BR +
                msg7 + " " + regReqEvalDto.getMtlEvlPamBgngDt() + HTML_BR +
                msg4 + " " + regReqEvalDto.getProjectNm() + HTML_BR +
                msg5 + " " + regReqEvalDto.getLcorsNoNm() + HTML_BR +
                msg6 + " " + regReqEvalDto.getUqstnNoNm();

        String pstartTitlNm = messageSource.getMessage("evalreq.msg.evldesnted.title", null,
                LocaleContextHolder.getLocale());
        regReqEvalDto.setPstartTitlNm(pstartTitlNm);
        regReqEvalDto.setPstartCn(pstartCn);
        regReqEvalDto.setNtcDivCd(Constants.NTC_DIV_CD_8);
        regReqEvalDto.setMdfcnDt(mdfcnDt);
        regReqEvalDto.setSysDivCd(Constants.SYS_DIV_CD_1);
        // evalutionMapper.insertNtcBbsBasFromEvl(regReqEvalDto);

        // 평가자 정보 구하기
        LcorsBasDto lcorsBasDto = new LcorsBasDto();
        lcorsBasDto.setInstCd(regReqEvalDto.getEvlInstCd());
        lcorsBasDto.setMbrId(regReqEvalDto.getEvlMbrId());
        List<LcorsBasDto> mbrInfos = evalutionMapper.selectLpMbrBasList(lcorsBasDto);

        resultMap.put("regReqEvalDto", regReqEvalDto);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    private void lockTeamMission(RegReqEvalDto regReqEvalDto) {
        if (regReqEvalDto.getProjectNo() == null
                || regReqEvalDto.getLcorsNo() == null
                || regReqEvalDto.getUqstnNo() == null
                || regReqEvalDto.getTeamSn() == null
                || Long.valueOf(9999999L).equals(regReqEvalDto.getUqstnNo())) {
            return;
        }

        evaluationAppendMapper.lockTeamMissionForUpdate(
                regReqEvalDto.getProjectNo().longValue(),
                regReqEvalDto.getLcorsNo().longValue(),
                regReqEvalDto.getUqstnNo(),
                Math.toIntExact(regReqEvalDto.getTeamSn()));
    }

    /**
     * 선택된 멤버(학습자)의 git 주소 api 호출
     *
     * @param gitlabPrjCreator
     * @return ResponseMessage
     */
    public ResponseMessage apiListProjectList(GitlabPrjCreator gitlabPrjCreator) {
        ResponseMessage responseMessage = new ResponseMessage();

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(COL_USER_NAME, gitlabPrjCreator.getUserName());
        hashMap.put("page", gitlabPrjCreator.getPage());
        hashMap.put("size", gitlabPrjCreator.getSize());
        hashMap.put("searchType", "projectURL"); // 240109 just2go : GitLab 프로젝트 검색 조건 변경
        hashMap.put("searchText", gitlabPrjCreator.getUserName());

        JSONObject data = new JSONObject(hashMap);
        String apiUrl = Constants.GITLAB_REPOSITORY;

        JSONObject sendJsonObject = apiUtil.callRestApi(data, apiUrl);
        String returnCode = (String) sendJsonObject.get(COL_RETURN_CODE);

        if (!sendJsonObject.containsKey(COL_RET_CODE)
                || !Constants.API_RESPONSE_CD_SUCCESS.equals(sendJsonObject.get(COL_RET_CODE))) {
            throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.gitlabpjtlisterror", null,
                    LocaleContextHolder.getLocale()));
        }

        Map<String, Object> dataMap = (Map<String, Object>) sendJsonObject.get(COL_RESULT);

        responseMessage.setResult(dataMap.get("data"));
        return responseMessage;
    }

    /**
     * 평가 자료 등록
     *
     * @param evalutionDataTxnDto
     * @return
     */
    public ResponseMessage evlDataTxnByUqstnNoSave(EvalutionDataTxnDto evalutionDataTxnDto)
            throws UnsupportedEncodingException {

        evalutionDataTxnDto.setDgnDataFileNm(FileStringUtils.replace(evalutionDataTxnDto.getDgnDataFileNm()));
        evalutionDataTxnDto.setTestPresentnFileNm(FileStringUtils.replace(evalutionDataTxnDto.getTestPresentnFileNm()));

        ResponseMessage responseMessage = new ResponseMessage();
        EvalutionBasDto tmpDto = new EvalutionBasDto();
        EvalutionDataTxnDto tmpEvalDataTxnDto = new EvalutionDataTxnDto();

        if (evalutionDataTxnDto.getUqstnNo() == null) {
            evalutionDataTxnDto.setUqstnNo(9999999L);
        }

        // ai 코드유사도를 위한 첫번째 평가자료 조회
        EvalutionDataTxnDto firstEvlDataTxn = evalutionMapper.selectFirstEvlDataTxn(evalutionDataTxnDto);

        int rtn = evalutionMapper.evlDataTxnByUqstnNoSave(evalutionDataTxnDto);

        tmpDto.setEvlNo(evalutionDataTxnDto.getEvlNo());
        tmpDto.setEvlDegr(evalutionDataTxnDto.getEvlDegr());
        tmpDto.setMbrId(evalutionDataTxnDto.getMbrId());
        tmpDto.setProjectNo(evalutionDataTxnDto.getProjectNo());
        tmpDto.setLcorsNo(evalutionDataTxnDto.getLcorsNo());
        tmpDto.setUqstnNo(evalutionDataTxnDto.getUqstnNo());
        tmpDto.setTeamSn(evalutionDataTxnDto.getTeamSn());

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        tmpDto.setLanguage(language);

        tmpDto = evalutionMapper.selectEvlBasByPk(tmpDto);

        tmpDto.setMbrId(evalutionDataTxnDto.getMbrId());

        if (rtn != 1) {
            responseMessage.setResult(-9);
        } else {
            tmpEvalDataTxnDto = evalutionMapper.selectMtlEvlTxnDataRegSn(evalutionDataTxnDto);

            tmpDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());

            if (Constants.EVL_STUS_CD_3.equals(tmpDto.getEvlStusCd())
                    && Constants.EVL_RESLT_CD_FAIL.equals(tmpDto.getEvlResltCd())) {
                EvalutionBasDto tmpBasDto = new EvalutionBasDto();

                // 평가 차수 증가 평가기본 insert
                rtn = evalutionMapper.insertEvlBasEvlDegr(tmpDto);

                // EVL_BAS refetch
                tmpBasDto = evalutionMapper.selectEvlNo(evalutionDataTxnDto);
                tmpDto.setEvlStusCd(Constants.EVL_STUS_CD_1);
                tmpDto.setEvlResltCd(null);
                tmpDto.setEvlDegr(tmpBasDto.getEvlDegr());
                rtn = evalutionMapper.updateEvlBasDataRegSn(tmpDto);
            } else if (Constants.EVL_STUS_CD_1.equals(tmpDto.getEvlStusCd())
                    || Constants.EVL_STUS_CD_2.equals(tmpDto.getEvlStusCd())) {
                rtn = evalutionMapper.updateEvlBasDataRegSn(tmpDto);
            } else {
                rtn = -8;
            }

            // 20260323 ai 코드 유사도 판별
            if (evalutionDataTxnDto.getSrccdUrlAddr() != null && evalutionDataTxnDto.getSrccdUrlAddr().length() > 0 && evalutionDataTxnDto.getBrnchNm() != null && evalutionDataTxnDto.getBrnchNm().length() > 0) {

                Member learnerMbr = memberMapper.getMember(evalutionDataTxnDto.getMbrId());
                String learnerREADME = fetchReadmeContent(learnerMbr, evalutionDataTxnDto.getSrccdUrlAddr(), evalutionDataTxnDto.getBrnchNm());

                if (learnerREADME != null && learnerREADME.length() > 0) {
                    AiApiQueueDto aiApiQueueDto = new AiApiQueueDto();
                    if (firstEvlDataTxn == null) {
                        String apiParam = buildApiParam("", learnerREADME);
                        aiApiQueueDto.setApiParam(apiParam);
                        aiApiQueueDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());
                        aiApiQueueDto.setRegId(evalutionDataTxnDto.getMbrId());
                        evalutionMapper.insertAiApiQueue(aiApiQueueDto);

                    } else {
                        Member standardMbr = memberMapper.getMember(firstEvlDataTxn.getRgtrId());
                        String standardREADME = fetchReadmeContent(standardMbr, firstEvlDataTxn.getSrccdUrlAddr(), firstEvlDataTxn.getBrnchNm());

                        if (standardREADME != null && standardREADME.length() > 0) {
                            String apiParam = buildApiParam(standardREADME, learnerREADME);
                            aiApiQueueDto.setApiParam(apiParam);
                            aiApiQueueDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());
                            aiApiQueueDto.setRegId(evalutionDataTxnDto.getMbrId());
                            evalutionMapper.insertAiApiQueue(aiApiQueueDto);
                        }
                    }
                }


            }


            // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
            if (rtn > 0 && tmpDto.getUqstnNo() != null && tmpDto.getUqstnNo() != 9999999L) {
                // 최신 evl_no 조회
                EvalutionBasDto latestEvlBas = evalutionMapper.selectEvlNo(evalutionDataTxnDto);
                UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                uqstnTeamUpdate.setProjectNo((long) tmpDto.getProjectNo());
                uqstnTeamUpdate.setLcorsNo((long) tmpDto.getLcorsNo());
                uqstnTeamUpdate.setUqstnNo(tmpDto.getUqstnNo());
                uqstnTeamUpdate.setMbrId(tmpDto.getMbrId());
                uqstnTeamUpdate.setTeamSn(Math.toIntExact(tmpDto.getTeamSn()));
                uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(latestEvlBas.getEvlNo()));
                learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);
            }
        }
        responseMessage.setResult(rtn);
        return responseMessage;
    }

    /**
     * 평가기본, 평가자료내역 신규 insert
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public ResponseMessage newEvlBasToDataTxnByUqstnNoSave(EvalutionDataTxnDto evalutionDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기

        // 팀정보 조회
        UqstnTeam uqstnTeamDto = new UqstnTeam();
        uqstnTeamDto.setInstCd(evalutionDataTxnDto.getInstCd());
        uqstnTeamDto.setMbrId(evalutionDataTxnDto.getMbrId());
        uqstnTeamDto.setProjectNo((long) evalutionDataTxnDto.getProjectNo());
        uqstnTeamDto.setLcorsNo((long) evalutionDataTxnDto.getLcorsNo());
        uqstnTeamDto.setUqstnNo((long) evalutionDataTxnDto.getUqstnNo());
        uqstnTeamDto.setLanguage(language);

        UqstnTeam uqstnTeam = learningMapper.selectUqstnTeam(uqstnTeamDto);

        if (uqstnTeam == null) {
            // 1. 팀정보가 없는 경우
            responseMessage.setCode(HttpStatus.BAD_REQUEST.value());
            responseMessage.setResult("fail");
            responseMessage.setMessage("팀미션입니다. 팀정보가 없어 자료등록이 불가합니다.");
        } else if (uqstnTeam.getLeadrYn().equals(Constants.USE_YN_N)) {
            // 2. 팀미션에 자료 등록 권한이 없는 경우(팀장이 아닌 경우)
            responseMessage.setCode(HttpStatus.BAD_REQUEST.value());
            responseMessage.setResult("fail");
            responseMessage.setMessage("팀미션에 자료등록 권한이 없습니다. (팀장만 가능)");
        } else {
            EvalutionBasDto tmpDto = new EvalutionBasDto();
            EvalutionDataTxnDto tmpEvalDataTxnDto = new EvalutionDataTxnDto();

            evalutionDataTxnDto.setTeamSn(Long.valueOf(uqstnTeam.getTeamSn()));
            if (evalutionDataTxnDto.getUqstnNo() == null) {
                evalutionDataTxnDto.setUqstnNo(9999999L);
            }

            // #67: 팀미션이면 미션행을 선점(FOR UPDATE)해 동시 자료등록의 중복 EVL_BAS 채번을 직렬화
            if (!Long.valueOf(9999999L).equals(evalutionDataTxnDto.getUqstnNo())
                    && evalutionDataTxnDto.getTeamSn() != null) {
                evaluationAppendMapper.lockTeamMissionForUpdate(
                        evalutionDataTxnDto.getProjectNo().longValue(),
                        evalutionDataTxnDto.getLcorsNo().longValue(),
                        evalutionDataTxnDto.getUqstnNo(),
                        Math.toIntExact(evalutionDataTxnDto.getTeamSn()));
            }

            // ai 코드유사도를 위한 첫번째 평가자료 조회
            EvalutionDataTxnDto firstEvlDataTxn = evalutionMapper.selectFirstEvlDataTxn(evalutionDataTxnDto);

            int rtn = evalutionMapper.evlDataTxnByUqstnNoSave(evalutionDataTxnDto);

            if (rtn != 1) {
                responseMessage.setResult(-9);
            } else {
                // 위에서 입력한 평가자료내역 자료등록 일련번호를 다시 찾아온다.
                tmpEvalDataTxnDto = evalutionMapper.selectMtlEvlTxnDataRegSn(evalutionDataTxnDto);

                tmpDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());
                tmpDto.setEvlNo(evalutionDataTxnDto.getEvlNo());
                tmpDto.setEvlDegr(evalutionDataTxnDto.getEvlDegr());
                tmpDto.setMbrId(evalutionDataTxnDto.getMbrId());

                tmpDto.setEvlStusCd(Constants.EVL_STUS_CD_1);
                tmpDto.setProjectNo(evalutionDataTxnDto.getProjectNo());
                tmpDto.setLcorsNo(evalutionDataTxnDto.getLcorsNo());
                tmpDto.setUqstnNo(evalutionDataTxnDto.getUqstnNo() == null ? 9999999L : evalutionDataTxnDto.getUqstnNo());
                tmpDto.setTeamSn(evalutionDataTxnDto.getTeamSn());

                // #67: 팀에 이미 평가기본이 있으면 재사용, 없을 때만 신규 채번 (중복 EVL_BAS / PLT.evl_no dangling 방지)
                EvalutionBasDto existingEvlBas = evalutionMapper.selectEvlNo(evalutionDataTxnDto);
                if (existingEvlBas == null || existingEvlBas.getEvlNo() == null) {
                    // 신규 평가기본을 등록한다. (평가번호:sequence 자동채번, 평가차수:1)
                    rtn = evalutionMapper.insertNewEvlBas(tmpDto);
                    log.info("[#67] newEvlBas 신규 채번 - team={}/{}/{}/{}", tmpDto.getProjectNo(), tmpDto.getLcorsNo(), tmpDto.getUqstnNo(), tmpDto.getTeamSn());
                } else {
                    log.info("[#67] newEvlBas 기존 재사용 evlNo={} - team={}/{}/{}/{}", existingEvlBas.getEvlNo(), tmpDto.getProjectNo(), tmpDto.getLcorsNo(), tmpDto.getUqstnNo(), tmpDto.getTeamSn());
                }

                // 최신 evl_no 조회
                EvalutionBasDto latestEvlBas = evalutionMapper.selectEvlNo(evalutionDataTxnDto);

                // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
                UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                uqstnTeamUpdate.setProjectNo((long) tmpDto.getProjectNo());
                uqstnTeamUpdate.setLcorsNo((long) tmpDto.getLcorsNo());
                uqstnTeamUpdate.setUqstnNo(tmpDto.getUqstnNo());
                uqstnTeamUpdate.setMbrId(tmpDto.getMbrId());
                uqstnTeamUpdate.setTeamSn(Math.toIntExact(tmpDto.getTeamSn()));
                uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(latestEvlBas.getEvlNo()));
                learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);

                responseMessage.setResult(rtn);

                // 20260323 ai 코드 유사도 판별
                if (evalutionDataTxnDto.getSrccdUrlAddr() != null && evalutionDataTxnDto.getSrccdUrlAddr().length() > 0 && evalutionDataTxnDto.getBrnchNm() != null && evalutionDataTxnDto.getBrnchNm().length() > 0) {

                    Member learnerMbr = memberMapper.getMember(evalutionDataTxnDto.getMbrId());
                    String learnerREADME = fetchReadmeContent(learnerMbr, evalutionDataTxnDto.getSrccdUrlAddr(), evalutionDataTxnDto.getBrnchNm());

                    if (learnerREADME != null && learnerREADME.length() > 0) {
                        AiApiQueueDto aiApiQueueDto = new AiApiQueueDto();
                        if (firstEvlDataTxn == null) {
                            String apiParam = buildApiParam("", learnerREADME);
                            aiApiQueueDto.setApiParam(apiParam);
                            aiApiQueueDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());
                            aiApiQueueDto.setRegId(evalutionDataTxnDto.getMbrId());
                            evalutionMapper.insertAiApiQueue(aiApiQueueDto);

                        } else {
                            Member standardMbr = memberMapper.getMember(firstEvlDataTxn.getRgtrId());
                            String standardREADME = fetchReadmeContent(standardMbr, firstEvlDataTxn.getSrccdUrlAddr(), firstEvlDataTxn.getBrnchNm());

                            if (standardREADME != null && standardREADME.length() > 0) {
                                String apiParam = buildApiParam(standardREADME, learnerREADME);
                                aiApiQueueDto.setApiParam(apiParam);
                                aiApiQueueDto.setDataRegSn(tmpEvalDataTxnDto.getDataRegSn());
                                aiApiQueueDto.setRegId(evalutionDataTxnDto.getMbrId());
                                evalutionMapper.insertAiApiQueue(aiApiQueueDto);
                            }
                        }
                    }


                }


            }

        }

        return responseMessage;
    }

    /**
     * 평가자지정 후 Git Lab 프로젝트 기존 멤버 삭제, 변경 멤버 추가 API 호출에 사용
     *
     * @param chgSrccUrlAddrDto
     * @return ResponseMessage
     */
    public ResponseMessage srccdUrlAddrUpdate(ChgSrccUrlAddrDto chgSrccUrlAddrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        String listEvlMbrId = StringUtils.EMPTY, listSrccdUrlAddr = StringUtils.EMPTY,
                listSrccdUrlAddrPrev = StringUtils.EMPTY;

        List<ChgSrccUrlAddrDto> rtnList = evalutionMapper.selectChangeSrccdUrlAddr(chgSrccUrlAddrDto);

        if (rtnList.size() > 0) {
            for (int i = 0; i < rtnList.size(); i++) {
                listEvlMbrId = rtnList.get(i).getEvlMbrId();
                listSrccdUrlAddr = rtnList.get(i).getSrccdUrlAddr();
                listSrccdUrlAddrPrev = rtnList.get(i).getSrccdUrlAddrPrev();

                if (StringUtils.isEmpty(listSrccdUrlAddrPrev)) {
                    Map<String, String> mapAddData = new HashMap<>();
                    String apiUrlAddData = Constants.GITLAB_PROXY_PROJECT_MEMBER_ADD;

                    mapAddData.put(COL_USER_NAME, listEvlMbrId);
                    mapAddData.put(COL_GIT_ADDRESS, listSrccdUrlAddr);

                    JSONObject sendJsonObject = apiUtil.callRestApi(new JSONObject(mapAddData), apiUrlAddData);

                    if (!sendJsonObject.containsKey(COL_RET_CODE)
                            || !(Constants.API_RESPONSE_CD_SUCCESS).equals(sendJsonObject.get(COL_RET_CODE))) {
                        throw new ValidCustomException(messageSource.getMessage(MSG_EVREQ_MSG_GITLABPJTADDERROR, null,
                                LocaleContextHolder.getLocale()));
                    }

                    Map<String, Object> dataMap = (Map<String, Object>) sendJsonObject.get(COL_RESULT);

                    responseMessage.setResult(dataMap.get("data"));
                } else if (!listSrccdUrlAddr.equals(listSrccdUrlAddrPrev)) {
                    Map<String, String> mapDelData = new HashMap<>();
                    String apiUrlDelData = Constants.GITLAB_PROXY_PROJECT_MEMBER_DELETE;

                    mapDelData.put(COL_USER_NAME, listEvlMbrId);
                    mapDelData.put(COL_GIT_ADDRESS, listSrccdUrlAddrPrev);

                    JSONObject sendJsonObject = apiUtil.callRestApi(new JSONObject(mapDelData), apiUrlDelData);

                    if (!sendJsonObject.containsKey(COL_RET_CODE)
                            || !(Constants.API_RESPONSE_CD_SUCCESS).equals(sendJsonObject.get(COL_RET_CODE))) {
                        throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.gitlabpjtdelerror", null,
                                LocaleContextHolder.getLocale()));
                    }

                    Map<String, Object> dataMap = (Map<String, Object>) sendJsonObject.get(COL_RESULT);

                    responseMessage.setResult(dataMap.get("data"));

                    Map<String, String> mapAddData = new HashMap<>();
                    String apiUrlAddData = Constants.GITLAB_PROXY_PROJECT_MEMBER_ADD;

                    mapAddData.put(COL_USER_NAME, listEvlMbrId);
                    mapAddData.put(COL_GIT_ADDRESS, listSrccdUrlAddr);

                    JSONObject sendJsonObj = apiUtil.callRestApi(new JSONObject(mapAddData), apiUrlAddData);

                    if (!sendJsonObj.containsKey(COL_RET_CODE)
                            || !(Constants.API_RESPONSE_CD_SUCCESS).equals(sendJsonObj.get(COL_RET_CODE))) {
                        throw new ValidCustomException(messageSource.getMessage(MSG_EVREQ_MSG_GITLABPJTADDERROR, null,
                                LocaleContextHolder.getLocale()));
                    }

                    Map<String, Object> resultMap = (Map<String, Object>) sendJsonObj.get(COL_RESULT);

                    responseMessage.setResult(resultMap.get("data"));
                }
            }
        }

        return responseMessage;
    }

    /**
     * 멘토회원 정보 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    public ResponseMessage selectMentoList(EvalutionMbrDto evalutionMbrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<>();
        List<EvalutionMbrDto> resultList = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(evalutionMbrDto.getBgngDt(), formatter);
        LocalDate endDate = LocalDate.parse(evalutionMbrDto.getEndDt(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EvalutionMbrDto paramDto = new EvalutionMbrDto();
            paramDto.setBgngDt(betweenDateList.get(i).toString());
            paramDto.setMbrId(evalutionMbrDto.getMbrId());
            paramDto.setInstCd(evalutionMbrDto.getInstCd());
            paramDto.setCn(evalutionMbrDto.getCn());
            paramDto.setProjectNo(evalutionMbrDto.getProjectNo());
            paramDto.setLcorsNo(evalutionMbrDto.getLcorsNo());
            paramDto.setUqstnNo(evalutionMbrDto.getUqstnNo());
            paramDto.setTeamSn(evalutionMbrDto.getTeamSn());
            resultMap.put(COL_LIST + i, evalutionMapper.mentoEvalList(paramDto));
        }

        // resultList로 resultMap에 있는 List 전부 옮김
        for (int i = 0; i < resultMap.size(); i++) {
            List<EvalutionMbrDto> tmpList = (List<EvalutionMbrDto>) resultMap.get(COL_LIST + i);
            for (int j = 0; j < tmpList.size(); j++) {
                resultList.add(tmpList.get(j));
            }
        }

        responseMessage.setResult(resultList.stream().distinct().collect(Collectors.toList())); // 중복제거
        return responseMessage;
    }

    /**
     * 전문가 평가자 평가 가능시간 목록 조회
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    public ResponseMessage selectScheduleList(EvalutionMbrDto evalutionMbrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(REGEX_DT_1);
        LocalDate startDate = LocalDate.parse(evalutionMbrDto.getBgngDt(), formatter);
        LocalDate endDate = LocalDate.parse(evalutionMbrDto.getEndDt(), formatter);

        Long numOfDayBetween = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> betweenDateList = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDayBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
        betweenDateList.add(endDate);

        for (int i = 0; i < betweenDateList.size(); i++) {
            EvalutionMbrDto paramDto = new EvalutionMbrDto();
            paramDto.setBgngDt(betweenDateList.get(i).toString());
            paramDto.setMbrId(evalutionMbrDto.getMbrId());
            paramDto.setMtrMbrId(evalutionMbrDto.getMtrMbrId());
            paramDto.setInstCd(evalutionMbrDto.getInstCd());
            paramDto.setCn(evalutionMbrDto.getCn());
            paramDto.setProjectNo(evalutionMbrDto.getProjectNo());
            paramDto.setLcorsNo(evalutionMbrDto.getLcorsNo());
            paramDto.setUqstnNo(evalutionMbrDto.getUqstnNo());
            paramDto.setTeamSn(evalutionMbrDto.getTeamSn());
            paramDto.setMtrMbrIds(Arrays.asList(evalutionMbrDto.getMtrMbrId().split(",")));
            resultMap.put("day" + (i + 1), evalutionMapper.mentoScheduleList(paramDto));
        }

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 멘토 일정 등록
     * 평가자 지정 팝업에서 BGNG_DT ~ END_DT의 데이터는 하나의 레코드지만 화면 노출 시 30분 단위 노출되며 선택된 일정
     * INSERT처리
     * EX) 1. 2023-12-08 00:00 ~ 23:00, 일간반복 = 원본 데이터
     * 2. 2023-12-09 01:00 ~ 01:30 = 평가요청
     *
     * @param params
     * @return ResponseMessage
     */
    public Map<String, Object> insertMentoSchedule(MenteeReqDto params) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<String, Object>();

        int apiDmndSn = params.getDmndSn(); // 최초 요청번호 (처음 호출시 0을 default로)

        // 최초 요청시에만 채번
        if (params.getDmndSn() == 0) {
            params.setDmndSn(evalutionMapper.selectDmndSn(params));
        }

        int rcnt = 0;
        int ncnt = 0;
        int scdlSn = Integer.parseInt(params.getScdlSn());
        /*
         * class timeVal {
         * String sttTime;
         * String endTime;
         * }
         */
        /*
         * for (int idx = 0; idx < timeArr.size(); idx++) {
         * timeVal temp = new timeVal();
         * temp = timeArr.get(idx);
         * String stt1 = temp.sttTime.substring(0, 2) + temp.sttTime.substring(2, 4);
         * String end1 = temp.endTime.substring(0, 2) + temp.endTime.substring(2, 4);
         * String stt2 = temp.sttTime.substring(0, 2) + ":" + temp.sttTime.substring(2,
         * 4);
         * String end2 = temp.endTime.substring(0, 2) + ":" + temp.endTime.substring(2,
         * 4);
         *
         * if(stt1.equals(newSttTime) && end1.equals(newEndTime)) {
         * params.setStusCd("00002");
         * }else {
         * params.setStusCd("00001");
         * }
         *
         * params.setBgngDt(stt2);
         * params.setEndDt(end2);
         *
         * if(apiDmndSn == 0) {
         * ncnt = evalutionMapper.insertMentoSchedule(params);
         * }
         *
         * if(ncnt <= 0) {
         * break;
         * }
         * }
         */
        /*
         * if(ncnt > 0) {
         * ncnt = evalutionMapper.updateMentoUseYn(params);
         * }
         */
        /*
         * params.setBgngDt(stt1 + ":" + stt2);
         * params.setEndDt(end1 + ":" + end2);
         */
        scdlSn = evalutionMapper.selectMentoScdlSn(params);
        params.setScdlSn(String.valueOf(scdlSn));
        rcnt = evalutionMapper.insertMenteeSchedule(params);

        if (rcnt > 0) {
            resultMap.put(COL_RETURN_CODE, "MT00001");
            resultMap.put("returnMessage",
                    messageSource.getMessage("usr.evreq.pop.evalsavemsg", null, LocaleContextHolder.getLocale())); // 평가요청에
                                                                                                                   // 성공하였습니다.
            resultMap.put(COL_DMND_SN, String.valueOf(params.getDmndSn()));
        } else {
            resultMap.put(COL_RETURN_CODE, "MT00002");
            resultMap.put("returnMessage",
                    messageSource.getMessage("usr.evreq.pop.evalfailmsg", null, LocaleContextHolder.getLocale())); // 평가요청에
                                                                                                                   // 실패하였습니다.
            resultMap.put(COL_DMND_SN, null);
        }

        return resultMap;
    }

    /**
     * 멘토 평가 요청등록
     *
     * @param menteeReqDtoList
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage selectMentiReqSave(ArrayList<MenteeReqDto> menteeReqDtoList, Member member) {
        ResponseMessage responseMessage = new ResponseMessage();
        RegReqEvalDto regReqEvalDto = new RegReqEvalDto();
        // 조회 조건 설정. 아래 항목은 List 모든 index의 값이 동일함.
        regReqEvalDto.setProjectNo(menteeReqDtoList.get(0).getProjectNo());
        regReqEvalDto.setLcorsNo(menteeReqDtoList.get(0).getLcorsNo());
        regReqEvalDto.setUqstnNo(menteeReqDtoList.get(0).getUqstnNo());
        regReqEvalDto.setTeamSn(menteeReqDtoList.get(0).getTeamSn());
        regReqEvalDto.setMbrId(menteeReqDtoList.get(0).getMteMbrId());

        EvalutionBasDto evalutionBasDto = new EvalutionBasDto();

        // 1. 평가기본이 있으면 평가번호/평가차수 가져오기, 평가기본이 없는 경우 INSERT
        List<RegReqEvalDto> evlBasList = evalutionMapper.selectEvlBas(regReqEvalDto);

        if (evlBasList.isEmpty()) {
            // 평가 기본 등록
            evalutionBasDto.setProjectNo(regReqEvalDto.getProjectNo());
            evalutionBasDto.setLcorsNo(regReqEvalDto.getLcorsNo());
            evalutionBasDto.setUqstnNo(regReqEvalDto.getUqstnNo() == null ? 9999999L : regReqEvalDto.getUqstnNo());
            evalutionBasDto.setTeamSn(regReqEvalDto.getTeamSn());
            evalutionBasDto.setMbrId(regReqEvalDto.getMbrId());

            // 평가 기본 등록
            evalutionMapper.insertNewEvlBas(evalutionBasDto);

            // 평가 기본을 다시 찾아 온다.
            evlBasList = evalutionMapper.selectEvlBas(regReqEvalDto);

            // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
            if (evalutionBasDto.getUqstnNo() != null && evalutionBasDto.getUqstnNo() != 9999999L) {
                UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                uqstnTeamUpdate.setProjectNo((long) evalutionBasDto.getProjectNo());
                uqstnTeamUpdate.setLcorsNo((long) evalutionBasDto.getLcorsNo());
                uqstnTeamUpdate.setUqstnNo(evalutionBasDto.getUqstnNo());
                uqstnTeamUpdate.setMbrId(evalutionBasDto.getMbrId());
                uqstnTeamUpdate.setTeamSn(Math.toIntExact(evalutionBasDto.getTeamSn()));
                uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(evlBasList.get(0).getEvlNo()));
                learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);
            }

            regReqEvalDto.setEvlNo(evlBasList.get(0).getEvlNo());
            regReqEvalDto.setEvlDegr(evlBasList.get(0).getEvlDegr());
        } else {
            if (Constants.EVL_RESLT_CD_FAIL.equals(evlBasList.get(0).getEvlResltCd())) {
                // 조회 결과 EVL_RESLT_CD = '00002'(FAIL) 이면
                // EVL_BAS(평가기본) INSERT (평가차수 = MAX + 1)
                evalutionBasDto.setEvlNo(evlBasList.get(0).getEvlNo());
                evalutionBasDto.setProjectNo(regReqEvalDto.getProjectNo());
                evalutionBasDto.setLcorsNo(regReqEvalDto.getLcorsNo());
                evalutionBasDto.setUqstnNo(regReqEvalDto.getUqstnNo() == null ? 9999999L : regReqEvalDto.getUqstnNo());
                evalutionBasDto.setMbrId(regReqEvalDto.getMbrId());
                evalutionBasDto.setTeamSn(regReqEvalDto.getTeamSn());
                // 평가 기본 등록
                evalutionMapper.insertEvlBasEvlDegr(evalutionBasDto);

                // 평가 기본을 다시 찾아 온다.
                evlBasList = evalutionMapper.selectEvlBas(regReqEvalDto);

                // 팀미션의 경우, 팀미션 테이블에 evl_no 추가
                if (evalutionBasDto.getUqstnNo() != null && evalutionBasDto.getUqstnNo() != 9999999L) {
                    UqstnTeam uqstnTeamUpdate = new UqstnTeam();
                    uqstnTeamUpdate.setProjectNo((long) evalutionBasDto.getProjectNo());
                    uqstnTeamUpdate.setLcorsNo((long) evalutionBasDto.getLcorsNo());
                    uqstnTeamUpdate.setUqstnNo(evalutionBasDto.getUqstnNo());
                    uqstnTeamUpdate.setMbrId(evalutionBasDto.getMbrId());
                    uqstnTeamUpdate.setTeamSn(Math.toIntExact(evalutionBasDto.getTeamSn()));
                    uqstnTeamUpdate.setEvlNo(BigDecimal.valueOf(evlBasList.get(0).getEvlNo()));
                    learningMapper.updateUqstnTeamEvlNo(uqstnTeamUpdate);
                }
            } else if (Constants.EVL_RESLT_CD_PASS.equals(evlBasList.get(0).getEvlResltCd())) {
                /* 조회 결과가 EVL_RESLT_CD = '00001'(PASS) 이면오류 */
                regReqEvalDto.setRtnStr("-1");
                responseMessage.setResult(regReqEvalDto);
                return responseMessage;
            }

            /* 평가번호 */
            regReqEvalDto.setEvlNo(evlBasList.get(0).getEvlNo());
            /* 평가차수 */
            regReqEvalDto.setEvlDegr(evlBasList.get(0).getEvlDegr());
        }

        /* 상호평가예정시작일시 */
        regReqEvalDto
                .setMtlEvlPamBgngDt(menteeReqDtoList.get(0).getBgngYmd() + " " + menteeReqDtoList.get(0).getBgngDt());
        /* 상호평가예정종료일시 */
        regReqEvalDto.setMtlEvlPamEndDt(menteeReqDtoList.get(menteeReqDtoList.size() - 1).getEndYmd() + " "
                + menteeReqDtoList.get(menteeReqDtoList.size() - 1).getEndDt());
        /* 평가자유형코드 */
        regReqEvalDto.setEvlrTypeCd(Constants.EVL_TYPE_CD_2);
        /* 상호평가상태코드 */
        regReqEvalDto.setMtlEvlStusCd(Constants.EVL_STUS_CD_1);
        /* 평가회원기관코드 */
        regReqEvalDto.setEvlInstCd(menteeReqDtoList.get(0).getMtrMbrInstCd());
        /* 평가회원아이디 */
        regReqEvalDto.setEvlMbrId(menteeReqDtoList.get(0).getMtrMbrId());

        Date nowTime = new Date();

        SimpleDateFormat timeFormat = new SimpleDateFormat(REGEX_DT_2);
        String mdfcnDt = timeFormat.format(nowTime);

        regReqEvalDto.setMdfcnDt(mdfcnDt);
        // 상호평가내역 1건 등록
        evalutionMapper.insertNewMtlEvlTxn(regReqEvalDto);

        // 다시 수정해서 상호평가 일련번호를 찾는다.
        evlBasList = evalutionMapper.selectNewMtlEvlTxn(regReqEvalDto);

        // 상호평가 일련번호
        regReqEvalDto.setMtlEvlSn(evlBasList.get(0).getMtlEvlSn());

        for (int i = 0; i < menteeReqDtoList.size(); i++) {
            regReqEvalDto.setMbrId(menteeReqDtoList.get(i).getMteMbrId());
            regReqEvalDto.setMbrInstCd(menteeReqDtoList.get(i).getMteMbrInstCd());
            regReqEvalDto
                    .setMtrEvlDtm(menteeReqDtoList.get(i).getBgngYmd() + " " + menteeReqDtoList.get(i).getBgngDt()); // 멘토평가일시
            regReqEvalDto.setEvlPsblYmd(menteeReqDtoList.get(i).getBgngYmd()); // 멘토평가일자
            regReqEvalDto.setEvlPsblTm(menteeReqDtoList.get(i).getBgngDt()); // 멘토평가시간
            regReqEvalDto.setEvlInstCd(menteeReqDtoList.get(i).getMtrMbrInstCd()); // 멘토(전문가) 기관코드
            regReqEvalDto.setEvlMbrId(menteeReqDtoList.get(i).getMtrMbrId()); // 멘토(전문가) id
            regReqEvalDto.setMtrScdlSn(Long.parseLong(menteeReqDtoList.get(i).getScdlSn())); // 멘토 일정 일련번호

            // 멘토평가일정기본 등록
            evalutionMapper.insertNewMtrEvlScdlBas(regReqEvalDto);
        }

        MtrEvlScdlBasByMteConDto mtrEvlScdlBasByMteConDto = new MtrEvlScdlBasByMteConDto();
        mtrEvlScdlBasByMteConDto.setProjectNo(regReqEvalDto.getProjectNo());
        mtrEvlScdlBasByMteConDto.setLcorsNo(regReqEvalDto.getLcorsNo());
        mtrEvlScdlBasByMteConDto.setTeamSn(regReqEvalDto.getTeamSn());
        mtrEvlScdlBasByMteConDto.setMtlEvlSn(regReqEvalDto.getMtlEvlSn());

        List<MtrEvlScdlBasByMteConDto> mtrEvlScdlList = evalutionMapper
                .selectMtrEvlScdlBasByMteCon(mtrEvlScdlBasByMteConDto);

        String dmndSnStr = "0", returnCode = StringUtils.EMPTY;

        for (int i = 0; i < mtrEvlScdlList.size(); i++) {
            MenteeReqDto paramDto = new MenteeReqDto();
            paramDto.setMtrMbrInstCd(menteeReqDtoList.get(0).getMtrMbrInstCd());
            paramDto.setMtrMbrId(menteeReqDtoList.get(0).getMtrMbrId());
            paramDto.setMtrRoleCd(
                    Constants.ROLE_M.substring(Constants.ROLE_M.indexOf("_") + 1, Constants.ROLE_S.length()));
            paramDto.setScdlSn(mtrEvlScdlList.get(i).getMtrScdlSn().toString());
            paramDto.setMteMbrInstCd(mtrEvlScdlList.get(i).getInstCd());
            paramDto.setMteMbrId(mtrEvlScdlList.get(i).getMbrId());
            paramDto.setMteRoleCd(
                    Constants.ROLE_S.substring(Constants.ROLE_S.indexOf("_") + 1, Constants.ROLE_S.length()));
            paramDto.setMtDmndCn(menteeReqDtoList.get(0).getMtDmndCn());
            paramDto.setBgngYmd(mtrEvlScdlList.get(i).getMtrEvlBgngYmd());
            paramDto.setBgngDt(mtrEvlScdlList.get(i).getMtrEvlBgngTm());
            paramDto.setEndYmd(mtrEvlScdlList.get(i).getMtrEvlEndYmd());
            paramDto.setEndDt(mtrEvlScdlList.get(i).getMtrEvlEndTm());
            paramDto.setDmndSn(Integer.parseInt(dmndSnStr));

            Map<String, Object> resultMap = this.insertMentoSchedule(paramDto);

            returnCode = resultMap.get(COL_RETURN_CODE).toString();

            if ("MT00002".equals(returnCode)) {
                regReqEvalDto.setRtnStr(returnCode);
                responseMessage.setResult(regReqEvalDto);
                return responseMessage;
            }

            dmndSnStr = String.valueOf(resultMap.get(COL_DMND_SN));

            regReqEvalDto.setDmndSn(dmndSnStr);
        }

        // 상호평가내역, 멘토평가일정기본 update
        // 멘토일정상태코드 : 요청중(00001), 수락(00002), 거절(00003), 요청취소(00004)
        regReqEvalDto.setMtrScdlStusCd(Constants.MTL_SCDL_STTUS_CD_1);
        evalutionMapper.updateMtrEvlScdlBasByPk(regReqEvalDto);
        evalutionMapper.updateMtlEvlTxnByPk(regReqEvalDto);

        regReqEvalDto.setRtnStr(returnCode);

        String msg1 = messageSource.getMessage("usr.evreq.msg.regmtsched1", null, LocaleContextHolder.getLocale());
        String msg2 = messageSource.getMessage("usr.evreq.msg.regmtsched2", null, LocaleContextHolder.getLocale());
        String msg3 = messageSource.getMessage("usr.evreq.msg.regmtsched3", null, LocaleContextHolder.getLocale());
        String msg4 = messageSource.getMessage("usr.evreq.msg.regmtsched4", null, LocaleContextHolder.getLocale());
        String msg5 = messageSource.getMessage("usr.evreq.msg.regmtsched5", null, LocaleContextHolder.getLocale());
        String msg6 = messageSource.getMessage("usr.evreq.msg.regmtsched6", null, LocaleContextHolder.getLocale());
        String msg7 = messageSource.getMessage("usr.evreq.msg.regsched7", null, LocaleContextHolder.getLocale());

        // 4. 알림게시판기본에 INSERT 처리
        String pstartCn = msg1 + HTML_BR +
                msg2 + HTML_BR +
                msg3 + HTML_BR +
                msg7 + " " + regReqEvalDto.getMtlEvlPamBgngDt() + HTML_BR +
                msg4 + " " + menteeReqDtoList.get(0).getLcorsNoNm() + HTML_BR +
                msg5 + " " + menteeReqDtoList.get(0).getUqstnNoNm() + HTML_BR +
                msg6 + " " + regReqEvalDto.getMtlEvlPamBgngDt();

        String pstartTitlNm = messageSource.getMessage("usr.evreq.msg.evldesntedmt.title", null,
                LocaleContextHolder.getLocale());
        regReqEvalDto.setPstartTitlNm(pstartTitlNm);
        regReqEvalDto.setPstartCn(pstartCn);
        regReqEvalDto.setNtcDivCd(Constants.NTC_DIV_CD_8);
        regReqEvalDto.setMdfcnDt(mdfcnDt);
        regReqEvalDto.setSysDivCd(Constants.SYS_DIV_CD_1);
        // evalutionMapper.insertNtcBbsBasFromEvl(regReqEvalDto);

        // 평가자 정보 구하기 (멘토용)
        MbrBasDto mbrBasDto2 = new MbrBasDto();
        mbrBasDto2.setInstCd(menteeReqDtoList.get(0).getMtrMbrInstCd());
        mbrBasDto2.setMbrId(menteeReqDtoList.get(0).getMtrMbrId());
        List<MbrBasDto> resultList = evalutionMapper.selectMbrBasList(mbrBasDto2);

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setMbrAuthCd(resultList.get(0).getRoleCd());
        emailDTO.setEmail(resultList.get(0).getEmlAddr());
        emailDTO.setName(resultList.get(0).getMbrNm());
        emailDTO.setEmailTemp(Constants.EMAIL_TYPE_DIV_CD_3);
        emailDTO.setMbrId(member.getMbrId());
        emailDTO.setSubTitle(
                messageSource.getMessage("usr.evreq.mailtem.subject2", null, LocaleContextHolder.getLocale())); // 멘토링
                                                                                                                // 요청 메일
        emailDTO.setContents(pstartCn);
        emailDTO.setSiteUrl(domainInfoAmsUrl);

        try {
            emailUtil.sendEmail(LocaleContextHolder.getLocale(), emailDTO); // 메일 발송
        } catch (RuntimeException e) {
            log.error("Error sending email #2");
        }

        responseMessage.setResult(regReqEvalDto);
        return responseMessage;
    }

    /**
     * 포인트 히스토리 누적(수정)
     *
     * @param member
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage experienceScrUpdate(Member member, EvalutionDataTxnDto evalutionDataTxnDto) {
        // 재평가인 경우 보상 지급 스킵
        if (evalutionDataTxnDto.getEvlNo() != null) {
            // 판정은 평가번호 단위 한 곳으로 모은다. MAX 1행의 REEVL_YN 을 보면
            // 승계 이전에 만들어진 행에서는 재도전 이력이 가려져 게이트가 뚫린다.
            if (reEvaluationMapper.existsReEvaluationDegree(evalutionDataTxnDto.getEvlNo().longValue())) {
                log.info("[재평가] 보상 지급 스킵. evlNo={}", evalutionDataTxnDto.getEvlNo());
                Map<String, Object> skipResultMap = new HashMap<>();
                skipResultMap.put(COL_RETURN_CODE, "CM00001");
                ResponseMessage skipResponse = new ResponseMessage();
                skipResponse.setResult(skipResultMap);
                return skipResponse;
            }
        }

        int i = 0;
        String mbrId = "",
                msg1 = messageSource.getMessage("usr.evreq.msg.dedctpntevlreq", null, LocaleContextHolder.getLocale()) // 평가요청
                                                                                                                       // 포인트
                                                                                                                       // 차감
                , roleCd = member.getRoleCd();
        Map<String, Object> resultMap = new HashMap<>();

        ResponseMessage responseMessage = new ResponseMessage();
        CrtrScrDto crtrScrDto = new CrtrScrDto();
        crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
        crtrScrDto.setCrtrTypeNo(11L);
        crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);

        // 포인트 적립/차감
        Double dl1 = Double.parseDouble(evalutionMapper.selectPointNowScr(crtrScrDto).toString());

        List<EvalutionDataTxnDto> rtnList = evalutionMapper.apiCmmnGetPointNowScr(evalutionDataTxnDto);

        if (!rtnList.isEmpty()) {
            StringBuffer dtlDescSb1 = new StringBuffer();

            for (i = 0; i < rtnList.size(); i++) {
                mbrId = rtnList.get(i).getMbrId();

                dtlDescSb1.append("'").append(evalutionDataTxnDto.getProjectNm())
                        .append("' '").append(evalutionDataTxnDto.getLcorsNoNm())
                        .append("' '").append(evalutionDataTxnDto.getUqstnNoNm())
                        .append("' '").append(evalutionDataTxnDto.getTeamSn())
                        .append("' '").append(evalutionDataTxnDto.getEvlMbrNm())
                        .append("' ").append(msg1);

                ExprPointDto exprPointDto = new ExprPointDto();
                exprPointDto.setInstitutionCode(evalutionDataTxnDto.getInstCd());
                exprPointDto.setRoleCd(
                        roleCd.contains("_") ? roleCd.substring(roleCd.indexOf("_") + 1, roleCd.length()) : roleCd);
                exprPointDto.setMemberId(mbrId);
                exprPointDto.setSystemDivisionCode(SYS_DIV_CD_2);
                exprPointDto.setWorkjobDivisionCode(Constants.WORK_DIV_CD_EV);
                exprPointDto.setPointTypeNumber(11L);
                exprPointDto.setFlPointScore(dl1);
                exprPointDto.setPointDetailDescription(dtlDescSb1.toString());

                int rtn = evalutionMapper.insertPointHst(exprPointDto);

                if (rtn != 1) {
                    String errormsg = messageSource.getMessage(MSG_EVREQ_MSG_POINTERROR, null,
                            LocaleContextHolder.getLocale()); // 포인트 적립/차감에 실패했습니다.
                    throw new ValidCustomException(errormsg);
                }
            }
        }

        // 경험치 적립/차감
        crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_E);
        crtrScrDto.setCrtrTypeNo(22L);
        crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);

        dl1 = Double.parseDouble(evalutionMapper.selectPointNowScr(crtrScrDto).toString());

        if (!rtnList.isEmpty()) {
            StringBuffer dtlDescSb2 = new StringBuffer();

            for (i = 0; i < rtnList.size(); i++) {
                mbrId = rtnList.get(i).getMbrId();

                dtlDescSb2.append("'").append(evalutionDataTxnDto.getProjectNm())
                        .append("' '").append(evalutionDataTxnDto.getLcorsNoNm())
                        .append("' '").append(evalutionDataTxnDto.getUqstnNoNm())
                        .append("' '").append(evalutionDataTxnDto.getTeamSn())
                        .append("' '").append(evalutionDataTxnDto.getEvlMbrNm())
                        .append("' ").append(msg1);

                ExprPointDto exprPointDto = new ExprPointDto();
                exprPointDto.setInstitutionCode(evalutionDataTxnDto.getInstCd());
                exprPointDto.setMemberId(mbrId);
                exprPointDto.setRoleCd(
                        roleCd.contains("_") ? roleCd.substring(roleCd.indexOf("_") + 1, roleCd.length()) : roleCd);
                exprPointDto.setSystemDivisionCode(SYS_DIV_CD_2);
                exprPointDto.setWorkjobDivisionCode(Constants.WORK_DIV_CD_EV);
                exprPointDto.setPointTypeNumber(22L);
                exprPointDto.setExperienceScore(dl1);
                exprPointDto.setExperienceDetailDescription(dtlDescSb2.toString());

                int rtn = evalutionMapper.insertExprHst(exprPointDto);

                if (rtn != 1) {
                    String errormsg = messageSource.getMessage("usr.evreq.msg.exprpointerror", null,
                            LocaleContextHolder.getLocale()); // 경험치 적립/차감에 실패했습니다.
                    throw new ValidCustomException(errormsg);
                }
            }
        }
        resultMap.put(COL_RETURN_CODE, "CM00001");
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가자지정 후 Git Lab 프로젝트 멤버 추가 API 호출
     *
     * @param evalutionBasDto
     * @return ResponseMessage
     */
    public ResponseMessage apiProxyProjectMemberSave(EvalutionBasDto evalutionBasDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        String returnCode = "";
        String srccdUrlAddr = evalutionMapper.selectSrccdUrlAddr(evalutionBasDto);

        if (StringUtils.isNotEmpty(srccdUrlAddr)) {
            HashMap<String, String> hashMap = new HashMap<>();
            JSONObject data = new JSONObject();
            String apiUrl = Constants.GITLAB_PROXY_PROJECT_MEMBER_ADD;

            hashMap.put(COL_USER_NAME, evalutionBasDto.getRgtrId());
            hashMap.put(COL_GIT_ADDRESS, srccdUrlAddr);

            data = new JSONObject(hashMap);

            JSONObject sendJsonObject = new JSONObject();
            sendJsonObject = apiUtil.callRestApi(data, apiUrl);

            if (!sendJsonObject.containsKey(COL_RET_CODE)
                    || !Constants.API_RESPONSE_CD_SUCCESS.equals(sendJsonObject.get(COL_RET_CODE))) {
                throw new ValidCustomException(messageSource.getMessage(MSG_EVREQ_MSG_GITLABPJTADDERROR, null,
                        LocaleContextHolder.getLocale()));
            }

            returnCode = (String) sendJsonObject.get(COL_RET_CODE);
        } else {
            returnCode = "-1";
            responseMessage.setResult(returnCode);
        }
        return responseMessage;
    }

    /**
     * 1. 평가자 불참 으로 인해 포인트 적립/차감 연계
     * 2. 경험치 적립/차감 연계
     *
     * @param evalutionDataTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage apiCmmnsetPointScrByCnclReq(Member member, EvalutionDataTxnDto evalutionDataTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String roleCd = member.getRoleCd();

        int i = 0;
        Double dl1 = 0d;
        String mbrId = "", pjtNm = "", dtlDesc = "", enDtlDesc = "";
        CrtrScrDto crtrScrDto = new CrtrScrDto();

        List<EvalutionDataTxnDto> rtnList = evalutionMapper.apiCmmnGetPointNowScr(evalutionDataTxnDto);

        // 요청취소 true 체크
        if ("00001".equals(evalutionDataTxnDto.getEvlDmndRtrcnRsnCd())) {

            // 팀 미션 평가는 신청 시 차감이 없으므로 취소 시 패널티도 없음 (팀원 혼란 방지)
            // 판정 기준: PJT_LCORS_UQSTN.TEAM_COMPOSITION 가 '00000'(개인) 이 아니면 팀 미션
            String teamComposition = null;
            if (evalutionDataTxnDto.getProjectNo() != null
                    && evalutionDataTxnDto.getLcorsNo() != null
                    && evalutionDataTxnDto.getUqstnNo() != null) {
                teamComposition = evaluationAppendMapper.getTeamComposition(
                        evalutionDataTxnDto.getProjectNo().longValue(),
                        evalutionDataTxnDto.getLcorsNo().longValue(),
                        evalutionDataTxnDto.getUqstnNo());
            }
            if (teamComposition != null && !"00000".equals(teamComposition)) {
                resultMap.put(COL_RETURN_CODE, "CM00001");
                responseMessage.setResult(resultMap);
                return responseMessage;
            }

            crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
            crtrScrDto.setCrtrTypeNo(14L);
            crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);

            dl1 = Double.parseDouble(evalutionMapper.selectPointNowScr(crtrScrDto).toString());

//            if (!rtnList.isEmpty()) {
//                for (i = 0; i < rtnList.size(); i++) {
//                    mbrId = rtnList.get(i).getMbrId(); // 학습회원아이디
//                    pjtNm = rtnList.get(i).getLrnrNm(); // 학습회원명
                    pjtNm = evalutionDataTxnDto.getLrnrNm();

                    String msg1 = messageSource.getMessage("usr.evreq.msg.dedctpntnoattncevl", null,
                            LocaleContextHolder.getLocale()); // 평가불참 포인트 차감
                    String msg2 = messageSource.getMessage("usr.evreq.msg.dedctpntnoattncevl", null, Locale.ENGLISH); // 평가불참
                                                                                                                      // 포인트
                                                                                                                      // 차감

                    dtlDesc = "프로젝트명 : " + evalutionDataTxnDto.getProjectNm() + "\n " +
                            "문제명 : " + evalutionDataTxnDto.getUqstnNoNm() + "\n " +
                            "평가 요청 시간 : " + evalutionDataTxnDto.getMtlEvlPamBgngDt() + "\n " +
                            "피평가자 : " + pjtNm + "\n " +
                            "포인트 차감 사유 : " + msg1;

                    enDtlDesc = "project : " + evalutionDataTxnDto.getProjectNm() + "\n " +
                            "problem : " + evalutionDataTxnDto.getUqstnNoNm() + "\n " +
                            "evaluation request time : " + evalutionDataTxnDto.getMtlEvlPamBgngDt() + "\n " +
                            "evaluated : " + pjtNm + "\n " +
                            "point deduction reason : " + msg2;

                    PointDto pointDto = new PointDto();
                    pointDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
                    pointDto.setCrtrTypeNo("14");
                    pointDto.setWrkjbDivCd(Constants.WORK_DIV_CD_EV);
                    PointDto pnt = pointService.selectPoint(pointDto);

                    if (pnt != null) {
                        ExprPointDto exprPointDto = new ExprPointDto();
                        exprPointDto.setInstitutionCode(evalutionDataTxnDto.getInstCd());
                        exprPointDto.setMemberId(evalutionDataTxnDto.getEvlMbrId());
                        exprPointDto.setSystemDivisionCode(SYS_DIV_CD_2);
                        exprPointDto.setWorkjobDivisionCode(Constants.WORK_DIV_CD_EV);
                        exprPointDto.setPointTypeNumber(14L);
                        exprPointDto.setRoleCd(
                                roleCd.contains("_") ? roleCd.substring(roleCd.indexOf("_") + 1, roleCd.length()) : roleCd);
                        exprPointDto.setFlPointScore(dl1);
                        exprPointDto.setPointDetailDescription(dtlDesc);
                        exprPointDto.setEngPointDetailDescription(enDtlDesc);

                        int rtn = evalutionMapper.insertPointHst(exprPointDto);

                        MbrPntHstDto mbrPntHstDto = new MbrPntHstDto();
                        mbrPntHstDto.setMbrId(evalutionDataTxnDto.getEvlMbrId());
                        mbrPntHstDto.setInstCd(evalutionDataTxnDto.getInstCd());
                        pointService.updateMbrInstRelPnt(mbrPntHstDto);

                        if (rtn != 1) {
                            String errormsg = messageSource.getMessage(MSG_EVREQ_MSG_POINTERROR, null,
                                    LocaleContextHolder.getLocale()); // 포인트 적립/차감에 실패했습니다.
                            throw new ValidCustomException(errormsg);
                        }
                    }


                }
//            }

            /***********
             * // 평가자 불참으로 인해 피평가자에게 포인트 반환 코드가 따로 있으므로 주석처리
             * crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_P);
             * crtrScrDto.setCrtrTypeNo(206L);
             * crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);
             *
             * dl1 =
             * Double.parseDouble(evalutionMapper.selectPointNowScr(crtrScrDto).toString());
             *
             * if (!rtnList.isEmpty()) {
             * for (i = 0; i < rtnList.size(); i++) {
             * mbrId = rtnList.get(i).getMbrId(); // 학습회원아이디
             * pjtNm = rtnList.get(i).getPjtNm(); // 학습회원명
             * String msg1 = messageSource.getMessage("usr.evreq.msg.rtnpntnoattncevl",
             * null, LocaleContextHolder.getLocale()); // 평가불참 포인트 반환
             *
             * dtlDesc = "'" + evalutionDataTxnDto.getProjectNm() + "' '" +
             * evalutionDataTxnDto.getLcorsNoNm() + "' '" +
             * evalutionDataTxnDto.getUqstnNoNm() + "' '" + evalutionDataTxnDto.getTeamSn()
             * + "' '" + pjtNm + "' " + msg1;
             * ExprPointDto exprPointDto = new ExprPointDto();
             * exprPointDto.setInstitutionCode(evalutionDataTxnDto.getInstCd());
             * exprPointDto.setMemberId(mbrId);
             * exprPointDto.setSystemDivisionCode(Constants.SYS_DIV_CD_2);
             * exprPointDto.setWorkjobDivisionCode(Constants.WORK_DIV_CD_EV);
             * exprPointDto.setPointTypeNumber(206L);
             * exprPointDto.setRoleCd(roleCd.contains("_") ?
             * roleCd.substring(roleCd.indexOf("_")+1, roleCd.length()) : roleCd);
             * exprPointDto.setFlPointScore(dl1);
             * exprPointDto.setPointDetailDescription(dtlDesc);
             *
             * int rtn = evalutionMapper.insertPointHst(exprPointDto);
             *
             * if (rtn != 1) {
             * String errormsg = messageSource.getMessage(MSG_EVREQ_MSG_POINTERROR, null,
             * LocaleContextHolder.getLocale()); // 포인트 적립/차감에 실패했습니다.
             * throw new ValidCustomException(errormsg);
             * }
             *
             *
             *
             * }
             * }
             *********************************/
//        }

        // 경험치 적립/차감 연계
        crtrScrDto.setCrtrTypeCd(Constants.CRTR_TYPE_CD_E);
        crtrScrDto.setCrtrTypeNo(23L);
        crtrScrDto.setCrtrUseYn(Constants.USE_YN_Y);

        dl1 = Double.parseDouble(evalutionMapper.selectPointNowScr(crtrScrDto).toString());

        if (!rtnList.isEmpty()) {
            for (i = 0; i < rtnList.size(); i++) {
                mbrId = rtnList.get(i).getMbrId(); // 학습회원아이디
                pjtNm = rtnList.get(i).getPjtNm(); // 학습회원명
                String msg1 = messageSource.getMessage("usr.evreq.msg.accrlexpcnclevlreq", null,
                        LocaleContextHolder.getLocale()); // 평가요청취소 경험치 적립

                dtlDesc = "'" + evalutionDataTxnDto.getProjectNm() + "' '" + evalutionDataTxnDto.getLcorsNoNm() + "' '"
                        + evalutionDataTxnDto.getUqstnNoNm() + "' '" + evalutionDataTxnDto.getTeamSn() + "' '"
                        + evalutionDataTxnDto.getEvlMbrNm() + "' " + msg1;

                ExprPointDto exprPointDto = new ExprPointDto();
                exprPointDto.setInstitutionCode(evalutionDataTxnDto.getInstCd());
                exprPointDto.setMemberId(mbrId);
                exprPointDto.setSystemDivisionCode(SYS_DIV_CD_2);
                exprPointDto.setWorkjobDivisionCode(Constants.WORK_DIV_CD_EV);
                exprPointDto.setExperienceTypeNumber(23L);
                exprPointDto.setExperienceScore(dl1);
                exprPointDto.setRoleCd(
                        roleCd.contains("_") ? roleCd.substring(roleCd.indexOf("_") + 1, roleCd.length()) : roleCd);
                exprPointDto.setExperienceDetailDescription(dtlDesc);

                int rtn = evalutionMapper.insertExprHst(exprPointDto);

                if (rtn != 1) {
                    String errormsg = messageSource.getMessage("usr.evreq.msg.exprpointerror", null,
                            LocaleContextHolder.getLocale()); // 경험치 적립/차감에 실패했습니다.
                    throw new ValidCustomException(errormsg);
                }
            }
        }
        resultMap.put(COL_RETURN_CODE, "CM00001");
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 상호평가 요청취소
     *
     * @param mtlEvlTxnDto
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage cancelMtlEvlTxnUpdate(Member member, MtlEvlTxnDto mtlEvlTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<String, Object>();

        // 평가 취소 전 상호평가 내역 상태 조회 (동기 처리)
        String preEvlStatus = evalutionMapper.getPreEvlStatusDetail(mtlEvlTxnDto);
        if (!(preEvlStatus.equals("00001") || preEvlStatus.equals("00002"))) {
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.pop.evalcnclFail", null, LocaleContextHolder.getLocale()));
        }

        // 소유권 검증: 인증된 사용자가 해당 평가 요청의 등록자인지 확인
        String rgtrId = evalutionMapper.getMtlEvlTxnRgtrId(mtlEvlTxnDto);
        if (rgtrId == null || !rgtrId.equals(member.getMbrId())) {
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.pop.evalcnclFail", null, LocaleContextHolder.getLocale()));
        }

        // 수정자는 클라이언트 값이 아니라 인증된 사용자로 확정한다 — 이 값이 취소 주체의 유일한 근거다
        mtlEvlTxnDto.setMdfrId(member.getMbrId());

        // "평가자 불참"으로 취소 요청 시: 평가 시작 후 15분이 경과한 경우에만 허용
        // (프론트에서 15분 후 체크박스가 활성화되지만, API 직접 호출/시계 조작 방어용 서버 검증)
        if ("00001".equals(mtlEvlTxnDto.getEvlDmndRtrcnRsnCd())) {
            String bgngDt = mtlEvlTxnDto.getMtlEvlPamBgngDt();
            if (bgngDt != null && bgngDt.length() >= 16) {
                // 다양한 입력 형식("yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss",
                // "yyyy-MM-dd HH:mm:ss.0", "yyyy-MM-ddTHH:mm" 등)을 모두 처리하기 위해
                // 앞 16자만 잘라서 'T'를 공백으로 정규화
                String normalized = bgngDt.substring(0, 16).replace('T', ' ');
                try {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    fmt.setLenient(false);
                    Date bgngDate = fmt.parse(normalized);
                    long nowMillis = System.currentTimeMillis();
                    long thresholdMillis = bgngDate.getTime() + (15L * 60L * 1000L);
                    if (nowMillis < thresholdMillis) {
                        throw new ValidCustomException(messageSource.getMessage(
                                "usr.evreq.pop.evalcnclNoShowTooEarly", null, LocaleContextHolder.getLocale()));
                    }
                } catch (java.text.ParseException pe) {
                    // 날짜 파싱 실패는 유효성 위반으로 간주하고 취소 차단
                    throw new ValidCustomException(messageSource.getMessage(
                            "usr.evreq.pop.evalcnclFail", null, LocaleContextHolder.getLocale()));
                }
            } else {
                log.warn("[cancelMtlEvlTxnUpdate] mtlEvlPamBgngDt is null or too short: '{}'", bgngDt);
            }
        }

        String roleCd = member.getRoleCd();
        roleCd = roleCd != null ? roleCd.replaceFirst("^ROLE_", "") : null;

        // 1. 상호평가 내역에 취소
        int rtnInt = evalutionMapper.cancelMtlEvlTxnUpdate(mtlEvlTxnDto);

        // 취소 완료 시
        if (rtnInt > 0) {
            // 평가자 불참(00001): 해당 평가 슬롯을 잠그고(00002->00003),
            // 같은 평가자의 미래 오픈(미매칭) 슬롯은 삭제해 추가 배정을 차단한다(#39). 매칭(예약)된 슬롯은 보존.
            // 취소사유는 클라이언트 값이라 null 일 수 있다. 위 분기와 같은 형태로 맞춘다.
            if ("00001".equals(mtlEvlTxnDto.getEvlDmndRtrcnRsnCd())) {
                rtnInt = evalutionMapper.updateNoShowEvlPsblScdlBasCncl(mtlEvlTxnDto);
                // 노쇼 평가자의 미래 오픈(미매칭) 슬롯 삭제 — 추가 배정 차단(#39). 매칭(예약)된 슬롯은 보존.
                // 대상 평가자는 클라이언트 값이 아닌 서버 조회로 확정한다.
                String noShowEvlMbrId = evalutionMapper.getMtlEvlTxnEvlMbrId(mtlEvlTxnDto);
                if (noShowEvlMbrId != null && !noShowEvlMbrId.isEmpty()) {
                    evalutionMapper.deleteNoShowEvlrOpenScdl(noShowEvlMbrId);
                }
            } else {
                // 일반 취소: 확정(00002) 슬롯을 다시 오픈(00001)으로 환원
                rtnInt = evalutionMapper.updateEvlPsblScdlBasCncl(mtlEvlTxnDto);
            }
        }

        RegReqEvalDto regReqEvalDto = new RegReqEvalDto();

        String msg1 = messageSource.getMessage("usr.evreq.msg.cancelsched1", null, LocaleContextHolder.getLocale());
        String msg2 = messageSource.getMessage("usr.evreq.msg.cancelsched2", null, LocaleContextHolder.getLocale());
        String msg3 = messageSource.getMessage("usr.evreq.msg.cancelsched3", null, LocaleContextHolder.getLocale());
        String msg4 = messageSource.getMessage("usr.evreq.msg.cancelsched4", null, LocaleContextHolder.getLocale());
        String msg5 = messageSource.getMessage("usr.evreq.msg.cancelsched5", null, LocaleContextHolder.getLocale());
        String msg6 = messageSource.getMessage("usr.evreq.msg.cancelsched6", null, LocaleContextHolder.getLocale());
        String msg7 = messageSource.getMessage("usr.evreq.msg.cancelsched7", null, LocaleContextHolder.getLocale());
        String msg8 = messageSource.getMessage("usr.evreg.msg.cacnelschedRequester", null,
                LocaleContextHolder.getLocale());
        String msg9 = messageSource.getMessage("usr.evreg.msg.cacnelschedRequesterDiscordID", null,
                LocaleContextHolder.getLocale());

        // 4. 알림게시판기본에 INSERT 처리
        String pstartCn = msg1 + HTML_BR +
                msg2 + HTML_BR +
                msg3 + HTML_BR +
                msg8 + " " + member.getMbrNm() + "(" + member.getEmlAddr() + ")" + HTML_BR +
                msg9 + " "
                + (member.getDiscordId() != null && !member.getDiscordId().equals("") ? member.getDiscordId() : "-")
                + HTML_BR +
                msg7 + " " + mtlEvlTxnDto.getProjectNm() + HTML_BR +
                msg4 + " " + mtlEvlTxnDto.getLcorsNoNm() + HTML_BR +
                msg5 + " " + mtlEvlTxnDto.getUqstnNoNm() + HTML_BR +
                msg6 + " " + mtlEvlTxnDto.getMtlEvlPamBgngDt();

        String pstartTitlNm = messageSource.getMessage("usr.evreq.msg.evlclcl.title", null,
                LocaleContextHolder.getLocale());
        Long nowTime = System.currentTimeMillis();

        SimpleDateFormat timeFormat = new SimpleDateFormat(REGEX_DT_2);
        String mdfcnDt = timeFormat.format(new Date(nowTime));

        regReqEvalDto.setEvlInstCd(mtlEvlTxnDto.getEvlInstCd());
        regReqEvalDto.setEvlMbrId(mtlEvlTxnDto.getEvlMbrId());
        regReqEvalDto.setMbrId(mtlEvlTxnDto.getRgtrId());
        regReqEvalDto.setMdfcnDt(mdfcnDt);

        // 공통 DTO로 변환
        NtcBbsBasDTO ntcBbsBasDTO = new NtcBbsBasDTO();
        ntcBbsBasDTO.setInstCd(member.getInstCd());
        ntcBbsBasDTO.setMbrId(regReqEvalDto.getEvlMbrId());
        ntcBbsBasDTO.setRgtrId(member.getMbrId());
        ntcBbsBasDTO.setReadYn("N");
        // 동료요청 취소이기에 같은 권한 사용
        ntcBbsBasDTO.setRoleCd(roleCd);
        ntcBbsBasDTO.setRgtrRoleCd(roleCd);
        ntcBbsBasDTO.setMdfrId(member.getMbrId());
        ntcBbsBasDTO.setMdfcnDt(mdfcnDt);
        ntcBbsBasDTO.setPstartTitlNm(pstartTitlNm);
        ntcBbsBasDTO.setPstartCn(pstartCn);
        ntcBbsBasDTO.setNtcDivCd(NTC_DIV_CD_101);
        ntcBbsBasDTO.setSysDivCd(DM_SYS_DIV_CD_19);
        // 평가일정 알림게시판기본에 등록
        ntcBbsBasMapper.save(ntcBbsBasDTO);

        // 빌더에서 Email과 Discord를 모두 생성하여 NotificationMessage 반환
        MtlEvlTxn mtlEvlTxn = mtlEvlTxnMapper.getMtlEvlTxn(mtlEvlTxnDto.getMtlEvlSn());
        Member reviewerMember = memberMapper.getMember(mtlEvlTxnDto.getEvlMbrId());

        // mtlEvlTxn이 null일 경우 mtlEvlTxnDto에서 값을 가져옴
        String evlrTypeCd = mtlEvlTxn != null ? mtlEvlTxn.getEvlrTypeCd() : mtlEvlTxnDto.getEvlrTypeCd();
        String mtlEvlStusCd = mtlEvlTxn != null ? mtlEvlTxn.getMtlEvlStusCd() : Constants.MTL_EVL_STUS_CD_5;
        String mtlEvlPamBgngDt = mtlEvlTxn != null ? mtlEvlTxn.getMtlEvlPamBgngDt() : mtlEvlTxnDto.getMtlEvlPamBgngDt();

        NotificationMtlEvlTxn notificationMtlEvlTxn = NotificationMtlEvlTxn.from(
                mtlEvlStusCd,
                evlrTypeCd,
                mtlEvlPamBgngDt);

        // 템플릿 조회 후 빌더 호출
        NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);

        // 이메일 제목 동적 생성
        String emailSubtitle = pstartTitlNm;

        // Discord 알림 유형 메시지 구성
        String discordNotificationType = msg2;

        String notificationBody = msg3 + HTML_BR +
                msg8 + " " + member.getMbrNm() + "(" + member.getEmlAddr() + ")" + HTML_BR +
                msg9 + " "
                + (member.getDiscordId() != null && !member.getDiscordId().equals("") ? member.getDiscordId() : "-")
                + HTML_BR +
                msg7 + " " + mtlEvlTxnDto.getProjectNm() + HTML_BR +
                msg4 + " " + mtlEvlTxnDto.getLcorsNoNm() + HTML_BR +
                msg5 + " " + mtlEvlTxnDto.getUqstnNoNm() + HTML_BR +
                msg6 + " " + mtlEvlTxnDto.getMtlEvlPamBgngDt();

        NotificationMessage message = commonNotificationMessageBuilder.buildContentNotificationMessage(
                template,
                mtlEvlTxnDto.getMtlEvlSn(),
                member,
                reviewerMember,
                member.getInstCd(),
                roleCd,
                emailSubtitle,
                notificationBody,
                discordNotificationType);

        applicationEventPublisher
                .publishEvent(new NotificationEvent(EventBoundary.EVALUATION, EventDetail.CANCEL, message));

        resultMap.put("rtnInt", rtnInt);
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가자지정 후 Git Lab 프로젝트 멤버 삭제 API 호출
     *
     * @param mtlEvlTxnDto
     * @return ResponseMessage
     */
    public ResponseMessage gitlabDeleteProjectMember(MtlEvlTxnDto mtlEvlTxnDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        String returnCode = "";

        String srccdUrlAddr = evalutionMapper.selectDelSrccdUrlAddr(mtlEvlTxnDto);

        if (StringUtils.isNotEmpty(srccdUrlAddr)) {
            HashMap<String, String> hashMap = new HashMap<>();
            JSONObject data = new JSONObject();
            String apiUrl = Constants.GITLAB_PROXY_PROJECT_MEMBER_DELETE;

            hashMap.put(COL_USER_NAME, mtlEvlTxnDto.getEvlMbrId());
            hashMap.put(COL_GIT_ADDRESS, srccdUrlAddr);

            data = new JSONObject(hashMap);
            JSONObject sendJsonObject = apiUtil.callRestApi(data, apiUrl);

            if (!sendJsonObject.containsKey(COL_RET_CODE)
                    || !(Constants.API_RESPONSE_CD_SUCCESS).equals(sendJsonObject.get(COL_RET_CODE))) {
                throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.gitlabpjtdelerror", null,
                        LocaleContextHolder.getLocale()));
            }

            returnCode = (String) sendJsonObject.get(COL_RET_CODE);
        } else {
            returnCode = "-1";
        }

        responseMessage.setResult(returnCode);
        return responseMessage;
    }

    /**
     * 전문가 평가자 멘티일정취소
     *
     * @param evalutionMbrDto
     * @return ResponseMessage
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseMessage getMentiCancelUpdate(Member member, EvalutionMbrDto evalutionMbrDto) {
        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, Object> resultMap = new HashMap<String, Object>();

        // 삭제·취소 대상을 전부 클라이언트 값으로 받으므로 남의 일정을 지울 수 있다.
        // 넘어오는 mbrId 는 멘토(전문가) 이므로 그것과 비교하면 안 되고,
        // 형제 메서드(평가요청 취소)와 같이 "그 상호평가를 등록한 사람"이 호출자인지로 확인한다.
        MtlEvlTxnDto ownerChk = new MtlEvlTxnDto();
        ownerChk.setMtlEvlSn(evalutionMbrDto.getDelMtlEvlSn());
        String ownerId = evalutionMapper.getMtlEvlTxnRgtrId(ownerChk);
        if (member == null || ownerId == null || !ownerId.equals(member.getMbrId())) {
            throw new ValidCustomException(
                    messageSource.getMessage("usr.evreq.pop.evalcnclFail", null, LocaleContextHolder.getLocale()));
        }

        MenteeReqDto menteeReqDto = new MenteeReqDto();
        menteeReqDto.setMteMbrInstCd(evalutionMbrDto.getInstCd());
        menteeReqDto.setMteMbrId(evalutionMbrDto.getMbrId());
        menteeReqDto.setDmndSn(Integer.parseInt(evalutionMbrDto.getDmndSn()));

        int rtn = evalutionMapper.deleteMenteeSchedule(menteeReqDto);

        if (rtn > 0) {
            // 요청 성공일 경우에만 상호평가 내역 취소
            MtlEvlTxnDto mtlEvlTxnDto = new MtlEvlTxnDto();
            mtlEvlTxnDto.setMtlEvlStusCd(evalutionMbrDto.getMtlEvlStusCd()); // mtlEvlStusCd = "00005"
            mtlEvlTxnDto.setEvlDmndRtrcnRsnCd(evalutionMbrDto.getEvlDmndRtrcnRsnCd()); // evlDmndRtrcnRsnCd = '00001' //
                                                                                       // 평가자 불참일 경우
            mtlEvlTxnDto.setMtlEvlSn(evalutionMbrDto.getDelMtlEvlSn()); // 삭제할 상호평가 번호
            mtlEvlTxnDto.setEvlMbrId(evalutionMbrDto.getMbrId()); // 학습자id
            // 수정자는 인증된 사용자로 확정 — 취소 주체 추적용
            mtlEvlTxnDto.setMdfrId(member.getMbrId());

            // 상호평가 내역에 취소
            evalutionMapper.cancelMtlEvlTxnUpdate(mtlEvlTxnDto);

            RegReqEvalDto regReqEvalDto = new RegReqEvalDto();
            regReqEvalDto.setMtlEvlSn(evalutionMbrDto.getDelMtlEvlSn()); // 삭제할 상호평가 번호
            regReqEvalDto.setMbrId(evalutionMbrDto.getMbrId()); // 학습자아이디 -> 수정자아이디
            regReqEvalDto.setMtrScdlStusCd(Constants.MTL_SCDL_STTUS_CD_4); // 멘토일정상태코드 : 요청중("00001"), 수락(00002),
                                                                           // 거절(00003), 요청취소(00004)
            evalutionMapper.updateMtrEvlScdlBasByPk2(regReqEvalDto);

            // 빌더에서 Email과 Discord를 모두 생성하여 NotificationMessage 반환
            MtlEvlTxn mtlEvlTxn = mtlEvlTxnMapper.getMtlEvlTxn(mtlEvlTxnDto.getMtlEvlSn());
            EvaluationBas evlBas = evaluationAppendMapper.getEvaluationBasByEvlNo(mtlEvlTxn.getEvlNo());

            Map<String, Object> params = new HashMap<>();
            params.put("mtlEvlSn", mtlEvlTxn.getMtlEvlSn());
            params.put("projectNo", evlBas.getProjectNo());
            params.put("lcorsNo", evlBas.getLcorsNo());
            params.put("uqstnNo", evlBas.getUqstnNo());
            params.put("teamSn", evlBas.getTeamSn());

            AppendedEvaluationResponse evlBasAndMtlEvlTxn = evaluationAppendMapper.getEvlBasAndMtlEvlTxn(params);

            Member reviewerMember = memberMapper.getMember(mtlEvlTxnDto.getEvlMbrId());
            NotificationMtlEvlTxn notificationMtlEvlTxn2 = NotificationMtlEvlTxn.from(
                    mtlEvlTxn.getMtlEvlStusCd(),
                    mtlEvlTxn.getEvlrTypeCd(),
                    mtlEvlTxn.getMtlEvlPamBgngDt());

            String rgtrRoleCd = member.getRoleCd() != null ? member.getRoleCd().replaceFirst("^ROLE_", "") : null;

            // 알림 내용 구성
            String msg1 = messageSource.getMessage("usr.evreq.msg.cancelsched1", null, LocaleContextHolder.getLocale());
            String msg2 = messageSource.getMessage("usr.evreq.msg.cancelsched2", null, LocaleContextHolder.getLocale());
            String msg3 = messageSource.getMessage("usr.evreq.msg.cancelsched3", null, LocaleContextHolder.getLocale());
            String msg4 = messageSource.getMessage("usr.evreq.msg.cancelsched4", null, LocaleContextHolder.getLocale());
            String msg5 = messageSource.getMessage("usr.evreq.msg.cancelsched5", null, LocaleContextHolder.getLocale());
            String msg6 = messageSource.getMessage("usr.evreq.msg.cancelsched6", null, LocaleContextHolder.getLocale());
            String msg7 = messageSource.getMessage("usr.evreq.msg.cancelsched7", null, LocaleContextHolder.getLocale());
            String msg8 = messageSource.getMessage("usr.evreg.msg.cacnelschedRequester", null, LocaleContextHolder.getLocale());
            String msg9 = messageSource.getMessage("usr.evreg.msg.cacnelschedRequesterDiscordID", null, LocaleContextHolder.getLocale());

            String pstartCn = msg1 + HTML_BR +
                    msg2 + HTML_BR +
                    msg3 + HTML_BR +
                    msg8 + " " + member.getMbrNm() + "(" + member.getEmlAddr() + ")" + HTML_BR +
                    msg9 + " "
                    + (member.getDiscordId() != null && !member.getDiscordId().equals("") ? member.getDiscordId() : "-")
                    + HTML_BR +
                    msg7 + " " + evlBasAndMtlEvlTxn.getProjectNm() + HTML_BR +
                    msg4 + " " + evlBasAndMtlEvlTxn.getLcorsNm() + HTML_BR +
                    msg5 + " " + evlBasAndMtlEvlTxn.getUqstnNm() + HTML_BR +
                    msg6 + " " + mtlEvlTxn.getMtlEvlPamBgngDt();

            String pstartTitlNm = messageSource.getMessage("usr.evreq.msg.evlclcl.title", null, LocaleContextHolder.getLocale());

            // 공통 DTO로 변환
            NtcBbsBasDTO ntcBbsBasDTO = new NtcBbsBasDTO();
            ntcBbsBasDTO.setInstCd(member.getInstCd());
            ntcBbsBasDTO.setMbrId(reviewerMember.getMbrId());
            ntcBbsBasDTO.setRgtrId(member.getMbrId());
            ntcBbsBasDTO.setReadYn("N");
            ntcBbsBasDTO.setRoleCd(MBR_AUTH_CD_M);
            ntcBbsBasDTO.setRgtrRoleCd(rgtrRoleCd);
            ntcBbsBasDTO.setMdfrId(member.getMbrId());
            ntcBbsBasDTO.setPstartTitlNm(pstartTitlNm);
            ntcBbsBasDTO.setPstartCn(pstartCn);
            ntcBbsBasDTO.setNtcDivCd(NTC_DIV_CD_101);
            ntcBbsBasDTO.setSysDivCd(DM_SYS_DIV_CD_23);
            // 평가일정 알림게시판기본에 등록
            ntcBbsBasMapper.save(ntcBbsBasDTO);

            // 템플릿 조회 후 빌더 호출
            NotificationTemplate template = notificationTemplateMapper.getNotificationTemplate(TMPLT_CD_6);

            // Discord 알림 유형 메시지 구성
            String discordNotificationType = msg2;

            String notificationBody = msg3 + HTML_BR +
                    msg8 + " " + member.getMbrNm() + "(" + member.getEmlAddr() + ")" + HTML_BR +
                    msg9 + " "
                    + (member.getDiscordId() != null && !member.getDiscordId().equals("") ? member.getDiscordId() : "-")
                    + HTML_BR +
                    msg7 + " " + evlBasAndMtlEvlTxn.getProjectNm() + HTML_BR +
                    msg4 + " " + evlBasAndMtlEvlTxn.getLcorsNm() + HTML_BR +
                    msg5 + " " + evlBasAndMtlEvlTxn.getUqstnNm() + HTML_BR +
                    msg6 + " " + mtlEvlTxn.getMtlEvlPamBgngDt();

            NotificationMessage message = commonNotificationMessageBuilder.buildContentNotificationMessage(
                    template,
                    mtlEvlTxn.getMtlEvlSn(),
                    member,
                    reviewerMember,
                    member.getInstCd(),
                    MBR_AUTH_CD_M,
                    pstartTitlNm,
                    notificationBody,
                    discordNotificationType);

            applicationEventPublisher
                    .publishEvent(new NotificationEvent(EventBoundary.EVALUATION, EventDetail.CANCEL, message));

        } else {
            String msg = messageSource.getMessage("usr.evreq.msg.menticancelerror", null,
                    LocaleContextHolder.getLocale());
            throw new ValidCustomException(msg);
        }

        resultMap.put("returnCode", "MT00001");
        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 평가요청 목록 조회
     *
     * @param evalutionListDto
     * @return
     */
    public ResponseMessage searchList(Member member, EvalutionListDto evalutionListDto) {
        ResponseMessage responseMessage = new ResponseMessage();

        // Paging Start
        Criteria criteria = new Criteria();
        criteria.setPageNum(evalutionListDto.getPage());
        criteria.setPagePerRows(evalutionListDto.getPagePerRows());
        evalutionListDto.setCriteria(criteria);

        // 목록조회
        if (evalutionListDto.getMbrId() == null) {
            evalutionListDto.setMbrId(member.getMbrId());
        }
        if (evalutionListDto.getInstCd() == null) {
            evalutionListDto.setInstCd(member.getInstCd());
        }

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionListDto.setLanguage(language);

        List<EvalutionListDto> list = evalutionMapper.searchList(evalutionListDto);

        // 총건수 조회
        int allCount = evalutionMapper.searchListAllCnt(evalutionListDto);
        int total = 0;

        try {
            if (!list.isEmpty()) {
                total = list.get(0).getTotal();
            }
        } catch (RuntimeException e) {
            total = list.size();
        }

        Paginator paginator = new Paginator(criteria, total);
        // Paging End

        // ResultMap Start
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("paginator", paginator);
        resultMap.put("allCount", allCount);
        // ResultMap End

        responseMessage.setResult(resultMap);
        return responseMessage;
    }

    /**
     * 파일 업로드
     *
     * @param evalutionDataTxnDto
     * @return
     * @throws IOException
     */
    public ResponseMessage fileSave(EvalutionDataTxnDto evalutionDataTxnDto) throws IOException {
        ResponseMessage responseMessage = new ResponseMessage();
        String saveFileNm = StringUtils.EMPTY;

        if (!evalutionDataTxnDto.getFileInput().isEmpty() && evalutionDataTxnDto.getFileInput() != null) {
            MultipartFile file = evalutionDataTxnDto.getFileInput();
            // 파일 업로드 경로
            String prefix = "evalreq/";
            String fileResult = StringUtils.EMPTY;

            fileResult = awsS3Util.summerNote(prefix, file);
            if (fileResult == null) {
                throw new RuntimeException("파일 업로드에 실패했습니다.");
            }
            fileResult = fileResult.substring(fileResult.lastIndexOf("/") + 1);
            String fileUpdNm[] = fileResult.split("_");
            saveFileNm = fileUpdNm[0];
        } else {
            // file upload에 실패했습니다.
            throw new ValidCustomException(messageSource.getMessage("usr.evreq.msg.failtoregfileupload", null,
                    LocaleContextHolder.getLocale()));
        }

        responseMessage.setResult(saveFileNm);
        return responseMessage;
    }

    /**
     * 마지막 평가 요청 조회
     *
     * @param evalutionListDto
     * @return
     */
    public List<EvalutionListDto> getLastEvalutionList(Member member, EvalutionListDto evalutionListDto) {
        // Paging Start
        Criteria criteria = new Criteria();
        criteria.setPageNum(evalutionListDto.getPage());
        criteria.setPagePerRows(evalutionListDto.getPagePerRows());
        evalutionListDto.setCriteria(criteria);

        // 목록조회
        evalutionListDto.setMbrId(member.getMbrId());
        evalutionListDto.setInstCd(member.getInstCd());

        String language = "en".equals(LocaleContextHolder.getLocale().toString()) ? "en" : "ko"; // Locale 객체 가져오기
        evalutionListDto.setLanguage(language);

        return evalutionMapper.searchList(evalutionListDto);
    }

    private String fetchReadmeContent(Member member, String githubUrl, String branchName) {

        String readmeContent = "";

        if (githubUrl == null || githubUrl.isEmpty()) {
            return readmeContent;
        }

        try {
            String repoName = "";
            if (githubUrl.contains("github.com")) {
                String[] parts = githubUrl.split("/");
                repoName = parts[parts.length - 1];
            }

            if (repoName.isEmpty()) {
                return readmeContent;
            }

            if( member.getGithubId() != null && !member.getGithubId().isEmpty() ) {
                try {
                    member.setDecGithubId( aes256Util.decrypt( member.getGithubId() ) );
                } catch( GeneralSecurityException e ) {
                    throw new RuntimeException( e );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }
            // 토큰은 복호화하지 않는다 — 아래 GitHub 호출에 암호문을 그대로 넘긴다.

            String githubId = member.getDecGithubId();
            if (githubId == null || githubId.isEmpty()) {
                return readmeContent;
            }

            // 재단에서 운영으로 README.md 파일을 사용하기로 했기에 변동이 있을 경우 기능 수정 필요함
            String apiUrl = "/repos/" + githubId + "/" + repoName + "/contents/README.md";

            GitHubInfoDto gitHubInfoDto = new GitHubInfoDto();
            gitHubInfoDto.setGithubId(member.getGithubId());
            gitHubInfoDto.setGithubToken(member.getGithubToken());
            gitHubInfoDto.setGithubUrl(apiUrl);

            ResponseMessage githubResult = githubService.getBranchFileContent(gitHubInfoDto);
            if (githubResult != null && githubResult.getResult() != null) {
                JSONObject result = (JSONObject) githubResult.getResult();
                JSONObject data = (JSONObject) result.get("branchFileContent");
                if (data != null && data.get("content") != null) {
                    String encodeReadmeContent = data.get("content").toString().replaceAll("\\s", "");
                    byte[] decodedBytes = Base64.getDecoder().decode(encodeReadmeContent);
                    readmeContent = new String(decodedBytes, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch README from GitHub: {}", githubUrl, e);
        }

        return readmeContent;
    }

    private String buildApiParam(String standardCode, String learnerCode) {
        try {
            Map<String, Object> paramMap = new LinkedHashMap<>();
            paramMap.put("stream", false);
            paramMap.put("standard_code", standardCode != null ? standardCode : "");
            paramMap.put("learner_code", learnerCode != null ? learnerCode : "");
            return new ObjectMapper().writeValueAsString(paramMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build API param JSON", e);
        }
    }

}
