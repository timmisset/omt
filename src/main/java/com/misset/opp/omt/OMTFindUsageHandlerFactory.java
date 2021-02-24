package com.misset.opp.omt;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTNamespacePrefix;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTVariable;
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
        final PsiElement targetElement = getTargetElement(target);
        return new ReferencesSearch.SearchParameters(targetElement,
                localOnly(targetElement) ? getLocalSearchScope(targetElement) : getGlobalSearchScope(targetElement),
                false,
                findUsagesOptions == null
                        ? null
                        : findUsagesOptions.fastTrack);
    }

    private static SearchScope getLocalSearchScope(PsiElement target) {
        return new LocalSearchScope(ReadAction.compute(target::getContainingFile));
    }

    private static SearchScope getGlobalSearchScope(PsiElement target) {
        Project project = ReadAction.compute(target::getProject);
        return target instanceof OMTModelItemLabel ?
                GlobalSearchScope.allScope(project) :
                GlobalSearchScope.FilesScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        OMTFileType.INSTANCE
                );
    }

    private static boolean localOnly(PsiElement element) {
        return element instanceof OMTNamespacePrefix ||
                element instanceof OMTVariable;
    }

    private static PsiElement getTargetElement(PsiElement element) {
        return ReadAction.compute(() ->
                element instanceof OMTPropertyLabel ?
                        element.getParent() :
                        element);
    }

    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return ReadAction.compute(() ->
                (psiElement instanceof OMTVariable) ||
                        (psiElement instanceof OMTPropertyLabel && psiElement.getParent() instanceof OMTModelItemLabel) ||
                        (psiElement instanceof OMTPropertyLabel && psiElement.getParent() instanceof OMTNamespacePrefix) ||
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
