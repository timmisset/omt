package com.misset.opp.omt.psi.resolvable;

import com.misset.opp.omt.psi.OMTQuery;

public interface OMTEquationStatementResolvable extends OMTQueryResolvable {

    OMTQuery getOpposite(OMTQuery query);

}
