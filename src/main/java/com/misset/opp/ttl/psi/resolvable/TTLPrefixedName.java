package com.misset.opp.ttl.psi.resolvable;

import org.apache.jena.rdf.model.Resource;

public interface TTLPrefixedName {
    Resource getAsResource();

    String getPrefix();

    String getLocalName();
}
