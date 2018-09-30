package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * This value object contains information about instance of an object,
 * see the {@link #containerType} field description.
 * 
 * <p>
 * This object created before serialization and contains analysis
 * of a source object state, see the {@link #containerType} field
 * and requirement of serialization of itself to JSON, see the
 * {@link #requiresToBeIncludedInJson} field.
 * 
 * @author Kyrylo Semenko
 */
public class Identifier {
    
    /**
     * <p>
     * Cannot be 'null', can be empty.
     * 
     * <p>
     * The {@link Identifier} includes only one {@link ContainerType}.
     * 
     * <p>
     * This root {@link ContainerType} can include zero or more
     * inner {@link ContainerType}s.
     * 
     * <p>
     * If a serialized object is not generic nor {@link Array}, the root
     * {@link ContainerType} will be empty.
     * 
     * <p>
     * If the serialized object is an {@link Array} or one
     * {@link ParameterizedType}, for example {@link List},
     * the root {@link ContainerType} will contain
     * a single inner {@link ContainerType}
     * in the {@link ContainerType#getContainerTypes()} field.
     * 
     * <p>
     * If the serialized object contains more
     * {@link ParameterizedType}s, for example {@link Map},
     * the root {@link ContainerType} will contain as many objects, as the
     * sourceObject has {@link ParameterizedType}s.
     * 
     * <p>
     * Example of {@link Identifier} without inner {@link ContainerType}s
     * 
     * <pre>
        "_a4id": {
            "containerType": {
                "objectClass": "com.apache.a4javadoc.javaagent.mapper.Container",
                "containerTypes": []
            }
        }
     * </pre>
     * 
     * Example of {@link Identifier} with one inner {@link ContainerType}
     * 
     * <pre>
        "_a4id": {
            "containerType": {
                "objectClass": "java.util.List",
                "containerTypes": [{
                    "objectClass": "java.lang.String",
                    "containerTypes": []
                }]
            }
        }
     * </pre>
     * 
     * Example of {@link Identifier} with multiple inner {@link ContainerType}s
     * 
     * <pre>
        "_a4id": {
            "containerType": {
                "objectClass": "java.util.LinkedHashMap",
                "containerTypes": [{
                    "objectClass": "java.lang.String",
                    "containerTypes": []
                },
                {
                    "objectClass": "java.util.LinkedHashMap",
                    "containerTypes": [{
                        "objectClass": "java.lang.String",
                        "containerTypes": []
                    },
                    {
                        "objectClass": "java.lang.Integer",
                        "containerTypes": []
                    }]
                }]
            }
        }
     * </pre>
     */
    private ContainerType containerType;
    
    /**
     * If 'true', this {@link Identifier} has to be included to JSON.
     */
    private boolean requiresToBeIncludedInJson;
    
    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    /** @return The {@link Identifier#containerType} field */
    public ContainerType getContainerType() {
        return containerType;
    }

    /** @param containerType see the {@link Identifier#containerType} field */
    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    /** @return The {@link Identifier#requiresToBeIncludedInJson} field */
    public boolean isRequiresToBeIncludedInJson() {
        return requiresToBeIncludedInJson;
    }

    /** @param requiresToBeIncludedInJson see the {@link Identifier#requiresToBeIncludedInJson} field */
    public void setRequiresToBeIncludedInJson(boolean requiresToBeIncludedInJson) {
        this.requiresToBeIncludedInJson = requiresToBeIncludedInJson;
    }

}
