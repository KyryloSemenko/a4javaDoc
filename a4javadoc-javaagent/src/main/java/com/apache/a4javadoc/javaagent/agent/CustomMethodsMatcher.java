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
public class CustomMethodsMatcher<T extends MethodDescription> extends ElementMatcher.Junction.AbstractBase<T> {
    private static final Logger logger = LoggerFactory.getLogger(CustomMethodsMatcher.class);
    
    private static CustomMethodsMatcher<? super MethodDescription> instance;
    
    /**
     * The static factory.
     * @return a singleton instance
     */
    public static CustomMethodsMatcher<? super MethodDescription> getInstance() {
        if (instance == null) {
            instance = new CustomMethodsMatcher<>();
        }
        return instance;
    }
    
    /** An empty constructor with a log message */
    private CustomMethodsMatcher() {
        logger.info("Construction of CustomMethodsMatcher started");
    }

    /** 
     * @see net.bytebuddy.matcher.ElementMatcher#matches(java.lang.Object)
     */
    @Override
    public boolean matches(T target) {
        StringBuilder stringBuilder = new StringBuilder()
            .append(target.getDeclaringType().getTypeName())
            .append(".")
            .append(target.getName())
            .append("(");
        for (int i = 0; i < target.getParameters().size(); i++) {
            ParameterDescription.AbstractBase parameter = (ParameterDescription.AbstractBase) target.getParameters().get(i);
            stringBuilder.append(parameter.getType().getTypeName());
            if (target.getParameters().size() - 1 > i) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        
        boolean matches = NameFilterService.getInstance().matches(stringBuilder.toString());
        
        if (logger.isDebugEnabled() && matches) {
            logger.debug("The method will be intercepted and instrumentalized '{}'", stringBuilder.toString());
        }
        
        return matches;
    }

}
