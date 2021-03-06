// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTMember;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.references.ExportMemberReference;
import com.misset.opp.omt.psi.references.ImportMemberReference;
import com.misset.opp.omt.psi.references.MemberReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OMTMemberImpl extends MemberNamedElementImpl<OMTMember> implements OMTMember {

    private NamedMemberType type;

    public OMTMemberImpl(@NotNull ASTNode node) {
        super(node, OMTMember.class);
    }

    @NotNull
    @Override
    public String getName() {
        return getNameIdentifier().getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        OMTMember replacement = OMTElementFactory.createMember(getProject(), newName);
        getNameIdentifier().replace(replacement.getNameIdentifier());
        return replacement;
    }

    @Override
    @NotNull
    public PsiElement getNameIdentifier() {
        return getFirstChild();
    }

    @Nullable
    @Override
    public PsiReference getReference() {
        final MemberReference<OMTMember> omtMemberMemberReference = getType() == NamedMemberType.ImportingMember ?
                new ImportMemberReference(getPsi(), getNameIdentifier().getTextRangeInParent()) :
                new ExportMemberReference(getPsi(), getNameIdentifier().getTextRangeInParent());
        return omtMemberMemberReference;
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.allScope(getProject()),
                OMTFileType.INSTANCE
        );
    }

    @NotNull
    @Override
    public NamedMemberType getType() {
        // do not add this to the constructor, the getPsi() will cause a stackoverflow during construction
        if (type == null) {
            type = PsiTreeUtil.getParentOfType(getPsi(), OMTImport.class) != null ?
                    NamedMemberType.ImportingMember :
                    NamedMemberType.ExportingMember;
        }
        return type;
    }
}
