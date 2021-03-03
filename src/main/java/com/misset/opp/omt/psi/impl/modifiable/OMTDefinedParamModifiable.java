package com.misset.opp.omt.psi.impl.modifiable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.omt.psi.OMTVariable;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTDefinedParamModifiable extends OMTModifiableContainerImpl implements OMTModifiableContainer, OMTDefineParam {
    public OMTDefinedParamModifiable(@NotNull ASTNode node) {
        super(node, OMTVariable.class, OMTTypes.COMMA, true);
    }

    @Override
    List<? extends PsiElement> getContainerElements() {
        return getVariableList();
    }
}
