package kr.innoaca.usr.web.ev.request.dto;

import kr.innoaca.usr.evaluation.EvaluationBas;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class EvaluationAppendResponse implements Serializable {
    private Long evlNo;
    private Integer evlDegr;

    private Long projectNo;
    private Long lcorsNo;
    private Long uqstnNo;

    private Integer teamSn;

    public EvaluationAppendResponse(EvaluationBas evaluationBas) {
        this.evlNo = evaluationBas.getEvlNo();
        this.evlDegr = evaluationBas.getEvlDegr();
        this.projectNo = evaluationBas.getProjectNo();
        this.lcorsNo = evaluationBas.getLcorsNo();
        this.uqstnNo = evaluationBas.getUqstnNo();
        this.teamSn = evaluationBas.getTeamSn();
    }
}
