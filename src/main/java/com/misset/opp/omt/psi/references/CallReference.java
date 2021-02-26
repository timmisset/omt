package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.named.OMTCall;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getMemberUtil;

public class CallReference extends MemberReference<OMTCall> {
    public CallReference(@NotNull OMTCall member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return declaringMemberToResolveResult(getMemberUtil().getDeclaringMember(myElement).orElse(null));
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        final PsiElement declaringMember = getMemberUtil().getDeclaringMember(myElement).orElse(null);
        return declaringMember != null &&
                (declaringMember.equals(element) || element.equals(isReferenceToTarget(declaringMember)));
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
    private PsiElement isReferenceToTarget(PsiElement declaringMember) {
        if (declaringMember instanceof OMTModelItemLabel) {
            return ((OMTModelItemLabel) declaringMember).getPropertyLabel();
        }
        if (declaringMember instanceof OMTDefinedStatement) {
            return ((OMTDefinedStatement) declaringMember).getDefineName();
        }
        return null;
    }
}
