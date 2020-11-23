package com.misset.opp.omt.psi;

import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.OMTLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class OMTElementType extends IElementType {
    public OMTElementType( @NotNull @NonNls String debugName) {
        super(debugName, OMTLanguage.INSTANCE);
    }
}
