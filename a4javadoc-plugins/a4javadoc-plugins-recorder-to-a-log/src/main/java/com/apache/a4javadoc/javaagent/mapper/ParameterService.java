package com.apache.a4javadoc.javaagent.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Stateless singleton for working with constructor or method parameters.
 * @author Kyrylo Semenko
 */
public class ParameterService {
    
    private static ParameterService instance;
    
    private ParameterService() {
        // empty
    }
    
    /**
     * @return the {@link ParameterService} singleton.
     */
    public static ParameterService getInstance() {
        if (instance == null) {
            instance = new ParameterService();
        }
        return instance;
    }

    /**
     * Decide if parameterTypes from the first argument suit to parameters from the second argument.
     * @param parameterTypes obtained from a {@link Constructor} or {@link Method}
     * @param parameters instances of some objects
     * @param isVarArgs is the method of the parameters {@link Method#isVarArgs()}? 
     * @return 'true' if the parameters can be used for invocation of a {@link Constructor} or {@link Method} 
     */
    public boolean isParametersSuit(Class<?>[] parameterTypes, List<Object> parameters, boolean isVarArgs) {
        int length = parameterTypes.length;
        if (isVarArgs) {
            int indexOfVarargs = parameterTypes.length - 1;
            for (int i = 0; i < indexOfVarargs; i++) {
                if (!parameterTypes[i].isAssignableFrom(parameters.get(i).getClass())) {
                    return false;
                }
            }
            List<Object> varArgsParameters = parameters.subList(indexOfVarargs, parameters.size());
            Class<?> varArgParametersClass = ClassService.getInstance().findCommonClass(varArgsParameters);
            return (parameterTypes[indexOfVarargs].getComponentType().isAssignableFrom(varArgParametersClass));
        } else {
            if (length != parameters.size()) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!parameterTypes[i].isAssignableFrom(parameters.get(i).getClass())) {
                    return false;
                }
            }
        }
        return true;
    }
}
