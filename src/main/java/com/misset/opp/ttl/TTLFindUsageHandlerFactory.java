package com.misset.opp.ttl;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import com.misset.opp.ttl.psi.TTLSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TTLFindUsageHandlerFactory extends FindUsagesHandlerFactory {
    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return psiElement instanceof TTLSubject;
    }

    @Override
    @Nullable
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        return new FindUsagesHandler(element) {
        };
    }
}
