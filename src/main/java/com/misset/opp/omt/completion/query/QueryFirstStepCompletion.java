package com.misset.opp.omt.completion.query;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import org.jetbrains.annotations.NotNull;

public class QueryFirstStepCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {
        final PsiElementPattern pattern = (PsiElementPattern) PlatformPatterns.psiElement().inside(FIRST_QUERY_STEP_PATTERN).andNot(
                PlatformPatterns.psiElement().inside(FILTER_STEP_PATTERN)
        );
        completionContributor.extend(CompletionType.BASIC, pattern,
                new QueryFirstStepCompletion().getCompletionProvider());
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

                complete(result);
            }
        };
    }
}
