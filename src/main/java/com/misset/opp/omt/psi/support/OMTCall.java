package com.misset.opp.omt.psi.support;

import com.misset.opp.omt.psi.OMTFlagSignature;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;
import com.misset.opp.omt.psi.resolvable.OMTResolvableResource;

public interface OMTCall extends OMTMemberNamedElement, OMTResolvableResource {

    boolean isCommandCall();

    boolean isOperatorCall();

    boolean canCallOperator();

    boolean canCallCommand();

    OMTSignature getSignature();

    OMTFlagSignature getFlagSignature();
}
