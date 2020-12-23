package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.psi.OMTParameterType;
import org.jetbrains.annotations.NotNull;

public class ParameterTypeCompletion extends RDFCompletion {

    // parameter type completion for parameter definitions with a type
    // model:
    //   MijnActiviteit: !Activity
    //     params:
    //     - $param (<caret>)
    public static void register(OMTCompletionContributor completionContributor) {
        final ElementPattern<PsiElement> pattern = PlatformPatterns.psiElement().inside(OMTParameterType.class);
        completionContributor.extend(CompletionType.BASIC, pattern,
                new ParameterTypeCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                // The first step of the query will suggest starting points of the query
                PsiElement element = parameters.getPosition();
                // all classes and types, which can be traversed using: /ont:ClassA / ^rdf:type ...
                setResolvedElementsForClasses(element, false);

                complete(result);
            }
        };
    }

}
