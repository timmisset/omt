package com.misset.opp.omt;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.util.*;
import org.jetbrains.annotations.NotNull;

public class OMTAnnotator implements Annotator {

    private static final ModelUtil modelUtil = ModelUtil.SINGLETON;
    private static final CurieUtil curieUtil = CurieUtil.SINGLETON;
    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;
    private static final VariableUtil variableUtil = VariableUtil.SINGLETON;
    private static final ScriptUtil scriptUtil = ScriptUtil.SINGLETON;
    private static final QueryUtil queryUtil = QueryUtil.SINGLETON;

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
        if (element instanceof OMTImportSource) {
            importUtil.annotateImportSource((OMTImportSource) element, holder);
        }
        if (element instanceof OMTMember) {
            if (((OMTMember) element).getType() == NamedMemberType.ImportingMember) {
                memberUtil.annotateImportedMember((OMTMember) element, holder);
            }
        }
        if (element instanceof OMTDefineParam) {
            variableUtil.annotateDefineParameter((OMTDefineParam) element, holder);
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
        if (element instanceof OMTCurieElement) {
            curieUtil.annotateCurieElement((OMTCurieElement) element, holder);
        }
        if (element instanceof OMTParameterType) {
            curieUtil.annotateParameterType((OMTParameterType) element, holder);
        }
        if (element instanceof OMTParameterWithType) {
            variableUtil.annotateParameterWithType((OMTParameterWithType) element, holder);
        }
        if (element instanceof OMTAddToCollection) {
            queryUtil.annotateAddToCollection((OMTAddToCollection) element, holder);
        }
        if (element instanceof OMTRemoveFromCollection) {
            queryUtil.annotateRemoveFromCollection((OMTRemoveFromCollection) element, holder);
        }
        if (element instanceof OMTAssignmentStatement) {
            queryUtil.annotateAssignmentStatement((OMTAssignmentStatement) element, holder);
        }
        if (element instanceof OMTEquationStatement) {
            queryUtil.annotateEquationStatement((OMTEquationStatement) element, holder);
        }
        if (element instanceof OMTBooleanStatement) {
            queryUtil.annotateBooleanStatement((OMTBooleanStatement) element, holder);
        }
        if (element instanceof OMTIfBlock) {
            queryUtil.annotateIfBlock((OMTIfBlock) element, holder);
        }

        if (element instanceof OMTQueryStep) {
            if (element.getParent() instanceof OMTQueryReverseStep) {
                return;
            }
            queryUtil.annotateQueryStep((OMTQueryStep) element, holder);
        }
        if (element instanceof OMTQueryPath) {
            queryUtil.annotateQueryPath((OMTQueryPath) element, holder);
        }
    }

}

