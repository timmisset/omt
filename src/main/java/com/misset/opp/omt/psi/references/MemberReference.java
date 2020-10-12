package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTPsiImplUtil;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.util.ImportUtil;
import com.misset.opp.omt.psi.util.MemberUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A member reference takes care of all references from operator/command calls to their declarations
 */
public class MemberReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private static final MemberUtil memberUtil = MemberUtil.SINGLETON;
    private static final ImportUtil importUtil = ImportUtil.SINGLETON;

    private final NamedMemberType type;

    public MemberReference(@NotNull PsiElement member, TextRange textRange, NamedMemberType type) {
        super(member, textRange);
        this.type = type;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        switch (type) {
            case ImportingMember:
                Optional<PsiElement> resolved = importUtil.resolveImportMember((OMTMember) myElement);
                return new ResolveResult[]{
                        resolved.map(PsiElementResolveResult::new).orElseGet(() -> new PsiElementResolveResult(myElement))};
            case ModelItem:
            case DefineName:
                return new ResolveResult[]{new PsiElementResolveResult(myElement)};
            case OperatorCall:
                return declaringMemberToResolveResult(memberUtil.getDeclaringMember((OMTOperatorCall) myElement));
            case CommandCall:
                return declaringMemberToResolveResult(memberUtil.getDeclaringMember((OMTCommandCall) myElement));
            case ExportingMember:
                return new ResolveResult[]{
                        memberUtil.getDeclaringMemberFromImport(myElement, ((OMTMember) myElement).getName())
                                .map(PsiElementResolveResult::new).orElseGet(() -> new PsiElementResolveResult(myElement))};

            default:
                return ResolveResult.EMPTY_ARRAY;
        }
    }

    private ResolveResult[] declaringMemberToResolveResult(Optional<PsiElement> declaringMember) {
        // resolve to either the importing member or the defineName in the DEFINE QUERY [defineName](...) =>
        if(declaringMember.isPresent()) {
            PsiElement element = declaringMember.get();
            if(element instanceof OMTDefineCommandStatement) { element = ((OMTDefineCommandStatement)element).getDefineName(); }
            if(element instanceof OMTDefineQueryStatement) { element = ((OMTDefineQueryStatement)element).getDefineName(); }
            return new ResolveResult[] { new PsiElementResolveResult(element) };
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
        switch (type) {
            case ImportingMember:
            case ExportingMember:
                return OMTPsiImplUtil.setName((OMTMember) super.myElement, newElementName);
            case ModelItem:
                return OMTPsiImplUtil.setName((OMTModelItemLabel) super.myElement, newElementName);
            case DefineName:
                return OMTPsiImplUtil.setName((OMTDefineName) super.myElement, newElementName);
            case OperatorCall:
                return OMTPsiImplUtil.setName((OMTOperatorCall) super.myElement, newElementName);
            case CommandCall:
                return OMTPsiImplUtil.setName((OMTCommandCall) super.myElement, newElementName);
        }
        return super.myElement;
    }
}
