package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;

/**
 * Container with elements that can be modified by refactoring operations
 * For example a prefixblock with prefixes, a sequenceList with sequenceListItems (parameters, variables)
 */
public interface OMTModifiableContainer extends PsiElement {
    void removeChild(PsiElement psiElement);

    void removeChildAtPosition(int position);

    int getChildPosition(PsiElement element);

    int numberOfChildren();
}
