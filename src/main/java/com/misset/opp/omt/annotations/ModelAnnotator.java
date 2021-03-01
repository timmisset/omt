package com.misset.opp.omt.annotations;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.intentions.generic.RemoveIntention;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTDefineName;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelItemLabel;
import com.misset.opp.omt.psi.OMTModelItemTypeElement;
import com.misset.opp.omt.psi.OMTScalarValue;
import org.apache.jena.rdf.model.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.misset.opp.util.UtilManager.getModelUtil;
import static com.misset.opp.util.UtilManager.getRDFModelUtil;

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
        } else if (element instanceof OMTModelItemLabel) {
            annotateUsage(element); // usage check of activities, procedures, etc
        } else if (element instanceof OMTDefineName) {
            annotateUsage(element); // usage of defined queries and commands
        } else if (element instanceof OMTScalarValue) {
            annotateScalarValue((OMTScalarValue) element);
        }
    }

    private void annotateModelItemType(OMTModelItemTypeElement modelItemTypeElement) {
        final List<String> modelRootItems = getModelUtil().getModelRootItems();
        String modelItemType = getModelUtil().getModelItemType(modelItemTypeElement);
        if (!modelRootItems.contains(modelItemType)) {
            setError(String.format("Unknown model type: %s", modelItemType));
        }
    }

    private void annotateScalarValue(OMTScalarValue scalarValue) {
        final List<Resource> resources = scalarValue.resolveToResource();
        final JsonObject jsonAttributes = getModelUtil().getJsonAtElementLevel(scalarValue);
        if (!resources.isEmpty() && jsonAttributes.has("type") && !jsonAttributes.get("type").getAsString().equals("query")) {
            List<Resource> acceptableTypes = getRDFModelUtil().getPrimitiveTypeAsResourceList(jsonAttributes.get("type").getAsString());
            final boolean validInput = getRDFModelUtil().validateType(acceptableTypes, resources);
            if (!validInput) {
                setError(String.format("Expected: %s, got: %s",
                        jsonAttributes.get("type").getAsString(),
                        resources.get(0).getLocalName()));
            }
        }
    }

    private void annotateBlockEntry(OMTGenericBlock omtBlockEntry) {
        String label = getModelUtil().getEntryBlockLabel(omtBlockEntry);
        PsiElement targetLabel = getModelUtil().getEntryBlockLabelElement(omtBlockEntry);

        JsonObject container = getModelUtil().getJsonAtElementLevel(omtBlockEntry);
        Set<String> keys = container.has(ATTRIBUTES) ? container.getAsJsonObject(ATTRIBUTES).keySet() : new JsonObject().keySet();
        String containerName = container.has(NAME) ? container.get(NAME).getAsString() : "";

        if (!keys.contains(label) && !getModelUtil().isMapNode(container)) {
            if (omtBlockEntry.getPropertyLabel().getReference() != null &&
                    omtBlockEntry.getPropertyLabel().getReference().resolve() != null) {
                // might be known references that are added as ad-hoc entries, or in some cases prefixes
                // in any-case, if it's resolvable it's fine
                return;
            }
            if (omtBlockEntry.getContainingFile() != null && ((OMTFile) omtBlockEntry.getContainingFile()).isModuleFile()) {
                return;
            }
            String errorMessage = String.format("%s is not a known attribute for %s",
                    label, containerName);

            IntentionAction remove = new RemoveIntention().getRemoveIntention(omtBlockEntry);
            setError(errorMessage, annotationBuilder -> annotationBuilder.withFix(remove).range(targetLabel));
        }
    }

    private void annotateMissingEntries(OMTBlock block) {
        List<String> entryLabels = block.getBlockEntryList().stream().map(OMTBlockEntry::getName).collect(Collectors.toList());
        final JsonObject jsonAttributes = getModelUtil().getJsonAttributes(block);
        if (jsonAttributes.has("shortcut") && entryLabels.isEmpty()) {
            // shortcut is being used, no validation yet
            return;
        }
        if (getModelUtil().isMapNode(getModelUtil().getJsonAtElementLevel(block))) {
            // Do not annotate blocks that consist of custom labelled properties, like payload, rules etc
            // The entries themselves are annotated
            return;
        }
        final JsonObject attributes = jsonAttributes.getAsJsonObject(ATTRIBUTES);
        List<String> missingElements = attributes.entrySet().stream()
                .filter(entry ->
                        entry.getValue().isJsonObject() &&
                                entry.getValue().getAsJsonObject().has("required") &&
                                entry.getValue().getAsJsonObject().get("required").getAsBoolean() &&
                                !entryLabels.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!missingElements.isEmpty()) {
            if (block.getContainingFile() != null && ((OMTFile) block.getContainingFile()).isModuleFile()) {
                return;
            }
            final String allMissingElements = String.join(", ", missingElements);
            final String entryBlockLabel = getModelUtil().getEntryBlockLabel(block);
            setError(String.format("%s is missing attribute(s): %s", entryBlockLabel, allMissingElements));
        }
    }
}
