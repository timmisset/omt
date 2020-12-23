package com.misset.opp.omt.psi.resolvable;

import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTQueryResolvable extends OMTResolvableResource {
    List<Resource> resolveToResource();

    List<Resource> filter(List<Resource> resources);

    boolean isType();

    boolean isBooleanType();

}
