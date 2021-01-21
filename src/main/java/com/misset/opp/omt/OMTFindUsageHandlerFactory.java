package com.misset.opp.omt;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OMTFindUsageHandlerFactory extends FindUsagesHandlerFactory {
    /**
     * This method is also used by the test framework (ReferenceTest.java) to mimic the FindUsage behavior
     *
     * @param target
     * @param findUsagesOptions
     * @return
     */
    public static ReferencesSearch.SearchParameters getSearchParameters(@NotNull PsiElement target, @Nullable FindUsagesOptions findUsagesOptions) {
        return new ReferencesSearch.SearchParameters(getTargetElement(target),
                GlobalSearchScope.FilesScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(target.getProject()),
                        OMTFileType.INSTANCE
                ),
                false,
                findUsagesOptions == null
                        ? null
                        : findUsagesOptions.fastTrack);
    }

    private static PsiElement getTargetElement(PsiElement element) {
        return ReadAction.compute(() ->
                element instanceof OMTPropertyLabel && element.getParent() instanceof OMTModelItemLabel ?
                        element.getParent() :
                        element);
    }

    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return ReadAction.compute(() ->
                (psiElement instanceof OMTVariable) ||
                        (psiElement instanceof OMTPropertyLabel && psiElement.getParent() instanceof OMTModelItemLabel) ||
                        (psiElement instanceof OMTDefineName) ||
                        (psiElement instanceof OMTNamespacePrefix));
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
