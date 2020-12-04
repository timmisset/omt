package com.misset.opp.omt.psi.resolvable;

import com.intellij.psi.PsiElement;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTResolvableResource extends PsiElement {
    List<Resource> resolveToResource();
}
