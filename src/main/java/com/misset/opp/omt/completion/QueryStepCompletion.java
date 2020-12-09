package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class QueryStepCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {
        completionContributor.extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(firstQueryStepPattern),
                new QueryStepCompletion().getFirstStepCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getFirstStepCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                // all classes and types, which can be traversed using: /ont:ClassA / ^rdf:type ...
                setResolvedElementsForClasses(element);
                // all known variables at this point
                setResolvedElementsForVariables(element);
                complete(result);
            }
        };
    }

}
