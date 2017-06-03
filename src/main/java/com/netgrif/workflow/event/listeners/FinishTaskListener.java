package com.netgrif.workflow.event.listeners;

import com.netgrif.workflow.event.events.UserFinishTaskEvent;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FinishTaskListener {

    @Autowired
    private ITaskService taskService;

    @EventListener(condition = "#event.user != null")
    public void onUserEvent(UserFinishTaskEvent event) {

    }
}