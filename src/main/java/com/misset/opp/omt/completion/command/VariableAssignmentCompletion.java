package com.misset.opp.omt.completion.command;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.completion.RDFCompletion;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTScriptContent;
import com.misset.opp.omt.psi.OMTSequence;
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

        // completion for a variable assignment within a script
        completionContributor.extend(CompletionType.BASIC, patternInScript,
                new VariableAssignmentCompletion().getCompletionProvider());

    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                if (allowCommandsAtThisPosition(element)) {
                    // all accessible commands
                    setResolvedElementsForDefinedCommands(element);
                    // all builtin commands
                    setResolvedElementsForBuiltinCommands();
                }
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

    /**
     * ScriptContent can also be part of the tree for sequence items that contain variable assignments for example
     * in which case it is not permitted to allow command calls:
     *
     * @param element
     * @return
     */
    private boolean allowCommandsAtThisPosition(PsiElement element) {
        // checks if the first container is a block entry, not a sequence
        return PsiTreeUtil.findFirstParent(element, parent ->
                parent instanceof OMTBlockEntry ||
                        parent instanceof OMTSequence) instanceof OMTBlockEntry;
    }

}
