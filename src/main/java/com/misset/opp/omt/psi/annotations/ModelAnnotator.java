package com.misset.opp.omt.psi.annotations;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelItemTypeElement;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getModelUtil;

public class ModelAnnotator extends AbstractAnnotator {
    private static final String ATTRIBUTES = "attributes";
    private static final String NAME = "name";

    public ModelAnnotator(AnnotationHolder annotationHolder) {
        super(annotationHolder);
    }

    public void annotate(PsiElement element) {
        if (element instanceof OMTModelItemTypeElement) {
            annotateModelItemType((OMTModelItemTypeElement) element);
        } else if (element instanceof OMTGenericBlock) {
            annotateBlockEntry((OMTGenericBlock) element);
        } else if (element instanceof OMTBlock) {
            annotateMissingEntries((OMTBlock) element);
        }
    }

    private void annotateModelItemType(OMTModelItemTypeElement modelItemTypeElement) {
        final List<String> modelRootItems = getModelUtil().getModelRootItems();
        String modelItemType = getModelUtil().getModelItemType(modelItemTypeElement);
        if (!modelRootItems.contains(modelItemType)) {
            setError(String.format("Unknown model type: %s", modelItemType));
        }
    }

    private void annotateBlockEntry(OMTGenericBlock omtBlockEntry) {
        String label = getModelUtil().getEntryBlockLabel(omtBlockEntry);
        PsiElement targetLabel = getModelUtil().getEntryBlockLabelElement(omtBlockEntry);

        JsonObject container = getModelUtil().getJsonAtElementLevel(omtBlockEntry);
        Set<String> keys = container.has(ATTRIBUTES) ? container.getAsJsonObject(ATTRIBUTES).keySet() : new JsonObject().keySet();
        String containerName = container.has(NAME) ? container.get(NAME).getAsString() : "";

        if (!keys.contains(label) && !getModelUtil().isMapNode(container)) {
            String errorMessage = String.format("%s is not a known attribute for %s",
                    label, containerName);

            IntentionAction remove = new RemoveIntention().getRemoveIntention(omtBlockEntry);
            setError(errorMessage, annotationBuilder -> annotationBuilder.withFix(remove).range(targetLabel));
        }
    }

    private void annotateMissingEntries(OMTBlock block) {
        List<String> entryLabels = block.getBlockEntryList().stream().map(OMTBlockEntry::getName).collect(Collectors.toList());
        final JsonObject jsonAttributes = getModelUtil().getJsonAttributes(block);
        if (jsonAttributes.has("shortcut")) {
            return;
        }
        final JsonObject attributes = jsonAttributes.getAsJsonObject(ATTRIBUTES);
        List<String> missingElements = attributes.entrySet().stream()
                .filter(entry ->
                        entry.getValue().getAsJsonObject().has("required") &&
                                entry.getValue().getAsJsonObject().get("required").getAsBoolean() &&
                                !entryLabels.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!missingElements.isEmpty()) {
            final String allMissingElements = String.join(", ", missingElements);
            final String entryBlockLabel = getModelUtil().getEntryBlockLabel(block);
            setError(String.format("%s is missing attribute(s): %s", entryBlockLabel, allMissingElements));
        }
    }
}
