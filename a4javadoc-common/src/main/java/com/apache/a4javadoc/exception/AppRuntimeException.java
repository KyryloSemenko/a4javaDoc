package com.apache.a4javadoc.exception;

/**
 * Application runtime exception. Extends the {@link RuntimeException}.
 * @author Kyrylo Semenko
 */
@SuppressWarnings("serial")
public class AppRuntimeException extends RuntimeException {
    
    /** Call a super {@link RuntimeException#RuntimeException()} constructor */
    public AppRuntimeException() {
        super();
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String)} constructor
     * @param message see {@link RuntimeException#RuntimeException(String)}
     */
    public AppRuntimeException(String message) {
         super(message);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(Throwable)} constructor
     * @param cause see {@link RuntimeException#RuntimeException(Throwable)}
     */
    public AppRuntimeException(Throwable cause) {
         super(cause);
    }

    /**
     * Call a super {@link RuntimeException#RuntimeException(String, Throwable)} constructor
     * @param message see {@link RuntimeException#RuntimeException(String, Throwable)}
     * @param cause see {@link RuntimeException#RuntimeException(String, Throwable)}
     */
    public AppRuntimeException(String message, Throwable cause) {
         super(message, cause);
    }
    
}
