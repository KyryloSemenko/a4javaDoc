package com.apache.a4javadoc.javaagent.agent.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;

/** 
 * @author Kyrylo Semenko
 */
public class ThreadExample implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ThreadExample.class);
    private int counter = 0;

    /** 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            InputStream inputStream = new FileInputStream("c:/temp/download/116/odzak/traces_ipayment/eps_truncated.log");
            printMessage("Message", inputStream);
            Long count = getNumber("123456");
            logger.info("Count of 123456: {}", count);
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    private int printMessage(String string, InputStream inputStream) {
        logger.info(string);
        return counter++;
    }
    
    private static Long getNumber(String parameter) {
        return (long) parameter.length();
    }

}
