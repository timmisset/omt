package com.misset.opp.omt;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.*;
import org.jetbrains.annotations.NotNull;

public class OMTAnnotator implements Annotator {

    private static final ModelUtil modelUtil = ModelUtil.SINGLETON;
    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;
    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;
    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ScriptUtil scriptUtil = ScriptUtil.SINGLETON;

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof OMTVariable) {
            variableUtil.annotateVariable((OMTVariable) element, holder);
        }
        if (element instanceof OMTNamespacePrefix) {
            curieUtil.annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
        }
        if (element instanceof OMTImport) {
            importUtil.annotateImport((OMTImport) element, holder);
        }
        if (element instanceof OMTCommandCall) {
            memberUtil.annotateCall((OMTCommandCall) element, holder);
        }
        if (element instanceof OMTOperatorCall) {
            memberUtil.annotateCall((OMTOperatorCall) element, holder);
        }
        if (element instanceof OMTModelItemTypeElement) {
            modelUtil.annotateModelItemType((OMTModelItemTypeElement) element, holder);
        }
        if (element instanceof OMTBlockEntry) {
            modelUtil.annotateBlockEntry((OMTBlockEntry) element, holder);
        }
        if (element instanceof OMTBlock) {
            modelUtil.annotateBlock((OMTBlock) element, holder);
        }
        if (element instanceof OMTReturnStatement) {
            scriptUtil.annotateFinalStatement(element, holder);
        }
    }

}

