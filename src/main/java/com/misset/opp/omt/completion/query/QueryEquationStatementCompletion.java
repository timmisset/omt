package com.misset.opp.omt.completion.query;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.support.OMTCallable;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

import static com.misset.opp.omt.util.UtilManager.getRDFModelUtil;

/**
 * The EquationStatement completion will do a type-check on the other side of the equation
 * When this can be resolved to a specific type or class it will show comparable options
 * This extends to showing potential implementations for a class
 */
public class QueryEquationStatementCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern = PlatformPatterns.psiElement().inside(EQUATION_STATEMENT_PATTERN);
        completionContributor.extend(CompletionType.BASIC, pattern,
                new QueryEquationStatementCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();

                // get the query of <caret> side of the equation
                final OMTQuery query = (OMTQuery) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTQuery && parent.getParent() instanceof OMTEquationStatement);
                if (query == null) {
                    return;
                }

                final OMTEquationStatement equationStatement = (OMTEquationStatement) query.getParent();
                final OMTQuery opposite = equationStatement.getOpposite(query);
                final List<Resource> resources = opposite.resolveToResource();

                final RDFModelUtil rdfModelUtil = getRDFModelUtil();
                if (opposite.isType()) {
                    if (!resources.isEmpty() && getRDFModelUtil().isTypePredicate(resources.get(0))) {
                        // rdf:type was used on an unknown query step, show all possible classes:
                        setResolvedElementsForClasses(element, true);
                    } else {
                        // the previous step could be resolved
                        // only show the input type and all possible implementation classes
                        resources.forEach(
                                resource -> setCurieSuggestion(element, resource, false, CLASSES_PRIORITY, true)
                        );
                        setResolvedElementsForComparableTypes(element, resources);
                    }

                }

                final Predicate<OMTCallable> acceptsInput = callable -> rdfModelUtil.validateType(resources, callable.getReturnType());

                // all known variables at this point
                setResolvedElementsForVariables(element);
                // all accessible queries
                setResolvedElementsForDefinedQueries(element, acceptsInput);
                // all builtin operators
                setResolvedElementsForBuiltinOperators(acceptsInput);

                complete(result);
            }
        };
    }
}
