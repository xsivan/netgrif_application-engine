package com.netgrif.workflow.event.events.task;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;

public class UserAssignTaskEvent extends UserTaskEvent{

    public UserAssignTaskEvent(User user, Task task, Case useCase) {
        super(user, task, useCase);
    }

    @Override
    public String getMessage() {
        return "Používateľ " + getEmail() + " si priradil úlohu " + getTask().getTitle() + " na prípade " + getUseCase().getTitle();
    }
}