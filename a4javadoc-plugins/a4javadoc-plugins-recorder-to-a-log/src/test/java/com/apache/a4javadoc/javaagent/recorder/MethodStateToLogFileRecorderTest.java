package com.apache.a4javadoc.javaagent.recorder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.apache.a4javadoc.javaagent.mapper.WrapperClass;

/** 
 * @author Kyrylo Semenko
 */
public class MethodStateToLogFileRecorderTest {

    /**
     * Test method for {@link com.apache.a4javadoc.javaagent.recorder.MethodStateToLogFileRecorder#removeCircularObjects(java.lang.Object[])}.
     */
    @Test
    public void testRemoveCircularObjects() {
        WrapperClass child = new WrapperClass();
        child.setId(2);
        child.setParent(child);
        
        Object[] allArguments = new Object[2];
        allArguments[0] = child;
        allArguments[1] = "string";
        
        MethodStateToLogFileRecorder methodStateToLogFileRecorder = new MethodStateToLogFileRecorder();
        methodStateToLogFileRecorder.removeCircularObjects(allArguments);
        
        assertEquals(MethodStateToLogFileRecorder.REMOVED_BECAUSE_THE_OBJECT_CONTAINED_A_CIRCULAR_DEPENDENCY, allArguments[0]);
        assertEquals("string", allArguments[1]);
    }

}
