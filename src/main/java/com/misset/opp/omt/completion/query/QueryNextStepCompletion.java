package com.misset.opp.omt.completion.query;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.misset.opp.omt.psi.OMTTypes.OPERATOR;

public class QueryNextStepCompletion extends QueryCompletion {

    public static void register(OMTCompletionContributor completionContributor) {

        // special pattern when completion is hit on an reverse-caret token only:
        // ... / ^<caret>
        // this will cause a parser error for the default IntelliJ placeholder
        // replacing it with a curie-element doesn't work because the semicolon messes-up the placeholder replacement
        final PatternCondition<PsiElement> carretOnlyCondition = new PatternCondition<>("Operator placeholder in reverse step") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                return element.getNode().getElementType() == OPERATOR &&
                        element.getParent() instanceof PsiErrorElement &&
                        ((PsiErrorElement) element.getParent()).getErrorDescription().contains("<curie element>");
            }
        };

        completionContributor.extend(CompletionType.BASIC, psiElement().inside(NEXT_QUERY_STEP_PATTERN),
                new QueryNextStepCompletion().getCompletionProvider());
        completionContributor.extend(CompletionType.BASIC, psiElement().with(carretOnlyCondition),
                new QueryNextStepCompletion().getCompletionForErrorState());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();

                result = getResult(element, result);

                // all accessible queries
                setResolvedElementsForOperators(element);

                // all traverse options at this position
                setResolvedElementsForQueryTraverse(element);

                complete(result);
            }
        };
    }

    public CompletionProvider<CompletionParameters> getCompletionForErrorState() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = PsiTreeUtil.prevLeaf(parameters.getPosition());
                result = result.withPrefixMatcher("^");

                // all traverse options at this position
                setResolvedElementsForQueryTraverse(element);

                complete(result);
            }
        };
    }
}
