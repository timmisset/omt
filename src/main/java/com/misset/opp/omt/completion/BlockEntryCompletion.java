package com.misset.opp.omt.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;

public class BlockEntryCompletion {
    public static CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                getModelUtil().getModelRootItems()
                        .forEach(label -> result.addElement(LookupElementBuilder.create(label)));
            }
        };
    }
}
