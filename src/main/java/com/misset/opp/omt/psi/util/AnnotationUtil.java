package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;
import org.jetbrains.annotations.NotNull;

public class AnnotationUtil {

    /**
     * Generic method to check if the declaration of an element (variable, prefix etc) is ever referred to by another element
     */
    public void annotateUsage(PsiElement element, @NotNull AnnotationHolder holder) {
        AnnotationBuilder annotationBuilder = annotateUsageGetBuilder(element, holder);
        if (annotationBuilder != null) {
            annotationBuilder
                    .withFix(new RemoveIntention().getRemoveIntention(element))
                    .create();
        }
    }

    public AnnotationBuilder annotateUsageGetBuilder(PsiElement element, @NotNull AnnotationHolder holder) {
        if (ReferencesSearch.search(element)
                .anyMatch(psiReference -> element != psiReference.getElement())) {
            return null;
        } // no error, it's being used
        return holder.newAnnotation(HighlightSeverity.WARNING, String.format("%s is never used", element.getText())).range(element);
    }

    /**
     * Generic method to check if the element, as an usage of a variable, or a prefix etc, can resolve to it's original declaration
     * This is used to check that variables are declared, prefixes are mapped etc
     */
    public void annotateOrigin(PsiElement element, @NotNull AnnotationHolder holder) {
        AnnotationBuilder annotationBuilder = annotateOriginGetBuilder(element, holder);
        if (annotationBuilder != null) {
            annotationBuilder.create();
        }
    }

    /**
     * Generic method to check if the element, as an usage of a variable, or a prefix etc, can resolve to it's original declaration
     * This is used to check that variables are declared, prefixes are mapped etc
     */
    public AnnotationBuilder annotateOriginGetBuilder(PsiElement element, @NotNull AnnotationHolder holder) {
        if (element.getReference() != null && element.getReference().resolve() == null) {
            return holder.newAnnotation(HighlightSeverity.ERROR, String.format("%s is not declared", element.getText()))
                    .range(element)
                    .withFix(new RemoveIntention().getRemoveIntention(element));
        }
        return null;
    }
}
