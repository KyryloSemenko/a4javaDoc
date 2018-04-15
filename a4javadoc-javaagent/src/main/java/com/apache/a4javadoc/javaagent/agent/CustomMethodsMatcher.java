package com.apache.a4javadoc.javaagent.agent;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.matcher.ElementMatcher;

/** 
 * @author Kyrylo Semenko
 */
public class CustomMethodsMatcher<T extends MethodDescription> extends ElementMatcher.Junction.AbstractBase<T> {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassesMatcher.class);
    
    /** En empty constructor with a log message */
    public CustomMethodsMatcher() {
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
        
        boolean isMatched = startsWith(stringBuilder.toString(), SystemParametersService.getInstance().getIncludeNames())
                && !startsWith(stringBuilder.toString(), SystemParametersService.getInstance().getExcludePackages());
        
        if (logger.isDebugEnabled() && isMatched) {
            logger.debug("The method will be intercepted and instrumented '{}'", stringBuilder.toString());
        }
        
        return isMatched;
    }

    /** @return true if the first parameter value starts with one of Strings from the set from the second parameter. */
    private boolean startsWith(String javaClassName, Set<String> set) {
        for (String next : set) {
            if (javaClassName.startsWith(next)) {
                return true;
            }
        }
        return false;
    }

}
