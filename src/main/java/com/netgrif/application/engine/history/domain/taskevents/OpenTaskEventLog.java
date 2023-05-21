package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Getter;

/**
 * Class represent long into DB after 'opentask' event is performed. Extends from OpenTaskEventLog class.
 * */
public class OpenTaskEventLog extends TaskEventLog {

    @Getter
    private String userId;

    public OpenTaskEventLog(Task task, Case useCase, EventPhase eventPhase, String userId) {
        super(task, useCase, eventPhase);
        this.userId = userId;
    }
}
