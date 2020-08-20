package com.misset.opp.omt.psi.support;

import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;

public interface OMTCall extends OMTMemberNamedElement {

    boolean isCommandCall();

    boolean isOperatorCall();

    boolean canCallOperator();

    boolean canCallCommand();

    OMTSignature getSignature();
}
