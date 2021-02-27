package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTPrefix;
import com.misset.opp.omt.psi.resolvable.OMTResolvableResource;
import com.misset.opp.omt.psi.support.OMTNamedElement;
import org.apache.jena.rdf.model.Resource;

public interface OMTCurie extends OMTResolvableResource, OMTNamedElement {

    PsiElement getPrefix();

    String getPrefixName();

    boolean isDefinedByPrefix(OMTPrefix prefix);

    Resource getAsResource();

    String getIri();

    boolean isIri();

}
