package com.misset.opp.omt;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.annotations.QueryAnnotations;
import com.misset.opp.omt.psi.named.NamedMemberType;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class OMTAnnotator implements Annotator {

    private static final QueryAnnotations queryAnnotations = new QueryAnnotations();

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof OMTVariable) {
            getVariableUtil().annotateVariable((OMTVariable) element, holder);
        }
        if (element instanceof OMTNamespacePrefix) {
            getCurieUtil().annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
        }
        if (element instanceof OMTImport) {
            getImportUtil().annotateImport((OMTImport) element, holder);
        }
        if (element instanceof OMTImportSource) {
            getImportUtil().annotateImportSource((OMTImportSource) element, holder);
        }
        if (element instanceof OMTMember) {
            if (((OMTMember) element).getType() == NamedMemberType.ImportingMember) {
                getMemberUtil().annotateImportedMember((OMTMember) element, holder);
            }
        }
        if (element instanceof OMTDefineParam) {
            getVariableUtil().annotateDefineParameter((OMTDefineParam) element, holder);
        }
        if (element instanceof OMTCommandCall) {
            getMemberUtil().annotateCall((OMTCommandCall) element, holder);
        }
        if (element instanceof OMTOperatorCall) {
            getMemberUtil().annotateCall((OMTOperatorCall) element, holder);
        }
        if (element instanceof OMTModelItemTypeElement) {
            getModelUtil().annotateModelItemType((OMTModelItemTypeElement) element, holder);
        }
        if (element instanceof OMTBlockEntry) {
            getModelUtil().annotateBlockEntry((OMTBlockEntry) element, holder);
        }
        if (element instanceof OMTBlock) {
            getModelUtil().annotateBlock((OMTBlock) element, holder);
        }
        if (element instanceof OMTReturnStatement) {
            getScriptUtil().annotateFinalStatement(element, holder);
        }
        if (element instanceof OMTCurieElement) {
            getCurieUtil().annotateCurieElement((OMTCurieElement) element, holder);
        }
        if (element instanceof OMTParameterType) {
            getCurieUtil().annotateParameterType((OMTParameterType) element, holder);
        }
        if (element instanceof OMTParameterWithType) {
            getVariableUtil().annotateParameterWithType((OMTParameterWithType) element, holder);
        }
        if (element instanceof OMTAddToCollection) {
            queryAnnotations.annotateAddToCollection((OMTAddToCollection) element, holder);
        }
        if (element instanceof OMTRemoveFromCollection) {
            queryAnnotations.annotateRemoveFromCollection((OMTRemoveFromCollection) element, holder);
        }
        if (element instanceof OMTAssignmentStatement) {
            queryAnnotations.annotateAssignmentStatement((OMTAssignmentStatement) element, holder);
        }
        if (element instanceof OMTEquationStatement) {
            queryAnnotations.annotateEquationStatement((OMTEquationStatement) element, holder);
        }
        if (element instanceof OMTBooleanStatement) {
            queryAnnotations.annotateBooleanStatement((OMTBooleanStatement) element, holder);
        }
        if (element instanceof OMTIfBlock) {
            queryAnnotations.annotateIfBlock((OMTIfBlock) element, holder);
        }

        if (element instanceof OMTQueryStep) {
            if (element.getParent() instanceof OMTQueryReverseStep) {
                return;
            }
            queryAnnotations.annotateQueryStep((OMTQueryStep) element, holder);
        }
        if (element instanceof OMTQueryPath) {
            queryAnnotations.annotateQueryPath((OMTQueryPath) element, holder);
        }
    }

}

