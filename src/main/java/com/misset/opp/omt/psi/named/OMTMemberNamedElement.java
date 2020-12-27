package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

/**
 * The named element version of Members, which are operators, commands or the calls to them, this is used for the reference provider
 */
public interface OMTMemberNamedElement extends PsiNameIdentifierOwner {

    @NotNull
    @Override
    PsiElement getNameIdentifier();

    @NotNull
    NamedMemberType getType();

    @NotNull
    @Override
    String getName();
}
