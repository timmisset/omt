package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OMTUtil {

    static OMTModelBlockGroup getModelItemBlockGroup(OMTModelItem modelItem, String id) {
        if(modelItem == null) { return null; }
        List<OMTModelBlockGroup> modelBlockGroupList = modelItem.getModelBlockGroupList();
        Optional<OMTModelBlockGroup> modelBlockGroup = modelBlockGroupList.stream()
                .filter(omtModelBlockGroup -> omtModelBlockGroup.getModelBlockId().getFirstChild().getText().equals(id)).findFirst();
        return modelBlockGroup.orElse(null);
    }

    static OMTModelItem getModelItem(PsiElement element) {
        return getUpstreamElement(element, OMTModelItem.class);
    }
    static OMTModelBlockGroup getMainGroup(PsiElement element, String blockName) {
        OMTFile file = getUpstreamElement(element, OMTFile.class);
        if(file == null) { return null; }
        for(PsiElement mainElement : file.getChildren()) {
            if(mainElement instanceof OMTModelBlockGroup) {
                OMTModelBlockGroup modelBlockGroup = (OMTModelBlockGroup)mainElement;
                if(modelBlockGroup.getModelBlockId().getFirstChild().getText().equals("prefixes")) {
                    return modelBlockGroup;
                }
            }
        }
        return null;
    }
    static List<OMTPrefix> getPrefixes(PsiElement element) {
        OMTModelBlockGroup prefixes = getMainGroup(element, "prefixes");
        if(prefixes == null) { return new ArrayList<>(); }
        return prefixes.getModelBlockContent() == null ? new ArrayList<>() : prefixes.getModelBlockContent().getPrefixList();
    }
    static List<OMTQuery> getRootQueries(PsiElement element) {
        OMTModelBlockGroup queries = getMainGroup(element, "queries");
        if(queries == null) { return new ArrayList<>(); }
        return queries.getModelBlockContent() == null ? new ArrayList<>() : queries.getModelBlockContent().getQueryList();
    }
    static OMTInputArguments getInputArguments(PsiElement element) {
        // retrieve from direct parent upstream
        OMTInputArguments upstreamElement = getUpstreamElement(element, OMTInputArguments.class);
        if(upstreamElement == null) {
            // retrieve from upstream query -> input arguments
            OMTQuery omtQuery = getUpstreamElement(element, OMTQuery.class);
            upstreamElement = omtQuery == null ? null : omtQuery.getInputArguments(); }

        return upstreamElement;
    }
    static OMTScriptBlock getScriptBlock(PsiElement element) {
        return getUpstreamElement(element, OMTScriptBlock.class);
    }

    static <T> T getUpstreamElement(PsiElement element, Class<T> upstreamClass) {
        if(upstreamClass.isAssignableFrom(element.getClass())) { return (T)element; }
        while(element.getParent() != null) {
            element = element.getParent();
            if(upstreamClass.isAssignableFrom(element.getClass())) { return (T)element; }
        }
        return null;
    }
}
