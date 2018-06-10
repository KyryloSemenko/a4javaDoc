package com.apache.a4javadoc.javaagent.agent.test;

import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apache.a4javadoc.exception.AppRuntimeException;

/** 
 * @author Kyrylo Semenko
 */
public class ShellTest {
    private static final Logger logger = LoggerFactory.getLogger(ShellTest.class);
    
    private final Display display;
    private final Shell shell;
    
    public ShellTest() {
        display = new Display();
        shell = new Shell(display);
    }

    public static void main(String[] args) {
        ShellTest instance = new ShellTest();
        
        instance.startApp();
    }

    private void startApp() {
        Text helloWorldTest = new Text(shell, SWT.NONE);
        helloWorldTest.setText("Hello World SWT");
        helloWorldTest.pack();
        
        shell.pack();
        shell.open ();
        prepareShellListeners(shell);
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
    
    public void prepareShellListeners(Shell shell){
        shell.addShellListener(new ShellListener() {

            @Override
            public void shellIconified(ShellEvent arg0) {
                logger.debug("Window was Iconified");
            }

            @Override
            public void shellDeiconified(ShellEvent arg0) {
                logger.debug("Window was Deiconified");
            }

            @Override
            public void shellDeactivated(ShellEvent arg0) {
                logger.debug("Window was Deactivated");
            }

            @Override
            public void shellClosed(ShellEvent arg0) {
                logger.debug("Window was Closed");
            }

            @Override
            public void shellActivated(ShellEvent arg0) {
                logger.debug("Window was Activated");

                logger.debug("Client is going to be resumed");
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("Try to resume");
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            throw new AppRuntimeException(e);
                        }
                    }
                });
            }
        });
    }


}
