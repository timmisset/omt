package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.model.ModelIntention;
import com.misset.opp.omt.wrappers.PsiTreeUtil;
import com.sun.istack.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public class ModelUtil {

    private static String ATTRIBUTESKEY = "attributes";
    private static String MAPOFKEY = "mapOf";

    public static final ModelUtil SINGLETON = new ModelUtil();

    private final PsiTreeUtil psiTreeUtil;
    private final ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    ;

    public ModelUtil() {
        psiTreeUtil = PsiTreeUtil.SINGLETON;
    }

    public ModelUtil(PsiTreeUtil psiTreeUtil, ProjectUtil projectUtil) {
        this.psiTreeUtil = psiTreeUtil;
    }

    public Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
        if (element instanceof OMTModelItemBlock) {
            return Optional.of((OMTModelItemBlock) element);
        }
        OMTModelItemBlock block = psiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        return block != null ? Optional.of(block) : Optional.empty();
    }

    @Nullable
    public String getModelItemType(PsiElement element) {
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        return modelItemBlock.map(omtModelItemBlock -> omtModelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1)).orElse(null);
    }

    public Optional<OMTBlockEntry> getModelItemBlockEntry(PsiElement element, String propertyLabel) {
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        if (!modelItemBlock.isPresent()) {
            return Optional.empty();
        }

        OMTBlock block = modelItemBlock.get().getBlock();
        if (block == null) {
            return Optional.empty();
        }

        return block.getBlockEntryList().stream()
                .filter(omtBlockEntry ->
                        isSameProperty(propertyLabel, getPropertyLabelText(omtBlockEntry)))
                .findFirst();
    }

    private boolean isSameProperty(String propertyLabelA, String propertyLabelB) {
        String stringA = propertyLabelA.endsWith(":") ? propertyLabelA.substring(0, propertyLabelA.length() - 1) : propertyLabelA;
        String stringB = propertyLabelB.endsWith(":") ? propertyLabelB.substring(0, propertyLabelB.length() - 1) : propertyLabelB;
        return stringA.equals(stringB);
    }

    private String getPropertyLabelText(OMTBlockEntry omtBlockEntry) {
        if (omtBlockEntry == null || omtBlockEntry.getPropertyLabel() == null) {
            return "";
        }
        return omtBlockEntry.getPropertyLabel().getText();
    }

    public List<OMTBlockEntry> getConnectedEntries(PsiElement element, List<String> labels) {
        List<OMTBlockEntry> blockEntries = new ArrayList<>();
        List<OMTBlockEntry> parents = psiTreeUtil.collectParents(element,
                OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTModelItemBlock);
        for (OMTBlockEntry parent : parents) {
            getSiblingEntryBlocks(parent).stream().filter(entry ->
                    labels.contains(getEntryBlockLabel(entry)))
                    .forEach(blockEntries::add);
        }
        return blockEntries;
    }

    private List<OMTBlockEntry> getSiblingEntryBlocks(PsiElement element) {
        return psiTreeUtil.getChildrenOfTypeAsList(element.getParent(), OMTBlockEntry.class);
    }

    /**
     * Returns the label of the block entry that directly contains this element
     * model:
     * MyActivity: !Activity
     * payload:
     * myPayloadParameter: myValue
     *
     * @param element - myValue
     * @return myPayloadParameter
     */
    public String getEntryBlockLabel(PsiElement element) {
        PsiElement labelElement = getEntryBlockLabelElement(element);
        String label = labelElement.getText();
        return label.endsWith(":") ? label.substring(0, label.length() - 1) : label;
    }

    private PsiElement getEntryBlockLabelElement(OMTBlockEntry omtBlockEntry) {
        return omtBlockEntry.getSpecificBlock() != null ?
                omtBlockEntry.getSpecificBlock().getFirstChild().getFirstChild() :
                omtBlockEntry.getPropertyLabel();
    }

    private OMTModelItemLabel getEntryBlockLabelElement(OMTModelItemBlock omtModelItemBlock) {
        return omtModelItemBlock.getModelItemLabel();
    }

    private PsiElement getEntryBlockLabelElement(PsiElement element) {
        if (element instanceof OMTModelItemBlock) {
            return getEntryBlockLabelElement((OMTModelItemBlock) element).getPropertyLabel();
        }
        if (element instanceof OMTBlockEntry) {
            return getEntryBlockLabelElement((OMTBlockEntry) element);
        }
        if (element instanceof OMTSequenceItem) {
            return element;
        }
        if (element instanceof OMTBlock) {
            return getEntryBlockLabelElement(psiTreeUtil.findFirstParent(element,
                    parent -> parent instanceof OMTBlockEntry
                            ||
                            parent instanceof OMTModelItemBlock
            ));

        }
        return null;
    }

    /**
     * Returns the label of the block entry that is part of the root level attributes of the modelitem (MyActivity)
     * model:
     * MyActivity: !Activity
     * payload:
     * myPayloadParameter: myValue
     *
     * @param element - myValue
     * @return payload
     */
    public String getModelItemEntryLabel(PsiElement element) {
        List<OMTBlockEntry> blockEntries = psiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent instanceof OMTModelBlock);
        OMTBlockEntry omtBlockEntry = blockEntries.get(blockEntries.size() - 1);

        return getEntryBlockLabel(omtBlockEntry);
    }

    private JsonObject getAttributes(String memberName) {
        JsonObject parsedModel = projectUtil.getParsedModel();
        boolean hasMember = parsedModel.has(memberName);
        return hasMember ? (JsonObject) parsedModel.get(memberName) : new JsonObject();
    }

    public void annotateModelItem(OMTModelItemBlock modelItemBlock, AnnotationHolder holder) {
        OMTBlock block = modelItemBlock.getBlock();
        if (block == null) {
            return;
        }
        OMTModelItemTypeElement type = modelItemBlock.getModelItemLabel().getModelItemTypeElement();
        String modelItemType = getModelItemType(type);
        JsonObject jsonObject = getAttributes(modelItemType);
        if (jsonObject.keySet().isEmpty()) {
            AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("Unknown model type: %s", type.getText()));
            annotationBuilder.range(type);
            annotationBuilder.create();
        } else {
            JsonObject attributes = jsonObject.getAsJsonObject(ATTRIBUTESKEY);
            annotateModelTree(attributes, block, block.getBlockEntryList(), holder);
        }
    }

    private void annotateMissingEntries(JsonObject attributes, OMTBlock block, AnnotationHolder holder) {
        List<String> entryLabels = block.getBlockEntryList().stream().map(this::getEntryBlockLabel).collect(Collectors.toList());
        List<String> missingElements = attributes.entrySet().stream()
                .filter(entry ->
                        entry.getValue().getAsJsonObject().has("required") &&
                                entry.getValue().getAsJsonObject().get("required").getAsBoolean() &&
                                !entryLabels.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!missingElements.isEmpty()) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("%s is missing attribute(s): \n%s",
                            getEntryBlockLabel(block),
                            String.join(", ", missingElements)))
                    .range(getEntryBlockLabelElement(block))
                    .create();
        }
    }

    private void annotateSequenceItem(OMTBlockEntry omtBlockEntry, JsonObject attributes, AnnotationHolder holder) {
        // process as sequence:
        for (OMTSequenceItem sequenceItem : omtBlockEntry.getSequence().getSequenceItemList()) {
            if (sequenceItem.getBlock() != null) {
                // sequence item consisting of a block (map) structure
                annotateModelTree(attributes, sequenceItem.getBlock(), sequenceItem.getBlock().getBlockEntryList(), holder);
            } else {
                if (attributes.has("shortcut")) {
                    // usage of a shortcut, lowest level of the tree, process here
                    JsonObject shortcut = attributes.get("shortcut").getAsJsonObject();
                    if (shortcut != null) {
                        String asString = shortcut.getAsJsonObject("regEx").get("pattern").getAsString();
                    }
                }
            }
        }
    }

    private void annotateBlockItem(OMTBlockEntry omtBlockEntry, JsonObject attributes, AnnotationHolder holder) {
        if (attributes.has(ATTRIBUTESKEY)) {
            // process the block:
            annotateModelTree(attributes.getAsJsonObject(ATTRIBUTESKEY), omtBlockEntry.getBlock(), omtBlockEntry.getBlock().getBlockEntryList(), holder);
        }
        if (attributes.has(MAPOFKEY)) {
            // process all entry using the mapOf
            String mappingType = attributes.get(MAPOFKEY).getAsString();
            if (mappingType.endsWith("Def")) {
                mappingType = mappingType.substring(0, mappingType.length() - 3);
            }
            attributes = getAttributes(mappingType);
            for (OMTBlockEntry entryListItem : omtBlockEntry.getBlock().getBlockEntryList()) {
                if (entryListItem.getBlock() != null) {
                    annotateModelTree(attributes.getAsJsonObject(ATTRIBUTESKEY), omtBlockEntry.getBlock(), entryListItem.getBlock().getBlockEntryList(), holder);
                }
            }
        }
    }

    private String getEntryType(JsonObject entry) {
        // get the type definition for the block entry
        String type = entry.get("type").getAsString();
        if (type.endsWith("Def")) {
            type = type.substring(0, type.length() - 3);
        }
        return type;
    }

    private void annotateEntry(JsonObject entry, OMTBlockEntry omtBlockEntry, AnnotationHolder holder) {
        JsonObject typeAttributes = getAttributes(getEntryType(entry));
        if (typeAttributes != null &&
                !typeAttributes.keySet().isEmpty()) {
            if (omtBlockEntry.getSequence() != null) {
                annotateSequenceItem(omtBlockEntry, typeAttributes, holder);
            } else if (omtBlockEntry.getBlock() != null) {
                annotateBlockItem(omtBlockEntry, typeAttributes, holder);
            }
        }
    }

    private void annotateModelTree(JsonObject object, OMTBlock block, List<OMTBlockEntry> entryList, AnnotationHolder holder) {
        JsonObject attributes = object.has(ATTRIBUTESKEY) ? object.getAsJsonObject(ATTRIBUTESKEY) : object;
        Set<String> keys = attributes.keySet();
        for (OMTBlockEntry omtBlockEntry : entryList) {
            String label = getEntryBlockLabel(omtBlockEntry);
            String entryBlockLabelText = getEntryBlockLabel(block);
            PsiElement targetLabel = getEntryBlockLabelElement(omtBlockEntry);

            if (!keys.contains(label)) {
                String errorMessage = String.format("%s is not a known attribute for %s",
                        label, entryBlockLabelText);
                holder.newAnnotation(HighlightSeverity.ERROR, errorMessage)
                        .range(targetLabel)
                        .withFix(ModelIntention.getRemoveBlockEntryIntention(omtBlockEntry, "Remove"))
                        .create();
            } else {
                JsonObject entry = attributes.getAsJsonObject(label);
                if (entry.has("type")) {
                    annotateEntry(entry, omtBlockEntry, holder);
                }
            }
        }
        annotateMissingEntries(attributes, block, holder);
    }

    public List<String> getLocalCommands(PsiElement element) {
        List<OMTBlockEntry> blockEntries = psiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTFile);
        OMTModelItemBlock modelItemBlock = psiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        if (modelItemBlock == null) {
            return new ArrayList<>();
        }
        String modelItemLabel = modelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1);
        JsonObject member = getAttributes(modelItemLabel);

        List<String> commands = new ArrayList<>();
        List<OMTModelItemBlock> modelItemBlocks = psiTreeUtil.getChildrenOfTypeAsList(modelItemBlock.getParent(), OMTModelItemBlock.class);
        modelItemBlocks.forEach(omtModelItemBlock ->
        {
            String type = omtModelItemBlock.getModelItemLabel().getModelItemTypeElement().getText();
            if (type.equalsIgnoreCase("!ontology")) {
                commands.add("LOAD_ONTOLOGY");
            }
        });

        while (member != null) {
            if (member.has("localCommands")) {
                JsonArray localCommands = member.getAsJsonArray("localCommands");
                localCommands.forEach(jsonElement -> commands.add(jsonElement.getAsString()));
            }
            if (member.has(ATTRIBUTESKEY) && !blockEntries.isEmpty()) {
                OMTBlockEntry omtBlockEntry = blockEntries.remove(blockEntries.size() - 1);
                JsonObject attributes = (JsonObject) member.get(ATTRIBUTESKEY);
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


    public JsonObject getJson(PsiElement element) {
        List<OMTBlockEntry> blockEntries = psiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent instanceof OMTModelItemBlock);
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        if (modelItemBlock.isPresent()) {
            JsonObject attributes = getAttributes(getModelItemType(element));
            while (attributes != null && attributes.has(ATTRIBUTESKEY) && !blockEntries.isEmpty()) {
                OMTBlockEntry blockEntry = blockEntries.remove(blockEntries.size() - 1);
                String entryBlockLabel = getEntryBlockLabel(blockEntry);
                attributes = attributes.getAsJsonObject(ATTRIBUTESKEY).getAsJsonObject(entryBlockLabel);
                if (attributes != null) {
                    if (attributes.has("type")) {
                        String type = attributes.get("type").getAsString();
                        if (type.endsWith("Def")) {
                            attributes = getAttributes(type.substring(0, type.length() - 3));
                        }
                    }
                    if (attributes.has(MAPOFKEY)) {
                        String type = attributes.get(MAPOFKEY).getAsString();
                        if (type.endsWith("Def")) {
                            attributes = getAttributes(type.substring(0, type.length() - 3));
                        }
                        blockEntries.remove(blockEntries.size() - 1);
                    }
                    attributes.addProperty("entryLabel", entryBlockLabel);
                }

            }
            return attributes;
        }
        return new JsonObject();
    }

    public boolean isOntology(PsiElement element) {
        return element instanceof OMTModelItemBlock &&
                ((OMTModelItemBlock) element).getModelItemLabel().getModelItemTypeElement().getText().equalsIgnoreCase("!ontology");
    }
}
