package com.netgrif.application.engine.history.domain.taskevents;

import com.netgrif.application.engine.history.domain.caseevents.CaseEventLog;
import com.netgrif.application.engine.petrinet.domain.events.EventPhase;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class TaskEventLog extends CaseEventLog {

    @Getter
    private String taskId;

    @Getter
    private String taskTitle;

    @Getter
    private String transitionId;

    protected TaskEventLog(Task task, Case useCase, EventPhase eventPhase) {
        super(task.getObjectId(), useCase, eventPhase);
        this.taskId = task.getStringId();
        this.taskTitle = task.getTitle().toString();
        this.transitionId = task.getTransitionId();
    }
}