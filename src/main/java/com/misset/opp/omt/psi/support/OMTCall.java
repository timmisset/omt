package com.misset.opp.omt.psi.support;

import com.misset.opp.omt.psi.OMTSignature;
import com.misset.opp.omt.psi.named.OMTMemberNamedElement;
import org.jetbrains.annotations.Nullable;

public interface OMTCall extends OMTMemberNamedElement {

    boolean canCallOperator();

    boolean canCallCommand();

    @Nullable
    OMTSignature getSignature();
}
