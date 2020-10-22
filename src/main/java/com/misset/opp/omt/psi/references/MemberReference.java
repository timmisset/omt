package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTPsiImplUtil;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import com.misset.opp.omt.psi.util.ImportUtil;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A member reference takes care of all references from operator/command calls to their declarations
 */
public class MemberReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private final MemberUtil memberUtil;
    private final ImportUtil importUtil;

    private final NamedMemberType type;

    public MemberReference(@NotNull PsiElement member, TextRange textRange, NamedMemberType type) {
        super(member, textRange);
        this.type = type;
        memberUtil = MemberUtil.SINGLETON;
        importUtil = ImportUtil.SINGLETON;
    }

    public MemberReference(@NotNull PsiElement member, TextRange textRange, NamedMemberType type, MemberUtil memberUtil, ImportUtil importUtil) {
        super(member, textRange);
        this.type = type;
        this.memberUtil = memberUtil;
        this.importUtil = importUtil;
    }


    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        ResolveResult[] result;
        switch (type) {
            case ImportingMember:
                result = toResolveResult(importUtil.resolveImportMember((OMTMember) myElement).orElse(myElement));
                break;
            case ExportingMember:
                result = toResolveResult(memberUtil.getDeclaringMemberFromImport(myElement, ((OMTMember) myElement).getName())
                        .orElse(myElement));
                break;
            case OperatorCall:
                result = declaringMemberToResolveResult(memberUtil.getDeclaringMember((OMTOperatorCall) myElement).orElse(null));
                break;
            case CommandCall:
                result = declaringMemberToResolveResult(memberUtil.getDeclaringMember((OMTCommandCall) myElement).orElse(null));
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
                element = OMTPsiImplUtil.setName((OMTMember) super.myElement, newElementName);
                break;
            case ModelItem:
                element = OMTPsiImplUtil.setName((OMTModelItemLabel) super.myElement, newElementName);
                break;
            case DefineName:
                element = OMTPsiImplUtil.setName((OMTDefineName) super.myElement, newElementName);
                break;
            case OperatorCall:
                element = OMTPsiImplUtil.setName((OMTOperatorCall) super.myElement, newElementName);
                break;
            case CommandCall:
                element = OMTPsiImplUtil.setName((OMTCommandCall) super.myElement, newElementName);
                break;
        }
        return element;
    }
}
