package com.misset.opp.omt.psi.resolvable;

import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTQueryResolvable extends OMTResolvableResource {
    List<Resource> resolveToResource(boolean lookBack);

    List<Resource> filter(List<Resource> resources);

    boolean isBooleanType();

}
