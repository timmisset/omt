package com.misset.opp.omt.psi.impl.modifiable;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.support.OMTModifiableContainer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public abstract class OMTModifiableContainerImpl extends ASTWrapperPsiElement implements OMTModifiableContainer {
    private final IElementType delimiterType;
    private final Class<? extends PsiElement> elementType;
    private final boolean removeSelfIfEmpty;

    protected OMTModifiableContainerImpl(
            @NotNull ASTNode node,
            Class<? extends PsiElement> elementType,
            IElementType delimiterType,
            boolean removeSelfIfEmpty) {
        super(node);
        this.delimiterType = delimiterType;
        this.elementType = elementType;
        this.removeSelfIfEmpty = removeSelfIfEmpty;
    }

    protected void removeVisibleLeafBeforeOrAfterIfOfType(PsiElement element) {
        if (delimiterType == null) {
            return;
        }
        final PsiElement prevLeaf = PsiTreeUtil.prevVisibleLeaf(element);
        if (prevLeaf.getNode().getElementType() == delimiterType) {
            prevLeaf.delete();
        } else {
            final PsiElement nextLeaf = PsiTreeUtil.nextVisibleLeaf(element);
            if (nextLeaf.getNode().getElementType() == delimiterType) {
                nextLeaf.delete();
            }
        }
    }

    @Override
    public void removeChild(PsiElement psiElement) {
        if (!elementType.isAssignableFrom(psiElement.getClass())) {
            psiElement = PsiTreeUtil.getParentOfType(psiElement, elementType);
            if (psiElement == null) return;
        }

        removeVisibleLeafBeforeOrAfterIfOfType(psiElement);
        psiElement.delete();

        if (removeSelfIfEmpty && numberOfChildren() == 0) {
            this.delete();
        }
    }

    @Override
    public int getChildPosition(PsiElement element) {
        final Optional<? extends PsiElement> containingElement = getContainerElements().stream().filter(
                childElement ->
                        PsiTreeUtil.isAncestor(childElement, element, false)
        ).findFirst();
        return containingElement.isPresent() ?
                getContainerElements().indexOf(containingElement.get()) :
                -1;
    }

    abstract List<? extends PsiElement> getContainerElements();

    @Override
    public void removeChildAtPosition(int position) {
        if (numberOfChildren() > position) {
            removeChild(getContainerElements().get(position));
        }
    }

    @Override
    public int numberOfChildren() {
        return getContainerElements().size();
    }
}
