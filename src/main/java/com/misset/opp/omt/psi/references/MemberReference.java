package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.util.UtilManager.getImportUtil;

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

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        if (myElement == element) return false;
        final PsiElement declaringMember = resolve();
        if (declaringMember == null) return false;

        if (element instanceof OMTMember &&
                ((OMTMember) myElement).getType() == NamedMemberType.ImportingMember) {
            // checks if this is reference to an import member statement, an import can be a reference to
            // an import statement in another file:
            // queries.omt => contains queryA
            // queries2.omt => contains queryB
            // queries3.omt => imports queryA and queryB from queries.omt AND queries2.omt but
            // doesn't use them in the file itself
            // usage.omt => imports queryA and queryB from queries3.omt

            // to make sure the import statements in queries3.omt get the proper highlighting, we need to
            // validate that they are (re)imported in usage.omt
            // when the method gets to this point we assert being in file usage.omt and receiving a
            // isReferenceTo to an import from queries3.omt that share the same name
            final PsiElement targetElementOfElement = element.getReference() != null ? element.getReference().resolve() : null;

            final VirtualFile importedFile = getImportedFile();
            final PsiFile containingFile = element.getContainingFile();

            return targetElementOfElement != null &&
                    importedFile != null &&
                    importedFile.exists() &&
                    containingFile != null &&
                    containingFile.getVirtualFile() != null &&
                    targetElementOfElement == declaringMember && // resolve to the same final element
                    importedFile.equals(containingFile.getVirtualFile()); // and the current member is importing from the target file
        } else if (element instanceof OMTMember &&
                ((OMTMember) myElement).getType() == NamedMemberType.ExportingMember) {
            final PsiElement targetElementOfElement = element.getReference() != null ? element.getReference().resolve() : null;
            return targetElementOfElement != null &&
                    targetElementOfElement == resolve() &&
                    myElement.getContainingFile() == element.getContainingFile();
        } else {
            return
                    (declaringMember.equals(element) ||
                            element.equals(isReferenceToTarget(declaringMember)));
        }
    }

    private VirtualFile getImportedFile() {
        // retrieves the file that is used to import the member from:
        final OMTImport omtImport = PsiTreeUtil.getParentOfType(myElement, OMTImport.class);
        return omtImport != null ?
                getImportUtil().getImportedFile(omtImport) :
                null;
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
