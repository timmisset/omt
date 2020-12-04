package com.misset.opp.omt.psi.resolvable;

import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTQueryStepResolvable extends OMTResolvableResource {

    List<Resource> resolveToResource(boolean lookBack, boolean filter);

    List<Resource> resolveToResource(boolean lookBack);

    List<Resource> resolveToResource();

    List<Resource> filter(List<Resource> resources);
}
