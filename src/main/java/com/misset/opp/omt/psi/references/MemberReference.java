package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.util.MemberUtil;
import com.misset.opp.omt.psi.util.VariableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A member reference takes care of all references from operator/command calls to their declarations
 */
public class MemberReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private NamedMemberType type;
    public MemberReference(@NotNull PsiElement member, TextRange textRange, NamedMemberType type) {
        super(member, textRange);
        this.type = type;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        switch (type) {
            case ImportingMember:
            case DefineName: return new ResolveResult[] { new PsiElementResolveResult(myElement) };

            case OperatorCall:
                Optional<PsiElement> declaringMember = MemberUtil.getDeclaringMember((OMTOperatorCall) myElement);

                // resolve to either the importing member or the defineName in the DEFINE QUERY [defineName](...) =>
                if(declaringMember.isPresent()) {
                    PsiElement element = declaringMember.get();
                    element = element instanceof OMTDefineQueryStatement ?
                            ((OMTDefineQueryStatement)element).getDefineName() :
                            element;
                    return new ResolveResult[] { new PsiElementResolveResult(element) };
                }
                return ResolveResult.EMPTY_ARRAY;

            case CommandCall: // not supported yet
            default: return ResolveResult.EMPTY_ARRAY;
        }
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
