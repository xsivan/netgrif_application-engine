package com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;

import java.util.List;

/**
 * Class represent output data (outcome) after performing "opentask" event. Extends from TaskEventOutcome class.
 * */
@Data
public class OpenTaskEventOutcome extends TaskEventOutcome {

    public OpenTaskEventOutcome() {
        super();
    }

    public OpenTaskEventOutcome(Case useCase, Task task) {
        super(useCase, task);
    }

    public OpenTaskEventOutcome(Case useCase, Task task, List<EventOutcome> outcomes) {
        this(useCase, task);
        setOutcomes(outcomes);
    }
}
