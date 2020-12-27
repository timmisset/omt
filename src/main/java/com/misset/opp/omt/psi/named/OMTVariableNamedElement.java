package com.misset.opp.omt.psi.named;

import com.intellij.psi.PsiNameIdentifierOwner;
import com.misset.opp.omt.psi.OMTVariableAssignment;
import com.misset.opp.omt.psi.OMTVariableValue;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The named element version of the OMTVariable, this is used for the reference provider
 */
public interface OMTVariableNamedElement extends PsiNameIdentifierOwner {

    @NotNull
    String getName();

    boolean isDeclaredVariable();

    boolean isIgnoredVariable();

    boolean isGlobalVariable();

    List<Resource> getType();

    OMTVariableValue getValue();

    List<OMTVariableAssignment> getAssignments();

}
