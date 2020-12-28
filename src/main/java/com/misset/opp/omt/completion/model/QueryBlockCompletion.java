package com.misset.opp.omt.completion.model;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.completion.PlaceholderProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.misset.opp.omt.psi.OMTTypes.CARET;

public class QueryBlockCompletion extends OMTCompletion {
    private static final String SIMPLE_TEMPLATE = "DEFINE QUERY yourQueryName => 'Hello world';";
    private static final String PARAMETER_TEMPLATE = "DEFINE QUERY yourQueryName($param) => $param;";
    private static final String BASE_TEMPLATE = "DEFINE QUERY yourQueryName => . / pol:someProperty;";
    private static final List<String> TEMPLATES = Arrays.asList(
            SIMPLE_TEMPLATE,
            PARAMETER_TEMPLATE,
            BASE_TEMPLATE);
    private static final String DEFINE_QUERY_STATEMENT_EXPECTED = "define query statement";

    // When the completion is triggered at a new query position or at a new query block it will trigger
    // a parser error telling us that a Define Query Statement is expected at the current location
    // the existing placeholder (IntelliJRulezzzz) is than captured inside the PsiErrorElement.
    // We can make a simple pattern matchter for this accepting this condition
    // queries: |
    //  <caret>             <-- valid
    //
    // queries: |
    //  DEFINE QUERY myQuery => 'test';
    //  <caret>             <-- valid
    //
    // queries: |
    // <caret>              <-- not valid
    //
    // queries: |
    //  DEFINE QUERY myQuery => 'test';
    // <caret>              <-- not valid

    private static final PatternCondition<PsiElement> DEFINE_QUERY_STATEMENT_EXPECTED_PATTERN =
            new PatternCondition<>("Define Query Statement expected") {
                @Override
                public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                    final PsiElement parent = element.getParent();
                    return parent instanceof PsiErrorElement &&
                            (PsiTreeUtil.prevLeaf(element) == null || PsiTreeUtil.prevLeaf(element).getNode().getElementType() != CARET) &&
                            PlaceholderProvider.getExpectedTypesFromError((PsiErrorElement) parent).contains(DEFINE_QUERY_STATEMENT_EXPECTED);
                }
            };

    public static void register(OMTCompletionContributor completionContributor) {

        // template for a query block entry
        // queries: |
        //      DEFINE QUERY myQuery => ...
        final PsiElementPattern.Capture<PsiElement> propertyPattern =
                PlatformPatterns.psiElement().with(DEFINE_QUERY_STATEMENT_EXPECTED_PATTERN);
        completionContributor.extend(CompletionType.BASIC, propertyPattern, new QueryBlockCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                setQueryTemplates();
                complete(result);
            }
        };
    }

    private void setQueryTemplates() {
        TEMPLATES.forEach(
                template -> addPriorityElement(template, DEFINED_STATEMENT_PRIORITY)
        );
    }

}
