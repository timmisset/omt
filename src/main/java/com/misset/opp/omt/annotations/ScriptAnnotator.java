package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;

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
        if ((isPartOfCommandBlock(scriptContent) || getModelUtil().isScalarEntry(scriptContent)) &&
                (psiElement == null || psiElement.getNode().getElementType() != OMTTypes.SEMICOLON)) {
            setError("; expected");
        } else if (getModelUtil().isQueryEntry(scriptContent)
                && psiElement != null && psiElement.getNode().getElementType() == OMTTypes.SEMICOLON) {
            setError("Query entry should not end with semicolon");
        }
    }

    private boolean isPartOfCommandBlock(OMTScriptContent scriptContent) {
        return PsiTreeUtil.findFirstParent(scriptContent, parent -> parent instanceof OMTCommandBlock) != null;
    }

    private void annotate(OMTIfBlock omtIfBlock) {
        annotateBoolean(omtIfBlock.getQuery().resolveToResource(), null);
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
