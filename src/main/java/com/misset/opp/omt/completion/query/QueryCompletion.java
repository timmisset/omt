package com.misset.opp.omt.completion.query;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.completion.RDFCompletion;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getQueryUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;

public abstract class QueryCompletion extends RDFCompletion {

    protected static final ElementPattern<OMTQueryPath> QUERY_PATH_PATTERN =
            PlatformPatterns.psiElement(OMTQueryPath.class);

    protected static final ElementPattern<OMTEquationStatement> EQUATION_STATEMENT_PATTERN =
            PlatformPatterns.psiElement(OMTEquationStatement.class);

    protected static final ElementPattern<OMTQueryStep> FIRST_QUERY_STEP_PATTERN =
            PlatformPatterns.psiElement(OMTQueryStep.class).atStartOf(QUERY_PATH_PATTERN);

    protected static final ElementPattern<OMTQueryStep> NEXT_QUERY_STEP_PATTERN =
            PlatformPatterns.psiElement(OMTQueryStep.class)
                    .inside(QUERY_PATH_PATTERN)
                    .andNot(FIRST_QUERY_STEP_PATTERN);

    protected static final ElementPattern<PsiElement> FILTER_STEP_PATTERN =
            PlatformPatterns.psiElement().inside(OMTQueryFilter.class);

    protected static final ElementPattern<PsiElement> FIRST_FILTER_STEP_PATTERN =
            PlatformPatterns.psiElement().inside(FIRST_QUERY_STEP_PATTERN).and(
                    PlatformPatterns.psiElement().inside(FILTER_STEP_PATTERN)
            );

    protected void setResolvedElementsForQueryTraverse(PsiElement element) {
        final OMTQueryStep queryStep = PsiTreeUtil.getParentOfType(element, OMTQueryStep.class);
        if (queryStep == null) {
            return;
        }
        if (getQueryUtil().isPreviousStepAType(queryStep)) {
            setCurieSuggestion(element, RDFModelUtil.RDF_TYPE.asResource(), true, PREDICATE_REVERSE_PRIORITY);
        } else {
            List<Resource> previousStep = getQueryUtil().getPreviousStepResources(queryStep);
            getRDFModelUtil().listPredicatesForSubjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(queryStep, resource, false,
                    PREDICATE_FORWARD_PRIORITY));
            getRDFModelUtil().listPredicatesForObjectClass(previousStep).forEach((resource, relation) -> setCurieSuggestion(queryStep, resource, true,
                    PREDICATE_REVERSE_PRIORITY));
            setCurieSuggestion(element, RDFModelUtil.RDF_TYPE.asResource(), false, PREDICATE_FORWARD_PRIORITY);
        }
    }
}
