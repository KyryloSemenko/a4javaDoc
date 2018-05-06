package com.apache.a4javadoc.javaagent.agent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;

/**
 * The class does the sane as the {@link MethodInterceptor}, with a difference in the {@link #enterConstructor(String, String, String, String, String, String, Object[])} method.<br>
 * This approach has been chosen because ByteBuddy cannot catch an expectedException in constructors,
 * see <a href="https://stackoverflow.com/questions/47248429/how-to-take-the-expectedException-thrown-by-a-constructor-using-a-bytebuddy-agent">this</a>
 * and <a href="https://github.com/raphw/byte-buddy/issues/375">this</a> links
 * @author Kyrylo Semenko
 */
public class ConstructorInterceptor {
    
    /**
     * The constructor should newer be invoked. It throws an {@link IllegalStateException}.
     */
    private ConstructorInterceptor() {
        throw new IllegalStateException("The class " + ConstructorInterceptor.class.getName() + " should not be instantiated");
    }
    
    /**
     * Call the {@link MethodInterceptor#enter(String, String, String, String, String, String, Object[])} method.
     * @param methodName an intercepted method's name, see {@link net.bytebuddy.asm.Advice.Origin#value()} #m
     * @param declaringType an intercepted method's declaring type, see {@link net.bytebuddy.asm.Advice.Origin#value()} #t
     * @param methodDescriptor an intercepted method's descriptor, see {@link net.bytebuddy.asm.Advice.Origin#value()} #d
     * @param methodSignature an intercepted method's signature, see {@link net.bytebuddy.asm.Advice.Origin#value()} #s
     * @param returnType an intercepted method's return type, see {@link net.bytebuddy.asm.Advice.Origin#value()} #r
     * @param methodComplexName an intercepted method's full name, see {@link net.bytebuddy.asm.Advice}
     * @param allArguments intercepted method parameters
     * @return the identifier of the intercepted method, see {@link MethodInterceptor#methodInvocationCounter}
     */
    @Advice.OnMethodEnter
    public static long enterConstructor(
            @Advice.Origin("#m") String methodName,
            @Advice.Origin("#t") String declaringType,
            @Advice.Origin("#d") String methodDescriptor,
            @Advice.Origin("#s") String methodSignature,
            @Advice.Origin("#r") String returnType,
            @Advice.Origin String methodComplexName,
            @Advice.AllArguments Object[] allArguments) {
        
        return MethodInterceptor.enter(methodName, declaringType, methodDescriptor, methodSignature, returnType, methodComplexName, allArguments);
    }

    /**
     * Call the {@link MethodInterceptor#exit(long, String, Object, Throwable, Object[])} method with <b>null</b> fourth parameter.
     * @param methodInvocationId a value for pairing {@link #enterConstructor(String, String, String, String, String, String, Object[])} and {@link #exitConstructor(long, String, Object, Object[])} records. See {@link MethodInterceptor#methodInvocationCounter}.
     * @param methodComplexName an intercepted metod's full name, see {@link net.bytebuddy.asm.Advice.Origin}
     * @param returnValue the value returned by the intercepted method, see {@link Return}
     * @param allArguments intercepted method parameters
     */
    @Advice.OnMethodExit
    public static void exitConstructor(@Advice.Enter final long methodInvocationId,
            @Advice.Origin String methodComplexName,
            @Advice.Return(typing = Typing.DYNAMIC) Object returnValue,
            @Advice.AllArguments Object[] allArguments) {
        MethodInterceptor.exit(methodInvocationId, methodComplexName, returnValue, null, allArguments);
    }
    
}
