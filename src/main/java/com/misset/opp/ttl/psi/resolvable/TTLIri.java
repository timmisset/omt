package com.misset.opp.ttl.psi.resolvable;

import com.intellij.psi.PsiElement;
import org.apache.jena.rdf.model.Resource;

public interface TTLIri extends PsiElement {
    Resource getAsResource();

    String getResourceAsString();

    String getLocalName();

    String getPrefix();
}
