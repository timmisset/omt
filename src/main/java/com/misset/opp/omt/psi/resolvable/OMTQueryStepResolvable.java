package com.misset.opp.omt.psi.resolvable;

import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTQueryStepResolvable extends OMTResolvableResource {

    List<Resource> resolveToResource(boolean filter);

    List<Resource> resolveToResource();

    boolean isType();

    boolean canLookBack();

    List<Resource> filter(List<Resource> resources);
}
