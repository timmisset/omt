package com.misset.opp.omt.psi.support;

import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTVariable;

/**
 * A parameter is a variable kind but with additional info in relationship to it's owner:
 * required, default value etc
 */
public interface OMTParameter {
    OMTVariable getVariable();

    boolean isRequired();

    Object getDefaultValue();

    OMTParameterType getType();

    String getName();
}
