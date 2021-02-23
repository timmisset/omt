package com.misset.opp.ttl.psi.named;

import com.intellij.psi.PsiNameIdentifierOwner;

// interface for any TTL element that has a resolvable iri
public interface TTLWithResolvableIriNamedElement extends PsiNameIdentifierOwner {

    String getResourceAsString();

}
