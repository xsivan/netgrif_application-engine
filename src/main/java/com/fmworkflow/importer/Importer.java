package com.fmworkflow.importer;

import com.fmworkflow.importer.model.*;
import com.fmworkflow.petrinet.domain.*;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.service.ArcFactory;
import com.fmworkflow.petrinet.service.FieldFactory;
import com.fmworkflow.petrinet.domain.dataset.logic.Editable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class Importer {
    private Document document;
    private PetriNet net;
    private Map<Long, ProcessRole> roles;
    private Map<Long, Field> fields;
    private Map<Long, Transition> transitions;
    private Map<Long, Place> places;

    @Autowired
    private PetriNetRepository repository;

    public Importer() {
        this.roles = new HashMap<>();
        this.transitions = new HashMap<>();
        this.places = new HashMap<>();
        this.fields = new HashMap<>();
    }

    public void importPetriNet(File xml, String title, String initials) {
        try {
            unmarshallXml(xml);
            createPetriNet(title, initials);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void unmarshallXml(File xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        document = (Document) jaxbUnmarshaller.unmarshal(xml);
    }

    private void createPetriNet(String title, String initials) {
        net = new PetriNet();
        net.setTitle(title);
        net.setInitials(initials);

        Arrays.stream(document.getImportData()).forEach(this::createDataSet);
        Arrays.stream(document.getImportRoles()).forEach(this::createRole);
        Arrays.stream(document.getImportPlaces()).forEach(this::createPlace);
        Arrays.stream(document.getImportTransitions()).forEach(this::createTransition);
        Arrays.stream(document.getImportArc()).forEach(this::createArc);

        repository.save(net);
    }

    private void createArc(ImportArc importArc) {
        Arc arc = ArcFactory.getArc(importArc.getType());
        arc.setMultiplicity(importArc.getMultiplicity());
        arc.setSource(getNode(importArc.getSourceId()));
        arc.setDestination(getNode(importArc.getDestinationId()));

        net.addArc(arc);
    }

    private void createDataSet(ImportData importData) {
        Field field = FieldFactory.getField(importData.getType());
        field.setName(importData.getTitle());

        net.addDataSetField(field);
        fields.put(importData.getId(), field);
    }

    private void createTransition(ImportTransition importTransition) {
        Transition transition = new Transition();
        transition.setTitle(importTransition.getLabel());
        transition.setPosition(importTransition.getX(), importTransition.getY());

        if (importTransition.getRoleRef() != null)
            Arrays.stream(importTransition.getRoleRef()).forEach(roleRef ->
                    // TODO: 18/02/2017 add Role logic
                    transition.addRole(roles.get(roleRef.getId()).getObjectId(), null)
            );
        if (importTransition.getDataRef() != null)
            Arrays.stream(importTransition.getDataRef()).forEach(dataRef ->
                    // TODO: 18/02/2017 add Field logic
                    transition.addDataSet(fields.get(dataRef.getId()).getObjectId(), new Editable())
            );

        net.addTransition(transition);
        transitions.put(importTransition.getId(), transition);
    }

    private void createPlace(ImportPlace importPlace) {
        Place place = new Place();
        place.setStatic(importPlace.getIsStatic());
        place.setTokens(importPlace.getTokens());
        place.setPosition(importPlace.getX(), importPlace.getY());
        place.setTitle(importPlace.getLabel());

        net.addPlace(place);
        places.put(importPlace.getId(), place);
    }

    private void createRole(ImportRole importRole) {
        ProcessRole role = new ProcessRole();
        role.setName(importRole.getName());

        net.addRole(role);
        roles.put(importRole.getId(), role);
    }

    private Node getNode(Long id) {
        // TODO: 18/02/2017 maybe throw exception if transitions doesn't contain id
        if (places.containsKey(id))
            return places.get(id);
        else
            return transitions.get(id);
    }
}
