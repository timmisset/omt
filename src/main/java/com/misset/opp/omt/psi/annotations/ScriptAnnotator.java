package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

public class ScriptAnnotator extends AbstractAnnotator {

    public ScriptAnnotator(AnnotationHolder holder) {
        super(holder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTIfBlock) {
            annotate((OMTIfBlock) element);
        } else if (element instanceof OMTScriptContent) {
            annotate((OMTScriptContent) element);
        } else if (element instanceof OMTScriptLine) {
            annotate((OMTScriptLine) element);
        }
    }

    private void annotate(OMTScriptContent scriptContent) {
        final PsiElement psiElement = PsiTreeUtil.nextVisibleLeaf(scriptContent);
        if (psiElement == null || psiElement.getNode().getElementType() != OMTTypes.SEMICOLON) {
            setError("; expected");
        }
    }

    private void annotate(OMTIfBlock omtIfBlock) {
        annotateBoolean(omtIfBlock.getQuery().resolveToResource());
    }

    private void annotate(OMTScriptLine scriptLine) {
        OMTScriptLine previousScriptline = PsiTreeUtil.getPrevSiblingOfType(scriptLine, OMTScriptLine.class);
        while (previousScriptline != null) {
            if (PsiTreeUtil.findChildOfType(previousScriptline, OMTReturnStatement.class, true, OMTCommandBlock.class) != null) {
                setError("Unreachable code");
                return;
            }
            previousScriptline = PsiTreeUtil.getPrevSiblingOfType(previousScriptline, OMTScriptLine.class);
        }
    }

}
