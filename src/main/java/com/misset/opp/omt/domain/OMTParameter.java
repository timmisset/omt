//package com.misset.opp.omt.domain;
//
//import com.google.gson.JsonPrimitive;
//import com.misset.opp.omt.psi.OMTListItemParameter;
//import com.misset.opp.omt.psi.OMTVariable;
//import com.misset.opp.omt.psi.OMTVariableAssignment;
//import com.misset.opp.omt.psi.OMTVariableValue;
//
//public class OMTParameter {
//
//    private String name;
//    private boolean required;
//    private OMTVariableValue defaultValue;
//    private boolean rest;
//
//    // TODO:
//    // private TypeOfParameter
//
//    // as parsed from builtIn tree
//    public OMTParameter(JsonPrimitive primitive) {
//        if(primitive.getAsString().startsWith("p.")) {
//            name = primitive.getAsString();
//            String typeOfParameter = name.substring(2);
//            required = !typeOfParameter.startsWith("optional");
//            rest = typeOfParameter.startsWith("rest");
//        }
//    }
//
//    public OMTParameter(OMTVariable variable) {
//        name = variable.getText();
//        required = true;
//        defaultValue = null;
//    }
//    public OMTParameter(com.misset.opp.omt.psi.OMTParameter parameter) {
//        name = parameter.getText();
//        required = true;
//        defaultValue = null;
//    }
//    public OMTParameter(OMTListItemParameter parameter) {
//        name = parameter.getText();
//        required = true;
//        defaultValue = null;
//    }
//
//    public OMTParameter(OMTVariableAssignment variableAssignment) {
//        name = variableAssignment.getVariable().getText();
//        required = false;
//        defaultValue = variableAssignment.getVariableValue();
//    }
//
//    public boolean isRequired() { return required; }
//    public boolean isRest() { return rest; }
//    public String getName() { return name; }
//
//}
