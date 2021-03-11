package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForeignReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final PsiElement targetElement;

    /**
     * Create a reference from a foreign language into the OMT language
     *
     * @param element
     * @param target
     */
    public ForeignReference(@NotNull PsiElement element, TextRange textRange, @NotNull PsiElement target) {
        super(element, textRange);
        targetElement = target;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return new ResolveResult[]{new PsiElementResolveResult(targetElement)};
    }

    @Override
    public @Nullable PsiElement resolve() {
        return targetElement;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return targetElement.equals(element);
    }
}
