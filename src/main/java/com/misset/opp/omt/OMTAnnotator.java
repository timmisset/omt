package com.misset.opp.omt;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.annotations.QueryAnnotator;
import com.misset.opp.omt.psi.annotations.ScriptAnnotator;
import com.misset.opp.omt.psi.annotations.VariableAnnotator;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTCall;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.UtilManager.*;

public class OMTAnnotator implements Annotator {

    private static final QueryAnnotator queryAnnotations = new QueryAnnotator();
    private static final ScriptAnnotator scriptAnnotations = new ScriptAnnotator();
    private static final VariableAnnotator variableAnnotations = new VariableAnnotator();

    private void doAnnoation(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof LeafPsiElement) {
            annotateLeaf(element, holder);
            return;
        }
        if (element instanceof OMTVariable) {
            variableAnnotations.annotateVariable((OMTVariable) element, holder);
            return;
        }
        if (element instanceof OMTNamespacePrefix) {
            getCurieUtil().annotateNamespacePrefix((OMTNamespacePrefix) element, holder);
            return;
        }
        if (element instanceof OMTImport) {
            getImportUtil().annotateImport((OMTImport) element, holder);
            return;
        }
        if (element instanceof OMTImportSource) {
            getImportUtil().annotateImportSource((OMTImportSource) element, holder);
            return;
        }
        if (element instanceof OMTMember) {
            if (((OMTMember) element).getType() == NamedMemberType.ImportingMember) {
                getMemberUtil().annotateImportedMember((OMTMember) element, holder);
            }
            return;
        }
        if (element instanceof OMTDefineParam) {
            getVariableUtil().annotateDefineParameter((OMTDefineParam) element, holder);
            return;
        }
        if (element instanceof OMTCall) {
            getMemberUtil().annotateCall((OMTCall) element, holder);
            return;
        }
        if (element instanceof OMTModelItemTypeElement) {
            getModelUtil().annotateModelItemType((OMTModelItemTypeElement) element, holder);
            return;
        }
        if (element instanceof OMTBlockEntry) {
            getModelUtil().annotateBlockEntry((OMTBlockEntry) element, holder);
            return;
        }
        if (element instanceof OMTBlock) {
            getModelUtil().annotateBlock((OMTBlock) element, holder);
            return;
        }
        if (element instanceof OMTReturnStatement) {
            getScriptUtil().annotateFinalStatement(element, holder);
            return;
        }
        if (element instanceof OMTCurieElement) {
            getCurieUtil().annotateCurieElement((OMTCurieElement) element, holder);
            return;
        }
        if (element instanceof OMTParameterType) {
            getCurieUtil().annotateParameterType((OMTParameterType) element, holder);
            return;
        }
        if (element instanceof OMTParameterWithType) {
            getVariableUtil().annotateParameterWithType((OMTParameterWithType) element, holder);
            return;
        }
        if (element instanceof OMTScriptContent) {
            scriptAnnotations.annotateSemicolonForScriptContent((OMTScriptContent) element, holder);
            return;
        }
        if (element instanceof OMTDefineQueryStatement) {
            queryAnnotations.annotateSemicolonForDefinedQueryStatement((OMTDefineQueryStatement) element, holder);
            return;
        }
        if (element instanceof OMTAddToCollection) {
            queryAnnotations.annotateAddToCollection((OMTAddToCollection) element, holder);
            return;
        }
        if (element instanceof OMTRemoveFromCollection) {
            queryAnnotations.annotateRemoveFromCollection((OMTRemoveFromCollection) element, holder);
            return;
        }
        if (element instanceof OMTAssignmentStatement) {
            queryAnnotations.annotateAssignmentStatement((OMTAssignmentStatement) element, holder);
            return;
        }
        if (element instanceof OMTEquationStatement) {
            queryAnnotations.annotateEquationStatement((OMTEquationStatement) element, holder);
            return;
        }
        if (element instanceof OMTBooleanStatement) {
            queryAnnotations.annotateBooleanStatement((OMTBooleanStatement) element, holder);
            return;
        }
        if (element instanceof OMTIfBlock) {
            scriptAnnotations.annotateIfBlock((OMTIfBlock) element, holder);
            return;
        }

        if (element instanceof OMTQueryStep) {
            if (element.getParent() instanceof OMTQueryReverseStep) {
                return;
            }
            queryAnnotations.annotateQueryStep((OMTQueryStep) element, holder);
        }
    }

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        doAnnoation(element, holder);
    }

    private void annotateLeaf(PsiElement element, AnnotationHolder holder) {
        // annotate leaf elements for syntax checking
        if (element.getNode().getElementType() == OMTTypes.FORWARD_SLASH &&
                PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTQueryPath) != null
        ) {
            annotateUnexpectedCharacter(HighlightSeverity.ERROR, element, OMTTypes.FORWARD_SLASH, holder);
        } else if (element.getNode().getElementType() == OMTTypes.SEMICOLON) {
            annotateUnexpectedCharacter(HighlightSeverity.WARNING, element, OMTTypes.SEMICOLON, holder);
        }
    }

    private void annotateUnexpectedCharacter(HighlightSeverity severity, PsiElement element, IElementType unexpectedPrevNode, AnnotationHolder holder) {
        final PsiElement prevLeaf = PsiTreeUtil.prevVisibleLeaf(element);
        if (prevLeaf != null && prevLeaf.getNode().getElementType() == unexpectedPrevNode) {
            holder.newAnnotation(severity, "Unexpected character")
                    .range(element)
                    .create();
        }

    }
}

