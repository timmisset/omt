package com.misset.opp.omt;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OMTFindUsageHandlerFactory extends FindUsagesHandlerFactory {
    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return (psiElement instanceof OMTVariable) ||
                (psiElement instanceof OMTModelItemLabel) ||
                (psiElement instanceof OMTDefineName) ||
                (psiElement instanceof OMTNamespacePrefix);
    }

    @Override
    public @Nullable FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        return new FindUsagesHandler(element) {
            @Override
            protected ReferencesSearch.@NotNull SearchParameters createSearchParameters(@NotNull PsiElement target, @NotNull SearchScope searchScope, @Nullable FindUsagesOptions findUsagesOptions) {
                return new ReferencesSearch.SearchParameters(target,
                        GlobalSearchScope.FilesScope.getScopeRestrictedByFileTypes(
                                GlobalSearchScope.allScope(target.getProject()),
                                OMTFileType.INSTANCE
                        ),
                        false,
                        findUsagesOptions == null
                                ? null
                                : findUsagesOptions.fastTrack);
            }
        };
    }
}
