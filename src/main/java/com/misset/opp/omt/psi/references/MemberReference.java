package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.util.PsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.omt.psi.util.UtilManager.getImportUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getMemberUtil;

/**
 * A member reference takes care of all references from operator/command calls to their declarations
 */
public class MemberReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private final NamedMemberType type;

    public MemberReference(@NotNull PsiElement member, TextRange textRange, NamedMemberType type) {
        super(member, textRange);
        this.type = type;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        ResolveResult[] result;
        switch (type) {
            case ImportingMember:
                result = toResolveResult(getImportUtil().resolveImportMember((OMTMember) myElement).orElse(myElement));
                break;
            case ExportingMember:
                result = toResolveResult(getMemberUtil().getDeclaringMemberFromImport(myElement, ((OMTMember) myElement).getName())
                        .orElse(myElement));
                break;
            case OperatorCall:
                result = declaringMemberToResolveResult(getMemberUtil().getDeclaringMember((OMTOperatorCall) myElement).orElse(null));
                break;
            case CommandCall:
                result = declaringMemberToResolveResult(getMemberUtil().getDeclaringMember((OMTCommandCall) myElement).orElse(null));
                break;
            case ModelItem:
            case DefineName:
            default: // not possible, added for branch coverage
                result = toResolveResult(myElement);
        }
        return result;
    }

    private ResolveResult[] toResolveResult(PsiElement element) {
        return new ResolveResult[]{new PsiElementResolveResult(element)};
    }

    private ResolveResult[] declaringMemberToResolveResult(PsiElement declaringMember) {
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
        return PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTImport) != null &&
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

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        PsiElement element;
        switch (type) {
            default: // not possible, added for branch coverage
            case ImportingMember:
            case ExportingMember:
                element = PsiImplUtil.setName((OMTMember) super.myElement, newElementName);
                break;
            case ModelItem:
                element = PsiImplUtil.setName((OMTModelItemLabel) super.myElement, newElementName);
                break;
            case DefineName:
                element = PsiImplUtil.setName((OMTDefineName) super.myElement, newElementName);
                break;
            case OperatorCall:
                element = PsiImplUtil.setName((OMTOperatorCall) super.myElement, newElementName);
                break;
            case CommandCall:
                element = PsiImplUtil.setName((OMTCommandCall) super.myElement, newElementName);
                break;
        }
        return element;
    }
}
