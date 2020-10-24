package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import org.jetbrains.annotations.NotNull;

public interface OMTDefinedStatement extends PsiElement {

    boolean isCommand();

    boolean isQuery();

    @NotNull
    OMTDefineName getDefineName();

}
