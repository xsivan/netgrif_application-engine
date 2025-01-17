package com.netgrif.application.engine.history.domain.caseevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "eventLog")
public class DeleteCaseEventLog extends CaseEventLog {

    public DeleteCaseEventLog(Case useCase, EventPhase eventPhase) {
        super(useCase, eventPhase);
    }
}