package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.support.OMTNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTNamespaceIriImpl extends OMTNamedElementImpl implements OMTNamedElement {

    public OMTNamespaceIriImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final String text = getText();
        return text.substring(text.indexOf("<") + 1, text.indexOf(">"));
    }
}
