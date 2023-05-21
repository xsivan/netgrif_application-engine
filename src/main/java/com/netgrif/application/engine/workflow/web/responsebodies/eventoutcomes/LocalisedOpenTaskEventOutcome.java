package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.OpenTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

/**
 * Represent outcome after performing 'opentask' event with Localised info.
 * */
public class LocalisedOpenTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedOpenTaskEventOutcome(OpenTaskEventOutcome outcome, Locale locale)  {
        super(outcome, locale);
    }
}
