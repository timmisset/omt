package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.CachedPsiElement;
import com.misset.opp.omt.psi.support.OMTNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTNamedElementImpl extends CachedPsiElement implements OMTNamedElement {

    public OMTNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }
}
