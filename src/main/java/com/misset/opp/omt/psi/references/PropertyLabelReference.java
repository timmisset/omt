package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PropertyLabelReference extends PsiReferenceBase<OMTPropertyLabel> implements PsiPolyVariantReference {

    public PropertyLabelReference(@NotNull OMTPropertyLabel element, TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    @NotNull
    public ResolveResult[] multiResolve(boolean incompleteCode) {

        final String name = getElement().getName();
        final OMTFile containingFile = (OMTFile) getElement().getContainingFile();
        // check if it can be resolved to a prefix:
        if (containingFile.getPrefixes().containsKey(name)) {
            return PsiElementResolveResult.createResults(
                    containingFile.getPrefixes().get(myElement.getName()).getNamespacePrefix()
            );
        }
        return new ResolveResult[0];
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return super.isReferenceTo(element);
    }
}
