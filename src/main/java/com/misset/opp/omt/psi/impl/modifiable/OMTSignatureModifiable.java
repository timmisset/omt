package com.misset.opp.omt.psi.impl.modifiable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.misset.opp.omt.psi.OMTTypes.OPERATOR_CALL;

public abstract class OMTSignatureModifiable extends OMTModifiableContainerImpl implements OMTModifiableContainer, OMTSignature {
    public OMTSignatureModifiable(@NotNull ASTNode node) {
        super(node, OMTSignatureArgument.class, OMTTypes.COMMA,
                node.getTreeParent() == null || node.getTreeParent().getElementType() == OPERATOR_CALL,
                false);
    }

    @Override
    List<? extends PsiElement> getContainerElements() {
        return getSignatureArgumentList();
    }
}
