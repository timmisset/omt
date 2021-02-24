package com.misset.opp.ttl;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.ttl.psi.TTLObject;
import com.misset.opp.ttl.psi.TTLPredicateObject;
import com.misset.opp.ttl.psi.TTLSubject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.misset.opp.omt.util.RDFModelUtil.SHACL_PATH;

public class TTLFindUsageHandlerFactory extends FindUsagesHandlerFactory {
    @Override
    public boolean canFindUsages(@NotNull PsiElement psiElement) {
        return psiElement instanceof TTLSubject ||
                shPathPredicate(psiElement);
    }

    private boolean shPathPredicate(PsiElement psiElement) {
        if (!(psiElement instanceof TTLObject)) {
            return false;
        }
        final TTLPredicateObject predicateObject = PsiTreeUtil.getParentOfType(psiElement, TTLPredicateObject.class);
        if (predicateObject == null ||
                predicateObject.getVerb().getPredicate() == null
        ) {
            return false;
        }
        return predicateObject.getVerb().getPredicate().getIri().getResourceAsString().equals(SHACL_PATH.getURI());
    }

    @Override
    @Nullable
    public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
        return new FindUsagesHandler(element) {
        };
    }
}
