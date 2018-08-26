package com.apache.a4javadoc.javaagent.mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This value object represents an object identifier part, for example {@code "java.util.Arrays$ArrayList<java.lang.String>"} see {@link IdentifierService}.
 * @author Kyrylo Semenko
 */
public class ContainerType {
    
    /** {@link Class#getCanonicalName()} */
    private String className;
    
    /** Can be empty if the object is not array nor generic */
    private List<ContainerType> containerTypes;
    
    /** 
     * This constructor initializes an empty {@link #containerTypes}.
     */
    public ContainerType() {
        setContainerTypes(new ArrayList<ContainerType>());
    }
    
    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    /** 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }
    
    /** 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherObject) {
        return EqualsBuilder.reflectionEquals(this, otherObject);
    }

    /** @return The {@link ContainerType#className} field */
    public String getClassName() {
        return className;
    }

    /** @param className see the {@link ContainerType#className} field */
    public void setClassName(String className) {
        this.className = className;
    }

    /** @return The {@link ContainerType#containerTypes} field */
    public List<ContainerType> getContainerTypes() {
        return containerTypes;
    }

    /** @param containerTypes see the {@link ContainerType#containerTypes} field */
    public void setContainerTypes(List<ContainerType> containerTypes) {
        this.containerTypes = containerTypes;
    }

}
