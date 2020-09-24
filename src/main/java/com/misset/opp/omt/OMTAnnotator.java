package com.misset.opp.omt;

//import com.intellij.lang.annotation.Annotation;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.*;
import org.jetbrains.annotations.NotNull;

public class OMTAnnotator implements Annotator {

    final static ModelUtil modelUtil = ModelUtil.SINGLETON;
    final static CurieUtil curieUtil = CurieUtil.SINGLETON;
    final static MemberUtil memberUtil = MemberUtil.SINGLETON;

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof OMTVariable) {
            VariableUtil.annotateVariable((OMTVariable) element, holder);
        }
        if (element instanceof OMTNamespacePrefix) {
            curieUtil.annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
        }
        if (element instanceof OMTImport) {
            ImportUtil.annotateImport((OMTImport) element, holder);
        }
        if (element instanceof OMTCommandCall) {
            memberUtil.annotateCall((OMTCommandCall) element, holder);
        }
        if (element instanceof OMTOperatorCall) {
            memberUtil.annotateCall((OMTOperatorCall) element, holder);
        }
        if (element instanceof OMTModelItemBlock) {
            modelUtil.annotateModelItem((OMTModelItemBlock) element, holder);
        }
        if (element instanceof OMTReturnStatement) {
            ScriptUtil.annotateFinalStatement(element, holder);
        }
    }

}

