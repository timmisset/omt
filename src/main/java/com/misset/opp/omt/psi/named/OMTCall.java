package com.misset.opp.omt.psi.named;

import com.misset.opp.omt.psi.OMTCallName;
import com.misset.opp.omt.psi.OMTFlagSignature;
import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.resolvable.OMTResolvableResource;
import com.misset.opp.omt.psi.support.OMTCallable;

/**
 * Interface for command and query call
 * Are named elements and can be resolved to a resource by resolving the response of the call
 */
public interface OMTCall extends OMTMemberNamedElement, OMTResolvableResource {

    boolean isCommandCall();

    boolean isOperatorCall();

    boolean canCallOperator();

    boolean canCallCommand();

    OMTSignature getSignature();

    OMTFlagSignature getFlagSignature();

    OMTCallable getCallable();

    OMTCallName getCallName();
}
