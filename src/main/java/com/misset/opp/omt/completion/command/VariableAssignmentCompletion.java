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
        final ElementPattern<PsiElement> pattern = PlatformPatterns.psiElement()
                .atStartOf(PlatformPatterns.psiElement(OMTVariableValue.class));
        completionContributor.extend(CompletionType.BASIC, pattern,
                new VariableAssignmentCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                // all accessible commands
                setResolvedElementsForDefinedCommands(element);
                // all builtin commands
                setResolvedElementsForBuiltinCommands();
                // all accessible commands
                setResolvedElementsForDefinedQueries(element);
                // all builtin commands
                setResolvedElementsForBuiltinOperators();
                // all accessible variables
                setResolvedElementsForVariables(element);

                complete(result);
            }
        };
    }

}
