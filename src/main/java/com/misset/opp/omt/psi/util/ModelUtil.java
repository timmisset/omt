package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModelUtil {

    /**
     * Returns the modelItem block that this element belongs to
     * A modelItem is an element declared directly under model:
     *
     * @param element
     * @return
     */
    public static Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
        OMTModelItemBlock block = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        return block != null ? Optional.of(block) : Optional.empty();
    }

    public static String getModelItemType(PsiElement element) {
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        return modelItemBlock.map(omtModelItemBlock -> omtModelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1)).orElse(null);
    }

    public static Optional<OMTBlockEntry> getModelItemBlockEntry(PsiElement element, String propertyLabel) {
        OMTModelItemBlock modelItemBlock = element instanceof OMTModelItemBlock ? (OMTModelItemBlock) element : getModelItemBlock(element).orElse(null);
        if (modelItemBlock == null) {
            return Optional.empty();
        }

        final String finalPropertyLabel = propertyLabel.endsWith(":") ? propertyLabel : propertyLabel + ":";
        return modelItemBlock.getBlock().getBlockEntryList().stream()
                .filter(omtBlockEntry ->
                        omtBlockEntry.getPropertyLabel().getText().equals(finalPropertyLabel))
                .findFirst();
    }

    public static List<OMTBlockEntry> getConnectedEntries(PsiElement element, List<String> labels) {
        List<OMTBlockEntry> blockEntries = new ArrayList<>();
        List<OMTBlockEntry> parents = PsiTreeUtil.collectParents(element,
                OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTModelItemBlock);
        for (OMTBlockEntry parent : parents) {
            getSiblingEntryBlocks(parent).stream().filter(entry ->
                    labels.contains(getEntryBlockLabel(entry)))
                    .forEach(blockEntries::add);
        }
        return blockEntries;
    }

    private static List<OMTBlockEntry> getSiblingEntryBlocks(PsiElement element) {
        return PsiTreeUtil.getChildrenOfTypeAsList(element.getParent(), OMTBlockEntry.class);
    }

    /**
     * Returns the label of the block entry that directly contains this element
     *
     * @param element
     * @return
     */
    public static String getEntryBlockLabel(PsiElement element) {
        return getEntryBlockLabel(
                (OMTBlockEntry) PsiTreeUtil.findFirstParent(element, parent -> parent instanceof OMTBlockEntry)
        );
    }

    /**
     * Returns the label of the block entry that is part of the root level attributes of the modelitem
     *
     * @param element
     * @return
     */
    public static String getModelItemEntryLabel(PsiElement element) {
        return getEntryBlockLabel(PsiTreeUtil.getTopmostParentOfType(element, OMTBlockEntry.class));
    }

    public static String getEntryBlockLabel(OMTBlockEntry omtBlockEntry) {
        if (omtBlockEntry == null) {
            return null;
        }
        String label = getEntryBlockLabelElement(omtBlockEntry).getText();
        return label.endsWith(":") ? label.substring(0, label.length() - 1) : label;
    }

    public static PsiElement getEntryBlockLabelElement(OMTBlockEntry omtBlockEntry) {
        return omtBlockEntry.getSpecificBlock() != null ?
                omtBlockEntry.getSpecificBlock().getFirstChild().getFirstChild() :
                omtBlockEntry.getPropertyLabel();
    }

    public static JsonObject getAttributes(String memberName) {
        boolean hasMember = ProjectUtil.getParsedModel().has(memberName);
        return hasMember ? (JsonObject) ProjectUtil.getParsedModel().get(memberName) : new JsonObject();
    }

    public static void annotateModelItem(OMTModelItemTypeElement type, AnnotationHolder holder) {
        OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) type.getParent().getParent();
        OMTBlock block = modelItemBlock.getBlock();

        JsonObject jsonObject = getAttributes(type.getText().substring(1));
        if (jsonObject.keySet().isEmpty()) {
            holder.createErrorAnnotation(type, "Unknown model type: " + type.getText());
        } else {
            JsonObject attributes = jsonObject.getAsJsonObject("attributes");
            annotateModelTree(type.getText(), attributes, block.getBlockEntryList(), holder);
        }
    }

    private static void annotateModelTree(String parent, JsonObject attributes, List<OMTBlockEntry> entryList, AnnotationHolder holder) {

        Set<String> keys = attributes.keySet();
        entryList.forEach(omtBlockEntry -> {
            String label = getEntryBlockLabel(omtBlockEntry);
            if (!keys.contains(label)) {
                holder.createErrorAnnotation(getEntryBlockLabelElement(omtBlockEntry),
                        String.format("%s is not a known attribute for %s", label, parent));
            } else {
                JsonObject attributeDetails = attributes.getAsJsonObject(label);
                if (attributeDetails.has("type")) {
                    String type = attributeDetails.get("type").getAsString();
                    if (type.endsWith("Def")) {
                        type = type.substring(0, type.length() - 3);
                    }

                    JsonObject typeAttributes = getAttributes(type);
                    if (typeAttributes != null &&
                            !typeAttributes.keySet().isEmpty() &&
                            typeAttributes.getAsJsonObject("attributes") != null &&
                            !typeAttributes.getAsJsonObject("attributes").keySet().isEmpty()) {
                        if (omtBlockEntry.getSequence() != null) {
                            // process as sequence:
                            for (OMTSequenceItem sequenceItem : omtBlockEntry.getSequence().getSequenceItemList()) {
                                if (sequenceItem.getBlock() != null) {
                                    // sequence item consisting of a block (map) structure
                                    annotateModelTree(type, typeAttributes, sequenceItem.getBlock().getBlockEntryList(), holder);
                                } else {
                                    // usage of a shortcut, lowest level of the tree, process here
                                    JsonObject shortcut = typeAttributes.get("shortcut").getAsJsonObject();
                                    if (shortcut != null) {
                                        String asString = shortcut.getAsJsonObject("regEx").get("pattern").getAsString();
                                        System.out.println("asString");
                                    }
                                }
                            }
                        } else if (omtBlockEntry.getBlock() != null) {
                            if (typeAttributes.has("attributes")) {
                                // process the block:
                                annotateModelTree(type, typeAttributes.getAsJsonObject("attributes"), omtBlockEntry.getBlock().getBlockEntryList(), holder);
                            }
                            if (typeAttributes.has("mapOf")) {
                                // process all entry using the mapOf
                                String mappingType = typeAttributes.get("mapOf").getAsString();
                                if (mappingType.endsWith("Def")) {
                                    mappingType = mappingType.substring(0, mappingType.length() - 3);
                                }
                                typeAttributes = getAttributes(mappingType);
                                for (OMTBlockEntry entryListItem : omtBlockEntry.getBlock().getBlockEntryList()) {
                                    if (entryListItem.getBlock() != null) {
                                        annotateModelTree(type, typeAttributes.getAsJsonObject("attributes"), entryListItem.getBlock().getBlockEntryList(), holder);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static List<String> getLocalCommands(PsiElement element) {
        List<OMTBlockEntry> blockEntries = PsiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTFile);
        OMTModelItemBlock modelItemBlock = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        if (modelItemBlock == null) {
            return new ArrayList<>();
        }
        String modelItemLabel = modelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1);
        JsonObject member = getAttributes(modelItemLabel);

        List<String> commands = new ArrayList<>();
        while (member != null) {
            if (member.has("localCommands")) {
                JsonArray localCommands = member.getAsJsonArray("localCommands");
                localCommands.forEach(jsonElement -> commands.add(jsonElement.getAsString()));
            }
            if (member.has("attributes") && !blockEntries.isEmpty()) {
                OMTBlockEntry omtBlockEntry = blockEntries.remove(blockEntries.size() - 1);
                JsonObject attributes = (JsonObject) member.get("attributes");
                String label = getEntryBlockLabel(omtBlockEntry);
                label = label.substring(0, label.length() - 1);
                if (attributes.has(label)) {
                    member = (JsonObject) attributes.get(label);
                    if (member.has("type")) {
                        String memberType = member.get("type").getAsString();
                        if (memberType.endsWith("Def")) {
                            member = getAttributes(memberType.substring(0, memberType.length() - 3));
                        }
                    }
                } else {
                    member = null;
                }
            } else {
                member = null;
            }
        }
        return commands;
    }


    public static JsonObject getJson(PsiElement element) {
        List<OMTBlockEntry> blockEntries = PsiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent instanceof OMTModelItemBlock);
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        if (modelItemBlock.isPresent()) {
            JsonObject attributes = getAttributes(getModelItemType(element));
            while (attributes != null && attributes.has("attributes") && !blockEntries.isEmpty()) {
                OMTBlockEntry blockEntry = blockEntries.remove(blockEntries.size() - 1);
                String entryBlockLabel = getEntryBlockLabel(blockEntry);
                attributes = attributes.getAsJsonObject("attributes").getAsJsonObject(entryBlockLabel);

                if (attributes.has("type")) {
                    String type = attributes.get("type").getAsString();
                    if (type.endsWith("Def")) {
                        attributes = getAttributes(type.substring(0, type.length() - 3));
                    }
                }
                if (attributes.has("mapOf")) {
                    String type = attributes.get("mapOf").getAsString();
                    if (type.endsWith("Def")) {
                        attributes = getAttributes(type.substring(0, type.length() - 3));
                    }
                    blockEntries.remove(blockEntries.size() - 1);
                }
                attributes.addProperty("entryLabel", entryBlockLabel);
            }
            return attributes;
        }
        return new JsonObject();
    }
}
