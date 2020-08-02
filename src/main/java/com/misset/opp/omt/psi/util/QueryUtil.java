package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.List;

public class QueryUtil {

    public static boolean isPartOfQueryStep(PsiElement element) {
        return PsiTreeUtil.getTopmostParentOfType(element, OMTQueryStep.class) != null;
    }

    public static OMTOperator standAloneQueryToOperator(OMTModelItemBlock modelItemBlock) {
        String name = modelItemBlock.getModelItemLabel().getPropertyName().getText();
        name = name.endsWith(":") ? name.substring(0, name.length() - 1) : name;

        List<OMTParameter> parameters = ModelUtil.getModelItemParameters(modelItemBlock);

        return new OMTOperator(name, parameters, "Standalone Query");
    }
}
