package com.misset.opp.omt.psi.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTIfBlock;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTTypes;

public class ScriptAnnotator extends OMTAnnotations {

    public void annotateSemicolonForScriptContent(OMTScriptContent scriptContent, AnnotationHolder holder) {
        final PsiElement psiElement = PsiTreeUtil.nextVisibleLeaf(scriptContent);
        if (psiElement == null || psiElement.getNode().getElementType() != OMTTypes.SEMICOLON) {
            holder
                    .newAnnotation(HighlightSeverity.ERROR, "; expected")
                    .range(scriptContent)
                    .create();
        }
    }

    public void annotateIfBlock(OMTIfBlock omtIfBlock, AnnotationHolder holder) {
        annotateBoolean(omtIfBlock.getQuery().resolveToResource(), holder, omtIfBlock.getQuery());
    }

}
