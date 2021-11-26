package com.netgrif.workflow.workflow.domain.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class wraps and holds list of predicates.
 * In the xml structure class is represented by <predicateMetadataItem> tag.
 * Same as the FilterMetadataExport class, this one needs to be converted into
 * map object while importing filter too.
 */
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class PredicateArray {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "predicate")
    protected List<Predicate> predicates;

    public PredicateArray(List<Object> value) {
        predicates = new ArrayList<>();
        for (Object val : value) {
            predicates.add(new Predicate((Map<String, Object>) val));
        }
    }

    public PredicateArray(Map<String, Object> value) {
        predicates = new ArrayList<>();
        value.forEach((k, v) -> {
            for (Object val : ((Collection<?>) v)) {
                predicates.add(new Predicate((Map<String, Object>) val));
            }
        });
    }

    @JsonIgnore
    public List<Object> getMapObject() {
        List<Object> mapObject = new ArrayList<>();
        for (Predicate val : predicates) {
            mapObject.add(val.getMapObject());
        }
        return mapObject;
    }
}
