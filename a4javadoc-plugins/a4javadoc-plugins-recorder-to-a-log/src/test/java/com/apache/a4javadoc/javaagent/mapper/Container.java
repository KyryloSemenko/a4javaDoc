package com.apache.a4javadoc.javaagent.mapper;

import java.util.List;

/**
 * For tests purposes only.
 * @author Kyrylo Semenko
 */
public class Container {
    private List<String> listOfStrings;
    private String string;

    /** @return The {@link Container#listOfStrings} field */
    public List<String> getListOfStrings() {
        return listOfStrings;
    }

    /** @param listOfStrings see the {@link Container#listOfStrings} field */
    public void setListOfStrings(List<String> listOfStrings) {
        this.listOfStrings = listOfStrings;
    }

    /** @return The {@link Container#string} field */
    public String getString() {
        return string;
    }

    /** @param string see the {@link Container#string} field */
    public void setString(String string) {
        this.string = string;
    }

}
