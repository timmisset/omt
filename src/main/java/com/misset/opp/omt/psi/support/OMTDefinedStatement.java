package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTLeading;
import com.misset.opp.omt.psi.impl.OMTDefinedStatementImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for Query and Command DEFINE statements statements
 */
public interface OMTDefinedStatement extends PsiElement {

    boolean isCommand();

    boolean isQuery();

    @NotNull
    OMTDefineName getDefineName();

    OMTLeading getLeading();

    PsiElement getDefineLabel();

    OMTDefinedStatementImpl.AvailabilityScope getScope();
}
