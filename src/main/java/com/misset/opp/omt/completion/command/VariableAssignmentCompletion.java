package com.misset.opp.omt.completion.command;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.completion.RDFCompletion;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTVariableValue;
import org.jetbrains.annotations.NotNull;

public class VariableAssignmentCompletion extends RDFCompletion {

    // VariableAssignment
    // commands: |
    //   DEFINE COMMAND command => {
    //      VAR $variable = <caret>;        <-- either a variable declare with value
    //      $variable = <caret>;            <-- or a re-assignment
    //   }
    //
    // Assigning a variable with a value is very flexible. There should be no type-checks
    // since OMT will allow for type re-assignments for variables
    // therefore, all commands, operators, queries, variables etc are all applicable
    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> patternInScript = PlatformPatterns.psiElement()
                .inside(OMTScriptContent.class)
                .atStartOf(PlatformPatterns.psiElement(OMTVariableValue.class));

        final ElementPattern<PsiElement> patternInModel = PlatformPatterns.psiElement()
                .atStartOf(PlatformPatterns.psiElement(OMTVariableValue.class))
                .andNot(PlatformPatterns.psiElement().inside(OMTScriptContent.class));

        // completion for a variable assignment within a script
        completionContributor.extend(CompletionType.BASIC, patternInScript,
                new VariableAssignmentCompletion().getCompletionProvider(true));

        // completion for a variable assignment within a script
        completionContributor.extend(CompletionType.BASIC, patternInModel,
                new VariableAssignmentCompletion().getCompletionProvider(false));
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider(boolean includeCommandCalls) {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                if (includeCommandCalls) {
                    // all accessible commands
                    setResolvedElementsForDefinedCommands(element);
                    // all builtin commands
                    setResolvedElementsForBuiltinCommands();
                    // all accessible commands
                    setResolvedElementsForDefinedQueries(element);
                }
                // all builtin commands
                setResolvedElementsForBuiltinOperators();
                // all accessible variables
                setResolvedElementsForVariables(element);

                complete(result);
            }
        };
    }

}
