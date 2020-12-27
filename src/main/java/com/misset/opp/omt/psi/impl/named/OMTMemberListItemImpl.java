package com.misset.opp.omt.psi.impl.named;

import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTMemberListItem;
import com.misset.opp.omt.psi.support.OMTNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class OMTMemberListItemImpl extends OMTNamedElementImpl implements OMTNamedElement {

    public OMTMemberListItemImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final OMTMemberListItem listItem = getNode().getPsi(OMTMemberListItem.class);
        return listItem.getMember() == null ? "" : listItem.getName();
    }
}
