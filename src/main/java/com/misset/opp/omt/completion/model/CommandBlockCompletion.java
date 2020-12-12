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
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.completion.PlaceholderProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * CommandBlockCompletion will show templates that can be used for completions
 */
public class CommandBlockCompletion extends OMTCompletion {
    private static final String SIMPLE_TEMPLATE = "DEFINE COMMAND yourCommandName => { RETURN true; }";
    private static final String PARAMETER_TEMPLATE = "DEFINE COMMAND yourCommandName($param) => { RETURN $param; }";
    private static final List<String> TEMPLATES = Arrays.asList(
            SIMPLE_TEMPLATE,
            PARAMETER_TEMPLATE);
    private static final String DEFINE_COMMAND_STATEMENT_EXPECTED = "define command statement";

    // See QueryBlockCompletion for details
    private static final PatternCondition<PsiElement> DEFINE_COMMAND_STATEMENT_EXPECTED_PATTERN =
            new PatternCondition<PsiElement>("Define Command Statement expected") {
                @Override
                public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                    final PsiElement parent = element.getParent();
                    return parent instanceof PsiErrorElement &&
                            PlaceholderProvider.getExpectedTypesFromError((PsiErrorElement) parent).contains(DEFINE_COMMAND_STATEMENT_EXPECTED);
                }
            };

    public static void register(OMTCompletionContributor completionContributor) {

        // template for a command block entry
        // queries: |
        //      DEFINE COMMAND myCommand => ...
        final PsiElementPattern.Capture<PsiElement> propertyPattern =
                PlatformPatterns.psiElement().with(DEFINE_COMMAND_STATEMENT_EXPECTED_PATTERN);
        completionContributor.extend(CompletionType.BASIC, propertyPattern, new CommandBlockCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                setCommandTemplates();
                complete(result);
            }
        };
    }

    private void setCommandTemplates() {
        TEMPLATES.forEach(
                template -> addPriorityElement(template, DEFINED_STATEMENT_PRIORITY)
        );
    }

}
