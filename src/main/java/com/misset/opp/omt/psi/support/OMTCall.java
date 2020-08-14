package com.misset.opp.omt.psi.support;

import com.misset.opp.omt.psi.named.OMTMemberNamedElement;

public interface OMTCall extends OMTMemberNamedElement {

    boolean canCallOperator();

    boolean canCallCommand();

}
