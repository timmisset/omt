package com.misset.opp.omt.annotations;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTTypes;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.OMTTypes.*;

public class OMTAnnotator implements Annotator {

    private static final TokenSet QUERY_ANNOTATIONS = TokenSet.create(
            DEFINE_QUERY_STATEMENT, ADD_TO_COLLECTION, REMOVE_FROM_COLLECTION, ASSIGNMENT_STATEMENT,
            EQUATION_STATEMENT, BOOLEAN_STATEMENT, QUERY_STEP, QUERY_REVERSE_STEP
    );
    private static final TokenSet IMPORT_ANNOTATIONS = TokenSet.create(
            IMPORT_SOURCE, MEMBER
    );
    private static final TokenSet COLLECTION_ANNOTATIONS = TokenSet.create(
            MEMBER_LIST_ITEM, SEQUENCE_ITEM, BLOCK_ENTRY, GENERIC_BLOCK, SPECIFIC_BLOCK
    );
    private static final TokenSet VARIABLE_ANNOTATIONS = TokenSet.create(
            VARIABLE
    );
    private static final TokenSet SCRIPT_ANNOTATIONS = TokenSet.create(
            IF_BLOCK, SCRIPT_CONTENT, SCRIPT_LINE
    );
    private static final TokenSet MODEL_ANNOTATIONS = TokenSet.create(
            MODEL_ITEM_TYPE, BLOCK, GENERIC_BLOCK, BLOCK_ENTRY,
            INDENTED_BLOCK, ROOT_BLOCK, MODEL_ITEM_LABEL, DEFINE_NAME
    );
    private static final TokenSet PARAMETER_ANNOTATIONS = TokenSet.create(
            PARAMETER_WITH_TYPE, PARAMETER_TYPE, DEFINE_PARAM
    );
    private static final TokenSet MEMBER_ANNOTATIONS = TokenSet.create(
            COMMAND_CALL, OPERATOR_CALL, SIGNATURE_ARGUMENT, SIGNATURE
    );
    private static final TokenSet CURIE_ANNOTATIONS = TokenSet.create(
            NAMESPACE_PREFIX, CURIE_ELEMENT
    );

    private void doAnnoation(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        final IElementType elementType = element.getNode().getElementType();
        if (element instanceof LeafPsiElement) {
            annotateLeaf(element, holder);
            return;
        } else if (QUERY_ANNOTATIONS.contains(elementType)) {
            new QueryAnnotator(holder).annotate(element);
        } else if (IMPORT_ANNOTATIONS.contains(elementType)) {
            new ImportAnnotator(holder).annotate(element);
        } else if (COLLECTION_ANNOTATIONS.contains(elementType)) {
            new CollectionAnnotator(holder).annotate(element);
        } else if (VARIABLE_ANNOTATIONS.contains(elementType)) {
            new VariableAnnotator(holder).annotate(element);
        } else if (SCRIPT_ANNOTATIONS.contains(elementType)) {
            new ScriptAnnotator(holder).annotate(element);
        } else if (MODEL_ANNOTATIONS.contains(elementType)) {
            new ModelAnnotator(holder).annotate(element);
        } else if (PARAMETER_ANNOTATIONS.contains(elementType)) {
            new ParameterAnnotator(holder).annotate(element);
        } else if (MEMBER_ANNOTATIONS.contains(elementType)) {
            new MemberAnnotator(holder).annotate(element);
        } else if (CURIE_ANNOTATIONS.contains(elementType)) {
            new CurieAnnotator(holder).annotate(element);
        }
    }

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiWhiteSpace) {
            return;
        }
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

