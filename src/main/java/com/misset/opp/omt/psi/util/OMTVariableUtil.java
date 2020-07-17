//package com.misset.opp.omt.psi.util;
//
//import com.misset.opp.omt.psi.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//public class OMTVariableUtil {
//
//    public static boolean isDeclared(OMTVariableName variable) {
//        return  getDeclaredByModelItem(variable).isPresent() ||
//                getDeclaredByInputArguments(variable).isPresent() ||
//                getDeclaredByScriptBlock(variable).isPresent();
//    }
//    private static Optional<OMTVariableName> getDeclaredByModelItem(OMTVariableName variable) {
//        OMTVariableName variableName = getDeclaredByModelItemPart(variable, "variables").orElse(getDeclaredByModelItemPart(variable,"params").orElse(null));
//        return variableName == null ? Optional.empty() : Optional.of(variableName);
//    }
//    private static Optional<OMTVariableName> getDeclaredByModelItemPart(OMTVariableName variable, String part) {
//        OMTModelBlockGroup variables = OMTUtil.getModelItemBlockGroup(OMTUtil.getModelItem(variable), part);
//        if(variables == null || variables.getModelBlockContent() == null || variables.getModelBlockContent().getListItemList() == null) { return Optional.empty(); }
//        List<OMTListItem> listItemList = variables.getModelBlockContent().getListItemList();
//        return listItemList.stream()
//                .filter(omtListItem -> omtListItem.getVariable().getText().equals(variable.getText()))
//                .map(OMTListItem::getVariable)
//                .map(OMTVariable::getVariableName)
//                .findFirst();
//    }
//    private static Optional<OMTVariableName> getDeclaredByInputArguments(OMTVariableName variable) {
//        OMTInputArguments inputArguments = OMTUtil.getInputArguments(variable);
//        if(inputArguments == null) { return Optional.empty(); }
//        return inputArguments.getInputArgumentList().stream()
//                .filter(omtInputArgument -> omtInputArgument.getVariable() != null)
//                .map(OMTInputArgument::getVariable)
//                .filter(inputVariable -> inputVariable.getText().equals(variable.getText()))
//                .map(OMTVariable::getVariableName)
//                .findFirst();
//    }
//    private static Optional<OMTVariableName> getDeclaredByScriptBlock(OMTVariableName variable) {
//        // check if the input is itself a VAR $variable = ... statement
//        OMTVariableDeclare declaredVariable = OMTUtil.getUpstreamElement(variable, OMTVariableDeclare.class);
//        if(declaredVariable != null) { return Optional.of(declaredVariable.getVariableName()); }
//
//        List<OMTVariableName> declaredByScriptBlockVariables = getDeclaredByScriptBlockVariables(variable);
//        return declaredByScriptBlockVariables.stream()
//                .filter(omtVariableName -> omtVariableName.getText().equals(variable.getText()))
//                .findFirst();
//    }
//    private static List<OMTVariableName> getDeclaredByScriptBlockVariables(OMTVariableName variable) {
//        // check if the variable is even part of a script block
//        OMTScriptBlock scriptBlock = OMTUtil.getScriptBlock(variable);
//        if(scriptBlock == null) { return new ArrayList<>(); }
//
//        // and get the scriptline, this is required to make sure that the declare happens before the usage
//        OMTScriptLine scriptLineOfVariable = OMTUtil.getUpstreamElement(variable, OMTScriptLine.class);
//        if(scriptLineOfVariable == null) { return new ArrayList<>(); }
//
//        return scriptBlock.getScriptLineList().stream()
//                .filter(omtScriptLine -> omtScriptLine.getStartOffsetInParent() <= scriptLineOfVariable.getStartOffsetInParent())
//                .filter(omtScriptLine -> omtScriptLine.getVariableDeclare() != null)
//                .map(OMTScriptLine::getVariableDeclare)
//                .map(OMTVariableDeclare::getVariableName)
//                .collect(Collectors.toList());
//    }
//}
