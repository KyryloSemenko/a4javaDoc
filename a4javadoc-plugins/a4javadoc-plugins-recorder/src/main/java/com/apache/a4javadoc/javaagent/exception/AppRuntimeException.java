package com.apache.a4javadoc.javaagent.exception;

/**
 * Application runtime exception. Extends {@link RuntimeException}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class AppRuntimeException extends RuntimeException {
    
    /** Call a super {@link RuntimeException#RuntimeException()} constructor */
    public AppRuntimeException() {
        super();
    }

    /** Call a super {@link RuntimeException#RuntimeException(String)} constructor */
    public AppRuntimeException(String message) {
         super(message);
    }

    /** Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor */
    public AppRuntimeException(Throwable e) {
         super(e);
    }

    /** Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor */
    public AppRuntimeException(String message, Throwable cause) {
         super(message, cause);
    }
    
}
