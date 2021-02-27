// This is a generated file. Not intended for manual editing.
package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.misset.opp.omt.OMTFileType;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTElementFactory;
import com.misset.opp.omt.psi.impl.OMTDefinedStatementImpl;
import com.misset.opp.omt.psi.named.NamedMemberType;
import com.misset.opp.omt.psi.support.OMTDefinedStatement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTDefineNameImpl extends MemberNamedElementImpl<OMTDefineName> implements OMTDefineName {

    public OMTDefineNameImpl(@NotNull ASTNode node) {
        super(node, OMTDefineName.class);
    }

    @Override
    @NotNull
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        PsiElement replacement = OMTElementFactory.createOperator(getProject(), newName);
        if (replacement != null) {
            replace(replacement);
        }
        return replacement;
    }

    @Override
    @NotNull
    public PsiElement getNameIdentifier() {
        return this;
    }

    @NotNull
    @Override
    public NamedMemberType getType() {
        return NamedMemberType.DefineName;
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        final OMTDefinedStatement definedStatement = (OMTDefinedStatement) getPsi().getParent();
        return definedStatement.getScope() == OMTDefinedStatementImpl.AvailabilityScope.File ?
                // only check find-usage in the file if it's part of a model item
                // TODO: check option to search in element (model-item) subtree
                GlobalSearchScope.fileScope(getContainingFile()) :
                // check global usage
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(getProject()),
                        OMTFileType.INSTANCE
                );
    }
}
