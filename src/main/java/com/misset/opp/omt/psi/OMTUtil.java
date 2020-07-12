package com.misset.opp.omt.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.*;
import java.util.stream.Collectors;

public class OMTUtil {

    public static boolean isDeclared(OMTVariable variable) {
        return isDeclaredByModelItem(variable) || isDeclaredByInputArguments(variable);
    }
    private static boolean isDeclaredByModelItem(OMTVariable variable) {
        return isDeclaredByModelItemPart(variable, "variables") || isDeclaredByModelItemPart(variable,"params");
    }
    private static boolean isDeclaredByModelItemPart(OMTVariable variable, String part) {
        OMTModelBlockGroup variables = getModelItemBlockGroup(getModelItem(variable), part);
        if(variables == null || variables.getModelBlockContent() == null || variables.getModelBlockContent().getListItemList() == null) { return false; }
        List<OMTListItem> listItemList = variables.getModelBlockContent().getListItemList();
        Optional<OMTListItem> variableMatch = listItemList.stream().filter(omtListItem -> omtListItem.getVariable().getText().equals(variable.getText())).findFirst();
        return variableMatch.isPresent();
    }
    private static boolean isDeclaredByInputArguments(OMTVariable variable) {
        OMTInputArguments inputArguments = getInputArguments(variable);
        if(inputArguments == null) { return false; }
        Optional<OMTVariable> variableMatch = inputArguments.getInputArgumentList().stream()
                .filter(omtInputArgument -> omtInputArgument.getVariable() != null)
                .map(OMTInputArgument::getVariable)
                .filter(inputVariable -> inputVariable.getText().equals(variable.getText()))
                .findFirst();
        return variableMatch.isPresent();
    }
    private static OMTModelBlockGroup getModelItemBlockGroup(OMTModelItem modelItem, String id) {
        if(modelItem == null) { return null; }
        List<OMTModelBlockGroup> modelBlockGroupList = modelItem.getModelBlockGroupList();
        Optional<OMTModelBlockGroup> modelBlockGroup = modelBlockGroupList.stream()
                .filter(omtModelBlockGroup -> omtModelBlockGroup.getModelBlockId().getFirstChild().getText().equals(id)).findFirst();
        return modelBlockGroup.orElse(null);
    }

    private static OMTModelItem getModelItem(PsiElement element) {
        return getUpstreamElement(element, OMTModelItem.class);
    }
    private static OMTInputArguments getInputArguments(PsiElement element) {
        // retrieve from direct parent upstream
        OMTInputArguments upstreamElement = getUpstreamElement(element, OMTInputArguments.class);
        if(upstreamElement == null) {
            // retrieve from upstream query -> input arguments
            OMTQuery omtQuery = getUpstreamElement(element, OMTQuery.class);
            upstreamElement = omtQuery == null ? null : omtQuery.getInputArguments(); }

        return upstreamElement;
    }

    private static <T> T getUpstreamElement(PsiElement element, Class<T> upstreamClass) {
        if(upstreamClass.isAssignableFrom(element.getClass())) { return (T)element; }
        while(element.getParent() != null) {
            element = element.getParent();
            if(upstreamClass.isAssignableFrom(element.getClass())) { return (T)element; }
        }
        return null;
    }
}
