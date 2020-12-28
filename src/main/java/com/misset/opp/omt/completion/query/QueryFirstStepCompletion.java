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
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTScalarValue;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTSignatureArgument;
import org.jetbrains.annotations.NotNull;

/**
 * The first query step has some specific completions that are not available to subsequent steps in the process:
 * variables and classes are only suggested at the start of a QueryPath.
 * Consider the query:
 * => $variable / ont:property [rdf:type == /ont:ClassA] / someProcedure('value');
 * - The entire query is considered a QueryPath
 * - The content of the filter is an EquationStatement containing a QueryPath on the left and right-side
 * - The 'value' is an OMTSignatureArgument which is also a new QueryPath (with just one step) by itself.
 * <p>
 * All of these cases have their own specific behaviors and completions. For example in the filter the predicates
 * of the step itself should also be shown. In the someProcedure signatureArgument there is type checking involved etc.
 * The pattern for the QueryFirstStepCompletion therefore excludes these positions to make sure this generic pattern
 * isn't triggered on top of the specific patterns.
 * <p>
 * Used PlatformPattern:
 * - inside(FILTER_STEP_PATTERN) => inside a FILTER at ANY level
 * - atStartOf(...) => works with a simple text position check. If the element that triggered the completion has the
 * same textOffset as the one from the pattern (for example the entire scriptContent) it returns true.
 */
public class QueryFirstStepCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern =
                PlatformPatterns.psiElement().inside(FIRST_QUERY_STEP_PATTERN)
                        .andNot(PlatformPatterns.psiElement().inside(FILTER_STEP_PATTERN))
                        .andNot(PlatformPatterns.psiElement().atStartOf(PlatformPatterns.psiElement(OMTScriptContent.class)))
                        .andNot(PlatformPatterns.psiElement().inside(PlatformPatterns.psiElement(OMTEquationStatement.class)))
                        .andNot(PlatformPatterns.psiElement().atStartOf(PlatformPatterns.psiElement(OMTSignatureArgument.class)))
                        .andNot(PlatformPatterns.psiElement().atStartOf(PlatformPatterns.psiElement(OMTScalarValue.class)));
        completionContributor.extend(CompletionType.BASIC, pattern,
                new QueryFirstStepCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                // all classes and types, which can be traversed using: /ont:ClassA / ^rdf:type ...
                setResolvedElementsForClasses(element, true);
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
