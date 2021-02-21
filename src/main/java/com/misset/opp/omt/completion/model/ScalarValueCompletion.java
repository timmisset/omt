package com.misset.opp.omt.completion.model;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.psi.OMTScalarValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;

import static util.UtilManager.getModelUtil;
import static util.UtilManager.getProjectUtil;

public class ScalarValueCompletion extends OMTCompletion {
    public static void register(OMTCompletionContributor completionContributor) {
        // register the scalar value for model items
        // model:
        //      MijnActiviteit: !Activity
        //          reason: <caret>
        //
        // completion based on specific block entries
        final PsiElementPattern.Capture<PsiElement> propertyPattern =
                PlatformPatterns.psiElement()
                        .inside(PlatformPatterns.psiElement(OMTScalarValue.class));
        completionContributor.extend(CompletionType.BASIC, propertyPattern, new ScalarValueCompletion().getCompletionProvider());
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider() {
        return new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                final PsiElement position = parameters.getPosition();
                final String entryBlockLabel = getModelUtil().getEntryBlockLabel(position);
                if (entryBlockLabel.equals("reason")) {
                    setReasons();
                }
                complete(result);
            }
        };
    }

    private void setReasons() {
        HashMap<String, String> reasons = getProjectUtil().getReasons();
        reasons.forEach((key, value) -> addPriorityElement(
                key, ATTRIBUTES_PRIORITY, Collections.emptyList(), "", value));
    }

}
