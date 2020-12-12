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

public class QueryNextStepCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {

        final PsiElementPattern.Capture<PsiElement> pattern = PlatformPatterns.psiElement().inside(NEXT_QUERY_STEP_PATTERN);
        completionContributor.extend(CompletionType.BASIC, pattern,
                new QueryNextStepCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
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
