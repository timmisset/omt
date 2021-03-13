package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.named.OMTCall;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getMemberUtil;
import static com.misset.opp.util.UtilManager.getModelUtil;

public class CallReference extends MemberReference<OMTCall> {
    public CallReference(@NotNull OMTCall member, TextRange textRange) {
        super(member, textRange);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        if (getModelUtil().isPrefixEntry(myElement)) {
            // In an ontology model, a prefix is used as a value (parsed to a CallReference)
            // which means it should resolve to (if present) a prefix to the top of the page
            final OMTFile containingFile = (OMTFile) myElement.getContainingFile();
            if (containingFile.getPrefixes().containsKey(myElement.getName())) {
                return PsiElementResolveResult.createResults(
                        containingFile.getPrefixes().get(myElement.getName()).getNamespacePrefix()
                );
            }
            return ResolveResult.EMPTY_ARRAY;
        } else {
            return declaringMemberToResolveResult(getMemberUtil().getDeclaringMember(myElement).orElse(null));
        }
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        return myElement.setName(newElementName);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        if (element instanceof OMTMember) {
            // checks if this call is the using an imported member by validating that the import
            // and the call both resolve to the same final element (and are part of the same file)
            final PsiElement declaringMember = resolve();
            return declaringMember != null &&
                    isImportedMemberUsage(element, declaringMember);

        } else {
            return super.isReferenceTo(element); // a reference check to something else, use the member method
        }
    }

    private boolean isImportedMemberUsage(PsiElement element, PsiElement declaringMember) {
        final PsiElement targetElementOfElement = element.getContainingFile() == myElement.getContainingFile() &&
                element.getReference() != null ?
                element.getReference().resolve() : null;
        return targetElementOfElement == declaringMember &&
                targetElementOfElement != element;
    }
}
