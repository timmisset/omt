package com.misset.opp.omt.psi;

import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.*;

public class OMTTokenType extends IElementType {
    public OMTTokenType(@NotNull @NonNls String debugName) {
        super(debugName, OMTLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "OMTTokenType." + super.toString();
    }
}
