package com.misset.opp.omt.psi.named;

import org.jetbrains.annotations.NotNull;

public interface OMTModelItemLabelNamedElement extends OMTMemberNamedElement {
    @NotNull
    String getModelItemType();
}
