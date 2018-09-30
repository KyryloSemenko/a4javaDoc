package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * This value object represents an object identifier part, for example
 * {@code "java.util.Arrays$ArrayList<java.lang.String>"} see
 * {@link IdentifierService}.
 * 
 * @author Kyrylo Semenko
 */
public class ContainerType {

    /**
     * {@link Class} of the object represented by this {@link ContainerType}
     */
    private Class<?> objectClass;

    /**
     * Some objects with generic types contains a method, by which it is
     * possible to iterate enclosing items. This method name is used for
     * serialization of the object, but not used for deserialization.
     */
    private Method disassembleMethod;

    /**
     * The factory {@link Method} name is used for instantiation of an object.
     * This {@link Method#getName()} is found out during serialization.
     */
    private String factory;

    /**
     * A method name. This method will be used in serializer for filling out a
     * deserialized object by generic items.
     */
    private String filling;

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

    /** Can be empty if the object is not array nor generic */
    private List<ContainerType> containerTypes;

    /** @return The {@link ContainerType#objectClass} field */
    public Class<?> getObjectClass() {
        return objectClass;
    }

    /** @param objectClass see the {@link ContainerType#objectClass} field */
    public void setObjectClass(Class<?> objectClass) {
        this.objectClass = objectClass;
    }

    /** @return The {@link ContainerType#disassembleMethod} field */
    public Method getDisassembleMethod() {
        return disassembleMethod;
    }

    /** @param disassembleMethod see the {@link ContainerType#disassembleMethod} field */
    public void setDisassembleMethod(Method disassembleMethod) {
        this.disassembleMethod = disassembleMethod;
    }

    /** @return The {@link ContainerType#factory} field */
    public String getFactory() {
        return factory;
    }

    /** @param factory see the {@link ContainerType#factory} field */
    public void setFactory(String factory) {
        this.factory = factory;
    }

    /**
     * This constructor initializes an empty {@link #containerTypes}.
     */
    public ContainerType() {
        setContainerTypes(new ArrayList<ContainerType>());
    }

    /** @return The {@link ContainerType#containerTypes} field */
    public List<ContainerType> getContainerTypes() {
        return containerTypes;
    }

    /**
     * @param containerTypes see the {@link ContainerType#containerTypes} field
     */
    public void setContainerTypes(List<ContainerType> containerTypes) {
        this.containerTypes = containerTypes;
    }

    /** @return The {@link ContainerType#filling} field */
    public String getFilling() {
        return filling;
    }

    /** @param filling see the {@link ContainerType#filling} field */
    public void setFilling(String filling) {
        this.filling = filling;
    }

}
