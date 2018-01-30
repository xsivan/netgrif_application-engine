package com.netgrif.workflow.petrinet.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.web.responsebodies.DataFieldReference;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface IPetriNetService {

    Optional<PetriNet> importPetriNet(File xmlFile, String name, String initials, LoggedUser user) throws IOException, SAXException, ParserConfigurationException;

    void savePetriNet(PetriNet petriNet);

    PetriNet loadPetriNet(String id);

    List<PetriNet> loadAll();

    List<PetriNetReference> getAllReferences(LoggedUser user, Locale locale);

    List<PetriNetReference> getAllAccessibleReferences(LoggedUser user, Locale locale);

    PetriNetReference getReferenceByTitle(LoggedUser user, String title, Locale locale);

    List<TransitionReference> getTransitionReferences(List<String> netsIds, LoggedUser user, Locale locale);

    List<DataFieldReference> getDataFieldReferences(List<String> petriNetIds, List<String> transitionIds, Locale locale);
}
