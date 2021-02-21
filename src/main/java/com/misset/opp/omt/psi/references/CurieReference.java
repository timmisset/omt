package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.named.OMTCurie;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static util.UtilManager.getProjectUtil;
import static util.UtilManager.getQueryUtil;
import static util.UtilManager.getRDFModelUtil;

/**
 * The curie reference resolves to the declaration of the curie prefix in either the prefixes: node or
 * a defined PREFIX statement when used in a script.
 * The CurieUtil will find the declaring statement of the prefix
 */
public class CurieReference extends PsiReferenceBase<OMTCurie> implements PsiPolyVariantReference {
    public CurieReference(@NotNull OMTCurie omtCurie, TextRange textRange) {
        super(omtCurie, textRange);
    }

    // Reference to the TTL Language
    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        final OMTQueryStep queryStep = PsiTreeUtil.getParentOfType(myElement, OMTQueryStep.class);
        if (queryStep == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        List<Resource> previousStepResources =
                myElement.getParent() instanceof OMTQueryReverseStep ?
                        queryStep.resolveToResource() :                         // for reverse path, resolve to the result and then check the predicate
                        getQueryUtil().getPreviousStepResources(queryStep);     // for forward path, resolve to the previous step and then check the predicate
        previousStepResources = getRDFModelUtil().allSuperClasses(previousStepResources);
        return getProjectUtil()
                .getTTLReference(myElement, previousStepResources)
                .stream()
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

}
