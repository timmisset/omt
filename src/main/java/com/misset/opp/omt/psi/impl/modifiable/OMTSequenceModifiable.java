package com.misset.opp.omt.psi.impl.modifiable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTSequence;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTSequenceModifiable extends OMTModifiableContainerImpl implements OMTModifiableContainer, OMTSequence {
    public OMTSequenceModifiable(@NotNull ASTNode node) {
        super(node, OMTSequenceItem.class, null, true);
    }

    @Override
    List<? extends PsiElement> getContainerElements() {
        return getSequenceItemList();
    }
}
