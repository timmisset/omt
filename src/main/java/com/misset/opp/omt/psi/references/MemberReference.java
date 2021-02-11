package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A member reference takes care of all references from operator/command calls to their declarations
 */
public abstract class MemberReference<T extends PsiElement> extends PsiReferenceBase<T> implements PsiPolyVariantReference {

    public MemberReference(@NotNull T member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return ResolveResult.EMPTY_ARRAY;
    }

    protected ResolveResult[] toResolveResult(PsiElement element) {
        return new ResolveResult[]{new PsiElementResolveResult(element)};
    }

    protected ResolveResult[] declaringMemberToResolveResult(PsiElement declaringMember) {
        // resolve to either the importing member or the defineName in the DEFINE QUERY [defineName](...) =>
        if (declaringMember != null) {
            if (declaringMember instanceof OMTDefinedStatement) {
                declaringMember = ((OMTDefinedStatement) declaringMember).getDefineName();
            }
            return new ResolveResult[]{new PsiElementResolveResult(declaringMember)};
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return isDeferredImportReference(element) || super.isReferenceTo(element);
    }

    private boolean isDeferredImportReference(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, OMTImport.class) != null &&
                element != myElement &&
                element.getContainingFile() == myElement.getContainingFile() &&
                element.getReference() != null && myElement.getReference() != null &&
                element.getReference().resolve() == myElement.getReference().resolve();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }
}
