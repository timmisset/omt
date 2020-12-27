package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

public interface OMTParameterWithTypeSup extends PsiElement {

    List<Resource> getType();

}
