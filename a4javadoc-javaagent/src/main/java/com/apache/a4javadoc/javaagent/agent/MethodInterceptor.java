package com.apache.a4javadoc.javaagent.agent;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.javaagent.api.StateAfterInvocation;
import com.apache.a4javadoc.javaagent.api.StateBeforeInvocation;
import com.apache.a4javadoc.javaagent.api.MethodStateRecorder;
import com.apache.a4javadoc.plugin.AgentPluginManager;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.asm.Advice.Thrown;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;

/**
 * The class methods are invoked by the {@link Agent}. The class contains a state object {@link #methodInvocationCounter}.<br>
 * The class contains the {@link #enter(String, String, String, String, String, String, Object[])} and {@link #exit(long, String, Object, Throwable, Object[])}
 * methods, where an intercepted methods will be instrumented.
 * @author Kyrylo Semenko
 */
public class MethodInterceptor {
    
    /** Public because it used in a code generation */
    public static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class); // NOSONAR
    
    /** Each method invocation has its own identifier. It starts from 1 when an instrumented application launched. It is public because it used in a code generation. */
    public static AtomicLong methodInvocationCounter = new AtomicLong(0L); // NOSONAR
    
    /**
     * The constructor should newer be invoked. It throws an {@link IllegalStateException}.
     */
    private MethodInterceptor() {
        throw new IllegalStateException("The class " + MethodInterceptor.class.getName() + " should not be instantiated");
    }
    
    /**
     * The code from this method will be placed before an intercepted method invocation.
     * @param methodName an intercepted method's name, see {@link net.bytebuddy.asm.Advice.Origin#value()} #m
     * @param declaringType an intercepted method's declaring type, see {@link net.bytebuddy.asm.Advice.Origin#value()} #t
     * @param methodDescriptor an intercepted method's descriptor, see {@link net.bytebuddy.asm.Advice.Origin#value()} #d
     * @param methodSignature an intercepted method's signature, see {@link net.bytebuddy.asm.Advice.Origin#value()} #s
     * @param returnType an intercepted method's return type, see {@link net.bytebuddy.asm.Advice.Origin#value()} #r
     * @param methodComplexName an intercepted method's full name, see {@link net.bytebuddy.asm.Advice}
     * @param allArguments intercepted method parameters
     * @return the identifier of the intercepted method, see {@link #methodInvocationCounter}
     */
    @Advice.OnMethodEnter
    public static long enter(
            @Advice.Origin("#m") String methodName,
            @Advice.Origin("#t") String declaringType,
            @Advice.Origin("#d") String methodDescriptor,
            @Advice.Origin("#s") String methodSignature,
            @Advice.Origin("#r") String returnType,
            @Advice.Origin String methodComplexName,
            @Advice.AllArguments Object[] allArguments) {
        
        long methodInvocationId = methodInvocationCounter.incrementAndGet();
        
        if (logger.isTraceEnabled()) {
            logger.trace("Debuging info of 'enter': "
                    + "methodInvocationCounter: " + methodInvocationId
                    + ", methodName: " + methodName
                    + ", declaringType: " + declaringType
                    + ", methodDescriptor: " + methodDescriptor
                    + ", methodSignature: " + methodSignature
                    + ", returnType: " + returnType
                    + ", methodComplexName: " + methodComplexName
                    + ", allArguments: " + toString(allArguments));
        }
        
        List<MethodStateRecorder> methodStateRecorders = AgentPluginManager.getInstance().getMethodStateRecorders();
        
        StateBeforeInvocation stateBeforeInvocation = new StateBeforeInvocation(
                methodInvocationId,
                new Date(),
                Thread.currentThread().getStackTrace(),
                methodName,
                declaringType,
                methodDescriptor,
                methodSignature,
                returnType,
                methodComplexName,
                allArguments);
        
        for (MethodStateRecorder methodStateRecorder : methodStateRecorders) {
            methodStateRecorder.recordBefore(stateBeforeInvocation);
        }        
        return methodInvocationId;
    }
    
    /**
     * The code from this method will be placed after an intercepted method invocation.
     * @param methodInvocationId a value for pairing {@link #enter(String, String, String, String, String, String, Object[])} and {@link #exit(long, String, Object, Throwable, Object[])} records. Value of {@link #methodInvocationCounter}.
     * @param methodComplexName an intercepted metod's full name, see {@link net.bytebuddy.asm.Advice.Origin}
     * @param returnValue the value returned by the intercepted method, see {@link Return}
     * @param throwable an {@link Throwable} thrown by an intercepted method, see {@link Thrown}
     * @param allArguments intercepted method parameters
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(
            @Advice.Enter final long methodInvocationId,
            @Advice.Origin String methodComplexName,
            @Advice.Return(typing = Typing.DYNAMIC) Object returnValue,
            @Advice.Thrown Throwable throwable,
            @Advice.AllArguments Object[] allArguments) {
      
        if (logger.isTraceEnabled()) {
            logger.trace("Debuging info of 'enter': "
                + " methodInvocationId: " + methodInvocationId
                + ", methodComplexName: " + methodComplexName
                + ", returnValue: " + returnValue
                + ", throwable: " + throwable
                + ", allArguments: " + toString(allArguments)
              );
        }
        List<MethodStateRecorder> methodStateRecorders = AgentPluginManager.getInstance().getMethodStateRecorders();
        
        StateAfterInvocation stateAfterInvocation = new StateAfterInvocation(
                methodInvocationId,
                new Date(),
                methodComplexName,
                returnValue,
                throwable,
                allArguments
                );
        
        for (MethodStateRecorder methodStateRecorder : methodStateRecorders) {
            methodStateRecorder.recordAfter(stateAfterInvocation);
        }
    }
    
    /**
     * Create a String for debugging purposes
     * @param allArguments an array of intercepted method parameters
     * @return a plain text
     */
    public static String toString(Object[] allArguments) {
        StringBuilder result = new StringBuilder("{");
        for (int i = 0; i < allArguments.length; i++) {
            result.append(allArguments[i]);
            if (i+1 < allArguments.length) {
                result.append(", ");
            }
        }
        result.append("}");
        return result.toString();
    }
}
