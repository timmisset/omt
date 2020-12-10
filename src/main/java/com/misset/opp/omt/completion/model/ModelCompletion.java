package com.misset.opp.omt.completion.model;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.misset.opp.omt.completion.OMTCompletion;
import com.misset.opp.omt.completion.OMTCompletionContributor;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;

public class ModelCompletion extends OMTCompletion {
    public static void register(OMTCompletionContributor completionContributor) {
        // register the regular model property key
        // model:
        //      MijnActiviteit: !Activity
        //          <caret>
        //
        // completion for JSON model attributes such as titel, params, variables etc for Activity
        final PsiElementPattern.Capture<PsiElement> propertyPattern =
                PlatformPatterns.psiElement(OMTTypes.PROPERTY)
                        .inside(PlatformPatterns.psiElement(OMTBlockEntry.class))
                        .andNot(PlatformPatterns.psiElement().inside(OMTSequenceItem.class));
        completionContributor.extend(CompletionType.BASIC, propertyPattern, new ModelCompletion().getCompletionProvider(true));

        // register the sequence destructure
        // model:
        //      MijnActiviteit: !Activiteit
        //          variables:
        //              -   <caret>
        //
        // completion for destructure attributes such as name, value, onChange etc for VariableType
        final PsiElementPattern.Capture<PsiElement> sequencePattern =
                PlatformPatterns.psiElement().andOr(
                        PlatformPatterns.psiElement(OMTTypes.OPERATOR),             // capture placeholder as operator
                        PlatformPatterns.psiElement(OMTTypes.PROPERTY)              // or the PROVIDE_MODEL_ENTRY (MODEL: ENTRY)
                ).inside(
                        PlatformPatterns.psiElement(OMTSequenceItem.class)
                );
        completionContributor.extend(CompletionType.BASIC, sequencePattern, new ModelCompletion().getCompletionProvider(false));
    }

    public CompletionProvider<CompletionParameters> getCompletionProvider(boolean fromBlockParent) {
        return new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                setAttributeSuggestions(parameters.getPosition(), fromBlockParent);
                complete(result);
            }
        };
    }

    // add completion in the OMT model based on the Json attributes
    private void setAttributeSuggestions(PsiElement element, boolean fromBlockParent) {
        // the element is the placeholder that will be replaced by the completion
        // therefore, retrieve the json of the elements parent to determine the applicable fields
        JsonObject json = getModelUtil().getJson(fromBlockParent ? getBlockParent(element) : element);
        List<String> existingSiblingEntryLabels = getExistingSiblingEntryLabels(element);
        if (json != null && json.has(ATTRIBUTES)) {

            json.getAsJsonObject(ATTRIBUTES).keySet()
                    .stream()
                    .filter(key -> !existingSiblingEntryLabels.contains(key))
                    .forEach(key -> addPriorityElement(String.format("%s:", key).trim(), ATTRIBUTES_PRIORITY)
                    );
        }
    }

    // used to fetch the entries already available for the item that is targeted by the completion
    private List<String> getExistingSiblingEntryLabels(PsiElement element) {
        OMTBlock container = getBlockParent(element);
        return container != null ?
                container.getBlockEntryList().stream().map(OMTBlockEntry::getName).collect(Collectors.toList()) :
                new ArrayList<>();
    }

    private OMTBlock getBlockParent(PsiElement element) {
        return (OMTBlock) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTBlock);
    }
}
