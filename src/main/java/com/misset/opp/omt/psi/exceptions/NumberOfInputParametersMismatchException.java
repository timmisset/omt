package com.misset.opp.omt.psi.exceptions;

public class NumberOfInputParametersMismatchException extends Exception {

    private String name;
    private int minExpected;
    private int maxExpected;
    private int found;

    public NumberOfInputParametersMismatchException(String name, Integer minExpected, Integer maxExpected, Integer found) {
        super();
        this.name = name;
        this.minExpected = minExpected;
        this.maxExpected = maxExpected;
        this.found = found;
    }
    @Override
    public String getMessage() {
        return String.format("%s expects %s %s, found %s", name, getExpectedParams(), singleAmount() && minExpected == 1 ? "parameter" : "parameters", found);
    }
    private boolean singleAmount() { return minExpected == maxExpected; }
    private String getExpectedParams() {
        if(singleAmount()) { return minExpected + ""; }
        if(maxExpected == -1) { return String.format("%s or more", minExpected); }
        return String.format("between %s and %s", minExpected, maxExpected);
    }
}
