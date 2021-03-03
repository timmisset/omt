package com.misset.opp.omt.psi.impl.modifiable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTLeading;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.OMTPrefixBlock;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTPrefixBlockModifiable extends OMTModifiableContainerImpl implements OMTModifiableContainer, OMTPrefixBlock {
    public OMTPrefixBlockModifiable(@NotNull ASTNode node) {
        super(node, OMTPrefix.class, null, true);
    }

    @Override
    List<? extends PsiElement> getContainerElements() {
        return getPrefixList();
    }

    @Override
    public PsiElement getLabel() {
        final PsiElement firstChild = getFirstChild();
        return firstChild instanceof OMTLeading ?
                PsiTreeUtil.nextVisibleLeaf(firstChild) :
                firstChild;
    }

    @Override
    public OMTBlock getBlock() {
        return null;
    }
}
