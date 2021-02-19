package com.misset.opp.ttl.psi;

import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TTLElementType extends IElementType {
    public TTLElementType(@NotNull @NonNls String debugName) {
        super(debugName, OMTLanguage.INSTANCE);
    }
}
