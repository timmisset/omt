package com.misset.opp.omt;

import com.intellij.lang.annotation.*;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.util.CurieUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.misset.opp.omt.psi.intentions.prefix.registerPrefixIntention.getRegisterPrefixIntention;
import static com.misset.opp.omt.psi.intentions.prefix.removePrefixIntention.getRemovePrefixIntention;


public class OMTAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
        if(element instanceof OMTCurieElement) {
            annotateCurie(new OMTCurie((OMTCurieElement)element), holder);
        }
        else if(element instanceof OMTCurieConstantElement) {
            annotateCurie(new OMTCurie((OMTCurieConstantElement)element), holder);
        }
        else if(element instanceof OMTPrefix) {
            annotatePrefix((OMTPrefix)element, holder);
        }
//        if(element instanceof OMTQueryPath) {
//            // annotate queryPath (pol:someThing)
//            annotateQueryPath((OMTQueryPath)element, holder);
//        }
//        if(element instanceof OMTQueryOperator) {
//            annotateQueryOperator((OMTQueryOperator)element, holder);
//        }
//        if (!(element instanceof PsiLiteralExpression)) return;
    }

//    private void annotateVariable(OMTVariableName variable, @NotNull AnnotationHolder holder) {
//        if(!OMTVariableUtil.isDeclared(variable)) {
//            holder.createErrorAnnotation(variable, "Variable is not defined");
//        }
//    }
//    private void annotateQueryPath(OMTQueryPath queryPath, @NotNull AnnotationHolder holder) {
//        if(!OMTQueryUtil.isPrefixDefined(queryPath)) {
//            holder.createErrorAnnotation(queryPath.getFirstChild(), String.format("Prefix '%s' is not defined", OMTQueryUtil.getPrefixName(queryPath)));
//        }
//    }
//    private void annotateQueryOperator(OMTQueryOperator queryOperator, @NotNull AnnotationHolder holder) {
//        System.out.println(queryOperator.getFirstChild().getText() + " operator checked");
//        OMTQueryUtil.isQueryDefined(queryOperator);
//    }
    private void annotateCurie(@NotNull OMTCurie curie, @NotNull AnnotationHolder holder) {
        Optional<OMTPrefix> definedByPrefix = CurieUtil.getDefinedByPrefix(curie);
        if(!definedByPrefix.isPresent()) {
            Annotation prefix_is_not_defined = holder.createErrorAnnotation(curie.getElement(), "Prefix is not defined");
            prefix_is_not_defined.registerFix(getRegisterPrefixIntention(curie));
        }
    }
    private void annotatePrefix(@NotNull OMTPrefix prefix, @NotNull AnnotationHolder holder) {
        if(CurieUtil.isPrefixedDefinedMoreThanOnce(prefix)) {
            Annotation prefix_is_defined_more_than_once = holder.createErrorAnnotation(prefix, "Prefix is defined more than once");
            prefix_is_defined_more_than_once.registerFix(getRemovePrefixIntention(prefix));
        }
        if(!CurieUtil.isPrefixUsed(prefix)) {
            Annotation prefix_is_not_used = holder.createWarningAnnotation(prefix, "Prefix is defined but it's value is never used");
            prefix_is_not_used.registerFix(getRemovePrefixIntention(prefix));
        }
    }
}
//
//        // Ensure the Psi element contains a string that starts with the key and separator
//        PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
//        String value = literalExpression.getValue() instanceof String ? (String) literalExpression.getValue() : null;
//        if ((value == null) || !value.startsWith(SIMPLE_PREFIX_STR + SIMPLE_SEPARATOR_STR)) return;
//
//        // Define the text ranges (start is inclusive, end is exclusive)
//        // "simple:key"
//        //  01234567890
//        TextRange prefixRange = TextRange.from(element.getTextRange().getStartOffset(), SIMPLE_PREFIX_STR.length() + 1);
//        TextRange separatorRange = TextRange.from(prefixRange.getEndOffset(), SIMPLE_SEPARATOR_STR.length());
//        TextRange keyRange = new TextRange(separatorRange.getEndOffset(), element.getTextRange().getEndOffset() - 1);
//
//        // Get the list of properties from the Project
//        String possibleProperties = value.substring(SIMPLE_PREFIX_STR.length() + SIMPLE_SEPARATOR_STR.length());
//        Project project = element.getProject();
//        List<OMTProperty> properties = OMTUtil.findProperties(project, possibleProperties);
//
//        // Set the annotations using the text ranges.
//        Annotation keyAnnotation = holder.createInfoAnnotation(prefixRange, null);
//        keyAnnotation.setTextAttributes(DefaultLanguageHighlighterColors.KEYWORD);
//        Annotation separatorAnnotation = holder.createInfoAnnotation(separatorRange, null);
//        separatorAnnotation.setTextAttributes(SimpleSyntaxHighlighter.SEPARATOR);
//        if (properties.isEmpty()) {
//            // No well-formed property found following the key-separator
//            Annotation badProperty = holder.createErrorAnnotation(keyRange, "Unresolved property");
//            badProperty.setTextAttributes(OMTSyntaxHighlighter.BAD_CHARACTER);
//            // ** Tutorial step 18.3 - Add a quick fix for the string containing possible properties
//            badProperty.registerFix(new SimpleCreatePropertyQuickFix(possibleProperties));
//        } else {
//            // Found at least one property
//            Annotation annotation = holder.createInfoAnnotation(keyRange, null);
//            annotation.setTextAttributes(SimpleSyntaxHighlighter.VALUE);
//        }VALUE
