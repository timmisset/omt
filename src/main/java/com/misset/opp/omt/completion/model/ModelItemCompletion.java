package com.misset.opp.omt.completion.model;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.psi.OMTTypes;
import org.jetbrains.annotations.NotNull;

import static com.misset.opp.util.UtilManager.getModelUtil;

/**
 * Adds completion types based on the JSON model
 * To allow for more flexibility in the parser, the ModelItemType uses RegEx !+{Name} which means
 * it will accept !!Activity in the grammar. This will be annotated with an error.
 */
public class ModelItemCompletion extends OMTCompletion {
    public static void register(OMTCompletionContributor completionContributor) {
        completionContributor.extend(CompletionType.BASIC, PlatformPatterns.psiElement(OMTTypes.MODEL_ITEM_TYPE),
                new ModelItemCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                getModelUtil().getModelRootItems()
                        .stream().map(s -> "!" + s)
                        .forEach(label -> addPriorityElement(label, MODEL_ITEM_TYPE_PRIORITY));
                if (!result.getPrefixMatcher().getPrefix().isEmpty()) {
                    // the default prefix matcher will trim the flag symbol from the prefix
                    // this would result to the insert results to become: !!Activity
                    result = result.withPrefixMatcher("!" + result.getPrefixMatcher().getPrefix());
                }
                complete(result);
            }
        };
    }
}
