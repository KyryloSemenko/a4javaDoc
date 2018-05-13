package com.apache.a4javadoc.javaagent.mapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/** An object with circular dependency. For test purposes. */
// TODO Kyrylo Semenko Create a serializer to resolve circular dependencies without this annotation
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class CircularClass {
    private long id;
    private CircularClass parent;
    
    /** @return The {@link CircularClass#id} field */
    public long getId() {
        return id;
    }
    
    /** @param id see the {@link CircularClass#id} field */
    public void setId(long id) {
        this.id = id;
    }
    
    /** @return The {@link CircularClass#parent} field */
    public CircularClass getParent() {
        return parent;
    }
    
    /** @param parent see the {@link CircularClass#parent} field */
    public void setParent(CircularClass parent) {
        this.parent = parent;
    }
}
