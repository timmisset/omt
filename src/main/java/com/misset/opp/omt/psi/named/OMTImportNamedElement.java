package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

/**
 * The named element version of Imported files, this is used for the reference provider
 */
public interface OMTImportNamedElement extends PsiNameIdentifierOwner {

    @NotNull
    @Override
    String getName();

}
