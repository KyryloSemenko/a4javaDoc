package com.apache.a4javadoc.javaagent.mapper;

/**
 * For test purposes.
 * @author Kyrylo Semenko
 */
public class VarArgsClass {
    private String string;
    
    private int[] integers;
    
    public void callMethod(String string, int ... i) {
        this.string = string;
        this.integers = i;
    }

    /** @return The {@link VarArgsClass#string} field */
    public String getString() {
        return string;
    }

    /** @param string see the {@link VarArgsClass#string} field */
    public void setString(String string) {
        this.string = string;
    }

    /** @return The {@link VarArgsClass#integers} field */
    public int[] getIntegers() {
        return integers;
    }

    /** @param integers see the {@link VarArgsClass#integers} field */
    public void setIntegers(int[] integers) {
        this.integers = integers;
    }
}
