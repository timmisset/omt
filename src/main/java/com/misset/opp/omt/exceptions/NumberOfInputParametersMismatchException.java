package com.misset.opp.omt.exceptions;

public class NumberOfInputParametersMismatchException extends Exception {

    private final String name;
    private final int minExpected;
    private final int maxExpected;
    private final int found;

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
