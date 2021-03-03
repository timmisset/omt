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

/**
 * There is some code duplication in this class due to it inheriting from different type branches
 * It's much easier to reproduce the LabelledElement methods and inherit the ModifiableContainer properties
 * ModifiableContainer should not inherit from LabelledElement
 */
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
    public String getName() {
        String propertyLabelText = getLabel().getText();
        return propertyLabelText.endsWith(":") ?
                propertyLabelText.substring(0, propertyLabelText.length() - 1) :
                propertyLabelText;
    }

    @Override
    public OMTBlock getBlock() {
        return null;
    }
}
