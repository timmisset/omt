package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTModelItemLabel;
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
        final PsiElement declaringMember = resolve();
        return declaringMember != null &&
                (declaringMember.equals(element)
                        || element.equals(isReferenceToTarget(declaringMember))
                        || isDeferredImportReference(element));
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

    /**
     * The declaring member is the element which is registered as the actual reference for the element.
     * However, the isReferenceTo is used for renaming also in which case the nameIdentifier is used.
     * <p>
     * For example:
     * When a rename is started from a call to a query, the reference resolved (declaring member) will return
     * the OMTDefinedStatement but the nameIdentifier is the OMTDefineName which is the element provided
     * for the isReferenceTo comparison
     * <p>
     * TODO:
     * Check if this a design flaw in the named elements for this plugin or this is simply how it should work
     *
     * @param declaringMember
     * @return
     */
    protected PsiElement isReferenceToTarget(PsiElement declaringMember) {
        if (declaringMember instanceof OMTModelItemLabel) {
            return ((OMTModelItemLabel) declaringMember).getPropertyLabel();
        }
        if (declaringMember instanceof OMTDefinedStatement) {
            return ((OMTDefinedStatement) declaringMember).getDefineName();
        }
        return null;
    }
}
