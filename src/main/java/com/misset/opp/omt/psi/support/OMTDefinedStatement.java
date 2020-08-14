package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTDefineParam;
import com.misset.opp.omt.psi.OMTLeading;
import com.misset.opp.omt.psi.OMTTrailing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OMTDefinedStatement extends PsiElement {

    @NotNull
    OMTDefineName getDefineName();

    @Nullable
    OMTDefineParam getDefineParam();

    @Nullable
    OMTLeading getLeading();

    @NotNull
    OMTTrailing getTrailing();

    boolean isCommand();

    boolean isQuery();

}
