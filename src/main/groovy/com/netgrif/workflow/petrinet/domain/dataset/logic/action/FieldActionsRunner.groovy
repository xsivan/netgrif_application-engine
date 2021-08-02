package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.business.IPostalCodeService
import com.netgrif.workflow.business.orsr.IOrsrService
import com.netgrif.workflow.event.IGroovyShellFactory
import com.netgrif.workflow.importer.service.FieldFactory
import com.netgrif.workflow.petrinet.domain.Function
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.Task
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Lookup
import org.springframework.stereotype.Component

@Component
@SuppressWarnings("GrMethodMayBeStatic")
abstract class FieldActionsRunner {

    private static final Logger log = LoggerFactory.getLogger(FieldActionsRunner.class)

    @Lookup("actionDelegate")
    abstract ActionDelegate getActionDeleget()

    @Autowired
    private IOrsrService orsrService

    @Autowired
    private IPostalCodeService postalCodeService

    @Autowired
    private FieldFactory fieldFactory

    @Autowired
    private IGroovyShellFactory shellFactory

    private Map<String, Object> actionsCache = new HashMap<>()
    private Map<String, Closure> actions = new HashMap<>()

    ChangedFieldsTree run(Action action, Case useCase, List<Function> functions) {
        return run(action, useCase, Optional.empty(), functions)
    }

    ChangedFieldsTree run(Action action, Case useCase, Optional<Task> task, List<Function> functions) {
        if (!actionsCache)
            actionsCache = new HashMap<>()

        log.debug("Action: $action")
        def code = getActionCode(action, functions)
        try {
            code.init(action, useCase, task, this)
            code()
        } catch (Exception e) {
            log.error("Action: $action.definition")
            throw e
        }
        return ((ActionDelegate) code.delegate).changedFieldsTree
    }

    Closure getActionCode(Action action, List<Function> functions) {
        def code
        if (actions.containsKey(action.importId)) {
            code = actions.get(action.importId)
        } else {
            code = (Closure) this.shellFactory.getGroovyShell().evaluate("{-> ${action.definition}}")
            actions.put(action.importId, code)
        }
        def actionDelegate = getActionDeleget()
        if (functions) {
            def shell = this.shellFactory.getGroovyShell()
            functions.each {
                actionDelegate.metaClass."${it.name}" = (Closure) shell.evaluate(it.definition)
            }
        }
        return code.rehydrate(actionDelegate, code.owner, code.thisObject)
    }

    void addToCache(String key, Object value) {
        this.actionsCache.put(key, value)
    }

    void removeFromCache(String key) {
        this.actionsCache.remove(key)
    }

    def getFromCache(String key) {
        return this.actionsCache.get(key)
    }

    IPostalCodeService getPostalCodeService() {
        return postalCodeService
    }

    IOrsrService getOrsrService() {
        return orsrService
    }
}