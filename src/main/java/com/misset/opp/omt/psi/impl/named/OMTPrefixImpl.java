package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTPrefix;
import org.jetbrains.annotations.NotNull;

public class OMTPrefixImpl extends OMTNamedElementImpl {
    public OMTPrefixImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return String.format("%s:%s",
                getNode().getPsi(OMTPrefix.class).getNamespacePrefix().getName(),
                getNode().getPsi(OMTPrefix.class).getNamespaceIri().getName());
    }
}
