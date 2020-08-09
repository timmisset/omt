package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * The named element version of the Prefix, this is used for the reference provider
 * The prefix part of the curie should be resolved to the prefix value in the prefixes block
 */
public interface OMTNamespacePrefixNamedElement extends PsiNameIdentifierOwner {
}
