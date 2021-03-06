package com.misset.opp.omt.psi.resolvable;

import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTStringEntry;
import com.misset.opp.omt.psi.OMTVariableAssignment;

public interface OMTScalarValueResolvable extends OMTResolvableResource {

    /**
     * @return the OMT Query if the scalar value only has a single content item
     */
    OMTQuery getQuery();

    /**
     * @return the OMT Variable Assignment if the scalar value only has a single content item.
     * For example, variable assignments directly in the variables: block
     */
    OMTVariableAssignment getVariableAssignment();

    /**
     * @return the OMT String entry if the scalar value only has a single content item.
     */
    OMTStringEntry getStringEntry();
}
