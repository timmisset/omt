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
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();

                setResolvedElementsForComparableTypes(element);
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
