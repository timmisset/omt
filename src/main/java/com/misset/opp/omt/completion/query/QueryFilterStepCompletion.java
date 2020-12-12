package com.misset.opp.omt.completion.query;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import org.jetbrains.annotations.NotNull;

/**
 * The QueryFilterStepCompletion only triggers on the first step in the filter
 * The QueryFirstStepCompletion pattern ignores the first step if it's inside a filter
 * The subsequent steps are handled by the QueryNextStepCompletion
 * <p>
 * Outside of filters a traverse options (predicates based on the current type/class) are only
 * available in subsequent steps in the QueryPath. For the filter this is different because
 * the context is known (the type(s) being filtered)
 */
public class QueryFilterStepCompletion extends QueryCompletion {
    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern =
                PlatformPatterns.psiElement().inside(FIRST_FILTER_STEP_PATTERN)
                        .andNot(PlatformPatterns.psiElement().inside(NEXT_QUERY_STEP_PATTERN))
                        .andNot(PlatformPatterns.psiElement().inside(EQUATION_STATEMENT_PATTERN));
        completionContributor.extend(CompletionType.BASIC, pattern,
                new QueryFilterStepCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                // all classes and types, which can be traversed using: /ont:ClassA / ^rdf:type ...
                setResolvedElementsForClasses(element);
                // all known variables at this point
                setResolvedElementsForVariables(element);
                // all accessible queries
                setResolvedElementsForDefinedQueries(element);
                // all builtin operators
                setResolvedElementsForBuiltinOperators();
                // all traverse options at this position
                setResolvedElementsForQueryTraverse(element);

                complete(result);
            }
        };
    }
}
