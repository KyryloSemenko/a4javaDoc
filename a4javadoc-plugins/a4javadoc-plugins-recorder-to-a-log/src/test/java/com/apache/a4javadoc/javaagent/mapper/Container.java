package com.apache.a4javadoc.javaagent.mapper;

import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * For tests purposes only.
 * @author Kyrylo Semenko
 */
public class Container implements Comparable<Container> {
    /** List of strings for tests purposes */
    private List<String> listOfStrings;
    
    /** String for tests purposes */
    private String string;
    
    /** Object for tests purposes */
    private Object objectField;
    
    
    /** Primitive int for tests purposes */
    private int intField;

    /** List without generic type for tests purposes */
    @SuppressWarnings("rawtypes")
    private List listWithoutGenericType;
    
    /** 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    /** 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    
    /** 
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(Container other) {
        return CompareToBuilder.reflectionCompare(this, other);
    }
    
    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    /** @return The {@link Container#listOfStrings} field */
    public List<String> getListOfStrings() {
        return listOfStrings;
    }

    /** @param listOfStrings see the {@link Container#listOfStrings} field */
    public void setListOfStrings(List<String> listOfStrings) {
        this.listOfStrings = listOfStrings;
    }

    /** @return The {@link Container#string} field */
    public String getString() {
        return string;
    }

    /** @param string see the {@link Container#string} field */
    public void setString(String string) {
        this.string = string;
    }

    /** @return The {@link Container#objectField} field */
    public Object getObjectField() {
        return objectField;
    }

    /** @param objectField see the {@link Container#objectField} field */
    public void setObjectField(Object objectField) {
        this.objectField = objectField;
    }

    /** @return The {@link Container#intField} field */
    public int getIntField() {
        return intField;
    }

    /** @param intField see the {@link Container#intField} field */
    public void setIntField(int intField) {
        this.intField = intField;
    }

    /** @return The {@link Container#listWithoutGenericType} field */
    @SuppressWarnings("rawtypes")
    public List getListWithoutGenericType() {
        return listWithoutGenericType;
    }

    /** @param listWithoutGenericType see the {@link Container#listWithoutGenericType} field */
    public void setListWithoutGenericType(@SuppressWarnings("rawtypes") List listWithoutGenericType) {
        this.listWithoutGenericType = listWithoutGenericType;
    }

}
