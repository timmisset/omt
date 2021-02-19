package com.misset.opp.ttl.psi;

import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TTLTokenType extends IElementType {
    public TTLTokenType(@NotNull @NonNls String debugName) {
        super(debugName, OMTLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "TTLTokenType." + super.toString();
    }
}
