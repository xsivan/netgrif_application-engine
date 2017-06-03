package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Place;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Document
public class Case {
    @Id
    private ObjectId _id;
    @DBRef
    @NotNull
    @JsonIgnore
    private PetriNet petriNet;
    @Field("activePlaces")
    private Map<String, Integer> activePlaces;
    @NotNull
    private String title;
    private String color;
    private Map<String, Object> dataSetValues;

    public Case() {
        _id = new ObjectId();
        activePlaces = new HashMap<>();
        dataSetValues = new HashMap<>();
    }

    public Case(String title) {
        this();
        this.title = title;
    }

    public Case(String title, PetriNet petriNet, Map<String, Integer> activePlaces) {
        this(title);
        this.petriNet = petriNet;
        this.activePlaces = activePlaces;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getStringId() {
        return _id.toString();
    }

    public PetriNet getPetriNet() {
        if (petriNet.isNotInitialized())
            petriNet.initializeArcs();
        return petriNet;
    }

    public void setPetriNet(PetriNet petriNet) {
        this.petriNet = petriNet;
    }

    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    public void setActivePlaces(Map<String, Integer> activePlaces) {
        this.activePlaces = activePlaces;
    }

    public void addActivePlace(String placeId, Integer tokens) {
        this.activePlaces.put(placeId, tokens);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color == null || color.isEmpty() ? "color-fg-fm-500" : color;
    }

    public Map<String, Object> getDataSetValues() {
        return dataSetValues;
    }

    public void setDataSetValues(Map<String, Object> dataSetValues) {
        this.dataSetValues = dataSetValues;
    }

    private void addTokensToPlace(Place place, Integer tokens) {
        Integer newTokens = tokens;
        String id = place.getStringId();
        if (activePlaces.containsKey(id))
            newTokens += activePlaces.get(id);
        activePlaces.put(id, newTokens);
    }

    public void updateActivePlaces() {
        activePlaces = petriNet.getActivePlaces();
    }

    private void removeTokensFromActivePlace(Place place, Integer tokens) {
        String id = place.getStringId();
        activePlaces.put(id, activePlaces.get(id) - tokens);
    }

    private boolean isNotActivePlace(Place place) {
        return !isActivePlace(place);
    }
    private boolean isActivePlace(Place place) {
        return activePlaces.containsKey(place.getStringId());
    }
}