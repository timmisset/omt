package com.misset.opp.ttl;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.ttl.psi.TTLSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TTLFindUsageHandlerFactory extends FindUsagesHandlerFactory {
    public static ReferencesSearch.SearchParameters getSearchParameters(@NotNull PsiElement target, @Nullable FindUsagesOptions findUsagesOptions) {
        return new ReferencesSearch.SearchParameters(target,
                GlobalSearchScope.allScope(target.getProject()),
                false,
                findUsagesOptions == null
                        ? null
                        : findUsagesOptions.fastTrack);
    }

    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return psiElement instanceof TTLSubject;
    }

    @Override
    @Nullable
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        return new FindUsagesHandler(element) {
            @Override
            @NotNull
            protected ReferencesSearch.SearchParameters createSearchParameters(@NotNull PsiElement target, @NotNull SearchScope searchScope, @Nullable FindUsagesOptions findUsagesOptions) {
                return getSearchParameters(target, findUsagesOptions);
            }
        };
    }
}
