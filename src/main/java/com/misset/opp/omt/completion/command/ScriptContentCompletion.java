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
import org.jetbrains.annotations.NotNull;

public class ScriptContentCompletion extends RDFCompletion {

    // Script content, any content on a scriptline finished with a semicolon
    // commands: |
    //   DEFINE COMMAND command => {
    //      <caret>
    //   }
    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern = PlatformPatterns.psiElement().atStartOf(
                PlatformPatterns.psiElement(OMTScriptContent.class)
        );
        completionContributor.extend(CompletionType.BASIC, pattern,
                new ScriptContentCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                PsiElement element = parameters.getPosition();
                // all commands
                setResolvedElementsForCommands(element);
                // all accessible variables
                setResolvedElementsForVariables(element);

                complete(result);
            }
        };
    }

}
