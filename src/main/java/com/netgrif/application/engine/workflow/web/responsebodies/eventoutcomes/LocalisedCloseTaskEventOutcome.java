  package com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes;

import com.netgrif.application.engine.workflow.domain.eventoutcomes.taskoutcomes.CloseTaskEventOutcome;
import com.netgrif.application.engine.workflow.web.responsebodies.eventoutcomes.base.LocalisedTaskEventOutcome;

import java.util.Locale;

  /**
   * Represent outcome after performing 'closetask' event with Localised info.
   * */
public class LocalisedCloseTaskEventOutcome extends LocalisedTaskEventOutcome {

    public LocalisedCloseTaskEventOutcome(CloseTaskEventOutcome outcome, Locale locale)  {
        super(outcome, locale);
    }

}