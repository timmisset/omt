package com.misset.opp.omt;

import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OMTParameterHints implements InlayParameterHintsProvider {

    @NotNull
    @Override
    public List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
         return new ArrayList<>();
    }

    @NotNull
    @Override
    public Set<String> getDefaultBlackList() {
        return Collections.emptySet();
    }
}
