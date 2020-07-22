package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelUtil {

    public static Optional<OMTBlock> getModelItemBlock(PsiElement element) {
        OMTPropertyLabel propertyLabel = PsiTreeUtil.getChildOfType(element, OMTPropertyLabel.class);
        if(propertyLabel != null) {
            if(propertyLabel.getLastChild().getText().startsWith("!")) { return Optional.of((OMTBlock)element); } // found the modelitem block
        }
        PsiElement parent = element.getParent();
        return parent != null ? getModelItemBlock(parent) : Optional.empty();
    }


    public static List<OMTDefineQueryStatement> getAllDefinedQueries(PsiElement containingElement) {
        // from the document in the queries root:
        List<OMTDefineQueryStatement> definedQueries = new ArrayList<>();
        OMTQueriesBlock queriesBlock = PsiTreeUtil.findChildOfType(containingElement, OMTQueriesBlock.class);
        if(queriesBlock != null) {
            definedQueries.addAll(queriesBlock.getDefineQueryStatementList());
        }
        return definedQueries;
    }

}
