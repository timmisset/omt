package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AnnotationUtil {

    /**
     * Generic method to check if the declaration of an element (variable, prefix etc) is ever referred to by another element
     *
     * @param element
     * @param usageClass
     * @param holder
     */
    public void annotateUsage(PsiElement element, Class<? extends PsiElement> usageClass, @NotNull AnnotationHolder holder) {
        AnnotationBuilder annotationBuilder = annotateUsageGetBuilder(element, usageClass, holder);
        if (annotationBuilder != null) {
            annotationBuilder
                    .withFix(new RemoveIntention().getRemoveIntention(element))
                    .create();
        }
    }

    public AnnotationBuilder annotateUsageGetBuilder(PsiElement element, Class<? extends PsiElement> usageClass, @NotNull AnnotationHolder holder) {
        List<? extends PsiElement> usages = PsiImplUtil.getUsages(element, usageClass);
        if (!usages.isEmpty()) {
            return null;
        }
        return holder.newAnnotation(HighlightSeverity.WARNING, String.format("%s is never used", element.getText())).range(element);
    }

    /**
     * Generic method to check if the element, as an usage of a variable, or a prefix etc, can resolve to it's original declaration
     * This is used to check that variables are declared, prefixes are mapped etc
     *
     * @param element
     * @param holder
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
     *
     * @param element
     * @param holder
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
