package com.misset.opp.omt.psi.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTScriptLine;

import java.util.Optional;

public class ScriptUtil {

    public static Optional<OMTScriptLine> getScriptLine(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getTopmostParentOfType(element, OMTScriptLine.class));
    }
    public static Optional<OMTScript> getScript(PsiElement element) {
        return Optional.ofNullable(PsiTreeUtil.getTopmostParentOfType(element, OMTScript.class));
    }

    public static boolean isBefore(PsiElement isElement, PsiElement beforeElement) {
        Optional<OMTScriptLine> isElementScriptline = getScriptLine(isElement);
        Optional<OMTScriptLine> beforeElementScriptline = getScriptLine(beforeElement);
        if(!isElementScriptline.isPresent() || !beforeElementScriptline.isPresent()) {
            return false;
        }
        return isElementScriptline.get().getParent().getStartOffsetInParent() <
                beforeElementScriptline.get().getParent().getStartOffsetInParent();
    }
}
