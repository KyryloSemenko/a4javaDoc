package com.apache.a4javadoc.javaagent.mapper;

/** An object with wrapped dependency. For test purposes. */
public class WrapperClass {
    private long id;
    private WrapperClass parent;
    
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
}
