package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTQueryStep;

public class QueryUtil {

    public static boolean isPartOfQueryStep(PsiElement element) {
        return PsiTreeUtil.getTopmostParentOfType(element, OMTQueryStep.class) != null;
    }

}
