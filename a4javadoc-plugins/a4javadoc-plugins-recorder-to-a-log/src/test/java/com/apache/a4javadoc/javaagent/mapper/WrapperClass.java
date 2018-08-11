package com.apache.a4javadoc.javaagent.mapper;

import java.util.List;

/** An object with wrapped dependency. For test purposes. */
public class WrapperClass {
    /** Primitive long for tests purposes */
    private long id;
    
    /** {@link WrapperClass} as for a circular dependency test */
    private WrapperClass parent;
    
    /** Primitive boolean for tests purposes */
    private boolean booleanValue;

    /** List of {@link String}s without getter and setter for tests purposes */
    public List<String> nullWithoutGetterAndSetter;
    
    /** @return The {@link WrapperClass#id} field */
    public long getId() {
        return id;
    }
    
    /** @param id see the {@link WrapperClass#id} field */
    public void setId(long id) {
        this.id = id;
    }
    
    /** @return The {@link WrapperClass#parent} field */
    public WrapperClass getParent() {
        return parent;
    }
    
    /** @param parent see the {@link WrapperClass#parent} field */
    public void setParent(WrapperClass parent) {
        this.parent = parent;
    }

    /** @return The {@link WrapperClass#booleanValue} field */
    public boolean isBooleanValue() {
        return booleanValue;
    }

    /** @param booleanValue see the {@link WrapperClass#booleanValue} field */
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
