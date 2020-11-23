package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface OMTDefinedBlock extends PsiElement {

    @NotNull
    List<OMTDefinedStatement> getStatements();

}
