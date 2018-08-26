package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** 
 * This value object contains information about instance of object, for example {@code "java.util.Arrays$ArrayList<java.lang.String>@fe2"}.<br>
 * The identifier can be simple, for example {@code "int@1"} or more complex, for example {@code "java.util.LinkedHashMap<java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>@27df"}
 * @author Kyrylo Semenko
 */
public class Identifier {
    
    /** The hash code, for example <b>fe2</b> in the {@code "java.util.Arrays$ArrayList<java.lang.String>@fe2"} identifier. */
    private String hash;
    
    /**
     * Cannot be 'null', can be empty.<br>
     * The {@link Identifier} includes only one {@link ContainerType}.
     * And the {@link ContainerType} can include one or more inner {@link ContainerType}s if the object is an {@link Array} or {@link ParameterizedType}.<br>
     * Else it contains an empty list of {@link ContainerType}s.<br>
     * Example of {@link Identifier} without inner {@link ContainerType}s
     * <pre>{@code "java.lang.String@fe2"}</pre>
     * 
     * Example of {@link Identifier} with one inner {@link ContainerType}
     * <pre>{@code "java.util.Arrays$ArrayList<java.lang.String>@fe2"}</pre>
     * 
     * Example of {@link Identifier} with multiple inner {@link ContainerType}s
     * <pre>{@code "java.util.LinkedHashMap<java.lang.String,java.util.LinkedHashMap<java.lang.String,java.lang.Integer>>@fe2"}</pre>
     */
    private ContainerType containerType;
    
    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /** @return The {@link Identifier#hash} field */
    public String getHash() {
        return hash;
    }

    /** @param hash see the {@link Identifier#hash} field */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /** @return The {@link Identifier#containerType} field */
    public ContainerType getContainerType() {
        return containerType;
    }

    /** @param containerType see the {@link Identifier#containerType} field */
    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

}
