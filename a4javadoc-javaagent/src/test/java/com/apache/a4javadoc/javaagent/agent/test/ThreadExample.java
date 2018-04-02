package com.apache.a4javadoc.javaagent.agent.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.apache.a4javadoc.javaagent.exception.AppRuntimeException;

/** 
 * @author Kyrylo Semenko
 */
public class ThreadExample implements Runnable {
    private int counter = 0;

    /** 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            InputStream inputStream = new FileInputStream("c:/temp/download/116/odzak/traces_ipayment/eps_truncated.log");
            printMessage("Message", inputStream);
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException(e);
        }
    }

    private int printMessage(String string, InputStream inputStream) {
        System.out.println(string);
        return counter++;
    }

}
