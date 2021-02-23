package com.misset.opp.omt.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTParameterType;
import com.misset.opp.omt.psi.OMTQueryReverseStep;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.psi.named.OMTCurie;
import com.misset.opp.ttl.psi.named.TTLWithResolvableIriNamedElement;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.misset.opp.util.UtilManager.getQueryUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;
import static com.misset.opp.util.UtilManager.getTTLUtil;

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
        final PsiElement container = PsiTreeUtil.findFirstParent(myElement,
                parent -> parent instanceof OMTQueryStep ||
                        parent instanceof OMTParameterType);
        if (container instanceof OMTQueryStep) {
            return resolveFromQuery((OMTQueryStep) container);
        }
        if (container instanceof OMTParameterType) {
            return resolveFromParameterType();
        }

        return ResolveResult.EMPTY_ARRAY;
    }

    private ResolveResult[] resolveFromQuery(OMTQueryStep queryStep) {
        List<Resource> previousStepResources =
                myElement.getParent() instanceof OMTQueryReverseStep ?
                        queryStep.resolveToResource() :                         // for reverse path, resolve to the result and then check the predicate
                        getQueryUtil().getPreviousStepResources(queryStep);     // for forward path, resolve to the previous step and then check the predicate
        previousStepResources = getRDFModelUtil().allSuperClasses(previousStepResources);
        return getResolveResults(previousStepResources);
    }

    private ResolveResult[] resolveFromParameterType() {
        return getResolveResults(Collections.emptyList());
    }

    private ResolveResult[] getResolveResults(List<Resource> subjectFilter) {
        return getTTLUtil()
                .getTTLReference(myElement, subjectFilter)
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

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return element instanceof TTLWithResolvableIriNamedElement &&
                ((TTLWithResolvableIriNamedElement) element).getResourceAsString().equals(
                        myElement.getAsResource().toString()
                );
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        // handle rename triggered from the TTL ontology
        if (myElement instanceof PsiNameIdentifierOwner) {
            return ((PsiNameIdentifierOwner) myElement).setName(newElementName);
        }
        return myElement;
    }
}
