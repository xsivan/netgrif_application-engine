package com.netgrif.workflow.startup

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class FilterRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    private static final String FILTER_FILE_NAME = "engine-processes/filter.xml";
    public static final String FILTER_PETRI_NET_IDENTIFIER = "filter"

    private static final String PREFERRED_FILTER_ITEM_FILE_NAME = "engine-processes/preference_filter_item.xml"
    private static final String PREFERRED_FILTER_ITEM_NET_IDENTIFIER = "preference_filter_item"

    @Override
    void run(String... args) throws Exception {
        importProcess("Petri net for filters", FILTER_PETRI_NET_IDENTIFIER, FILTER_FILE_NAME)
        importProcess("Petri net for filter preferences", PREFERRED_FILTER_ITEM_NET_IDENTIFIER, PREFERRED_FILTER_ITEM_FILE_NAME)
    }

    Optional<PetriNet> importProcess(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return new Optional<>(filter)
        }

        Optional<PetriNet> filterNet = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.loggedSystem)

        if (!filterNet.isPresent()) {
            log.error("Import of ${message} failed!")
        }

        return filterNet
    }
}
