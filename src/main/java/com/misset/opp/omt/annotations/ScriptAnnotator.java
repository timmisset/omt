package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTCommandBlock;
import com.misset.opp.omt.psi.OMTIfBlock;
import com.misset.opp.omt.psi.OMTReturnStatement;
import com.misset.opp.omt.psi.OMTScript;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTScriptLine;
import com.misset.opp.omt.psi.OMTTypes;

import static com.misset.opp.util.UtilManager.getModelUtil;

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
        final boolean queryEntry = getModelUtil().isQueryEntry(scriptContent);
        if (queryEntry
                && psiElement != null && psiElement.getNode().getElementType() == OMTTypes.SEMICOLON) {
            setError("Query entry should not end with semicolon");
        } else if (!queryEntry && isPartOfMultilineScript(scriptContent) &&
                (psiElement == null || psiElement.getNode().getElementType() != OMTTypes.SEMICOLON)) {
            setError("; expected");
        }
    }

    /*
        A multiline script is anything that contains more than 1 scriptline or is nested in a command block { } to
        indicate that any code lines in such a script should be delimited by a ;
     */
    private boolean isPartOfMultilineScript(PsiElement psiElement) {
        final OMTScript omtScript = PsiTreeUtil.getParentOfType(psiElement, OMTScript.class);
        return (omtScript != null && omtScript.getScriptLineList().size() > 1) ||           // either multiple scriptlines
                PsiTreeUtil.getParentOfType(psiElement, OMTCommandBlock.class) != null;     // or anything inside a command block
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
