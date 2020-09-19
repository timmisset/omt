package com.misset.opp.omt.psi.util;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnnotationUtil {

    /**
     * Generic method to check if the declaration of an element (variable, prefix etc) is ever referred to by another element
     * @param element
     * @param usageClass
     * @param holder
     */
    public static void annotateUsage(PsiElement element, Class<? extends PsiElement> usageClass, @NotNull AnnotationHolder holder) {
        Collection<? extends PsiElement> usages = PsiTreeUtil.findChildrenOfType(element.getContainingFile(), usageClass);
        for(PsiElement usage : usages) {
            if(!usage.isEquivalentTo(element) &&
                    usage.getReference() != null &&
                    usage.getReference().isReferenceTo(element)) {
                return;
            }
        }
        holder.createErrorAnnotation(element, String.format("%s is never used", element.getText()));
    }

    /**
     * Generic method to check if the element, as an usage of a variable, or a prefix etc, can resolve to it's original declaration
     * This is used to check that variables are declared, prefixes are mapped etc
     * @param element
     * @param holder
     */
    public static void annotateOrigin(PsiElement element, @NotNull AnnotationHolder holder) {
        if(element.getReference() != null && element.getReference().resolve() == null) {
            holder.createErrorAnnotation(element, String.format("%s is not declared", element.getText()));
        }
    }
}
