package com.fmworkflow.workflow.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;

@Document
@Entity(name = "task_trigger")
public abstract class Trigger {
    @Id
    protected ObjectId _id;

    @javax.persistence.Id
    protected Long id;

    public String getObjectId() {
        return _id.toString();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public enum Type {
        AUTO ("auto"),
        MESSAGE ("message"),
        TIME ("time"),
        USER ("user");

        String name;

        Type(String name) {
            this.name = name;
        }

        public static Type fromString(String name) {
            return Type.valueOf(name.toUpperCase());
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }
}