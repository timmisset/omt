package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.resolvable.OMTResolvableResource;
import org.apache.jena.rdf.model.Resource;

public interface OMTCurie extends OMTResolvableResource {

    PsiElement getPrefix();

    String getPrefixName();

    boolean isDefinedByPrefix(OMTPrefix prefix);

    Resource getAsResource();

}
