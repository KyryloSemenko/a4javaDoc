package com.apache.a4javadoc.javaagent.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.agent.namefilter.NameFilterService;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Singleton. Contains the {@link #matches(MethodDescription)} method.
 * @author Kyrylo Semenko
 * @param <T> see {@link MethodDescription}
 */
public class MethodsMatcher<T extends MethodDescription> extends ElementMatcher.Junction.AbstractBase<T> {
    
    /** Separates parameters names within brackets */
    public static final String METHOD_PARAMETERS_SEPARATOR = ",";

    private static final Logger logger = LoggerFactory.getLogger(MethodsMatcher.class);
    
    private static MethodsMatcher<? super MethodDescription> instance;
    
    /**
     * The static factory.
     * @return a singleton instance
     */
    public static MethodsMatcher<? super MethodDescription> getInstance() {
        if (instance == null) {
            instance = new MethodsMatcher<MethodDescription>();
        }
        return instance;
    }
    
    /** An empty constructor with a log message */
    private MethodsMatcher() {
        logger.info("Construction of MethodsMatcher started");
    }

    /** 
     * @see net.bytebuddy.matcher.ElementMatcher#matches(java.lang.Object)
     */
    @Override
    public boolean matches(T target) {
        StringBuilder stringBuilder = new StringBuilder()
            .append(target.getDeclaringType().getTypeName())
            .append(".")
            .append(target.getInternalName())
            .append("(");
        for (int i = 0; i < target.getParameters().size(); i++) {
            ParameterDescription.AbstractBase parameter = (ParameterDescription.AbstractBase) target.getParameters().get(i);
            stringBuilder.append(parameter.getType().getTypeName());
            if (target.getParameters().size() - 1 > i) {
                stringBuilder.append(METHOD_PARAMETERS_SEPARATOR);
            }
        }
        stringBuilder.append(")");
        
        boolean matches = NameFilterService.getInstance().matches(stringBuilder.toString());
        
        if (logger.isTraceEnabled() && matches) {
            logger.trace("The method will be intercepted and instrumented '{}'", stringBuilder.toString());
        }
        
        return matches;
    }

}
