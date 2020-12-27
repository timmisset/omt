package com.misset.opp.omt.psi.resolvable;

import com.misset.opp.omt.psi.named.OMTCall;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTCallResolvable extends OMTCall {

    List<Resource> resolveToResource();

    List<Resource> getFirstArgumentType();

}
