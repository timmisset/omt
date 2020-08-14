package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTTrailing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OMTDefinedBlock extends PsiElement {

    @Nullable
    OMTTrailing getTrailing();

    @NotNull
    List<OMTDefinedStatement> getStatements();

}
