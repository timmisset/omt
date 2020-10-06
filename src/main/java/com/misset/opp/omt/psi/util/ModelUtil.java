package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.intentions.generic.RemoveIntention;
import com.sun.istack.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public class ModelUtil {

    private final String ATTRIBUTESKEY = "attributes";
    private final String MAPOFKEY = "mapOf";
    private final String TYPE = "type";
    private final String DEF = "Def";

    public static final ModelUtil SINGLETON = new ModelUtil();
    private ProjectUtil projectUtil = ProjectUtil.SINGLETON;
    private RemoveIntention removeIntention = RemoveIntention.SINGLETON;

    /**
     * Returns the modelitem block containing the element, for example an Activity or Procedure block
     *
     * @param element
     * @return
     */
    public Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
        if (element instanceof OMTModelItemBlock) {
            return Optional.of((OMTModelItemBlock) element);
        }
        OMTModelItemBlock block = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
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
        List<OMTBlockEntry> parents = PsiTreeUtil.collectParents(element,
                OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTModelItemBlock);
        for (OMTBlockEntry parent : parents) {
            getSiblingEntryBlocks(parent).stream().filter(entry ->
                    labels.contains(getEntryBlockLabel(entry)))
                    .forEach(blockEntries::add);
        }
        return blockEntries;
    }

    private List<OMTBlockEntry> getSiblingEntryBlocks(PsiElement element) {
        return PsiTreeUtil.getChildrenOfTypeAsList(element.getParent(), OMTBlockEntry.class);
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
        if (labelElement == null) {
            return  ""; }
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
        if (element == null) {
            return null;
        }
        return getEntryBlockLabelElement(PsiTreeUtil.findFirstParent(element,
                parent -> parent instanceof OMTBlockEntry
                        ||
                        parent instanceof OMTModelItemBlock
        ));
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
        List<OMTBlockEntry> blockEntries = PsiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent instanceof OMTModelBlock);
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
            annotateModelTree(attributes, block, holder);
        }
    }

    public void annotateModelTree(JsonObject object, OMTBlock block, AnnotationHolder holder) {
        JsonObject attributes = object.has(ATTRIBUTESKEY) ? object.getAsJsonObject(ATTRIBUTESKEY) : object;
        annotateEntries(attributes, block, holder);
        annotateMissingEntries(attributes, block, holder);
    }

    private void annotateEntries(JsonObject attributes, OMTBlock block, AnnotationHolder holder) {
        Set<String> keys = attributes.keySet();
        List<OMTBlockEntry> entryList = block.getBlockEntryList();
        // reset all to false
        entryList.forEach(omtBlockEntry -> omtBlockEntry.setAnnotated(false));

        for (OMTBlockEntry omtBlockEntry : entryList) {
            String label = getEntryBlockLabel(omtBlockEntry);
            String entryBlockLabelText = getEntryBlockLabel(block);
            PsiElement targetLabel = getEntryBlockLabelElement(omtBlockEntry);

            if (!omtBlockEntry.isAnnotated() && !keys.contains(label)) {
                String errorMessage = String.format("%s is not a known attribute for %s",
                        label, entryBlockLabelText);
                IntentionAction remove = removeIntention.getRemoveIntention(omtBlockEntry);
                holder.newAnnotation(HighlightSeverity.ERROR, errorMessage)
                        .range(targetLabel)
                        .withFix(remove)
                        .create();
            } else {
                JsonObject entry = attributes.getAsJsonObject(label);
                if (entry.has("type")) {
                    annotateEntry(entry, omtBlockEntry, holder);
                }
            }
        }
    }

    private void annotateEntry(JsonObject entry, OMTBlockEntry omtBlockEntry, AnnotationHolder holder) {
        JsonObject typeAttributes = getAttributes(getEntryType(entry));
        if (typeAttributes != null &&
                !typeAttributes.keySet().isEmpty()) {
            if (omtBlockEntry.getSequence() != null) {
                annotateSequenceItem(omtBlockEntry, typeAttributes, holder);
                omtBlockEntry.setAnnotated(true);
            } else if (omtBlockEntry.getBlock() != null) {
                annotateBlockItem(omtBlockEntry, typeAttributes, holder);
                omtBlockEntry.setAnnotated(true);
            }
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
            AnnotationBuilder annotationBuilder = holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("%s is missing attribute(s): %n%s",
                            getEntryBlockLabel(block),
                            String.join(", ", missingElements)));

            annotationBuilder.range(getEntryBlockLabelElement(block));
            annotationBuilder.create();
        }
    }

    private void annotateSequenceItem(OMTBlockEntry omtBlockEntry, JsonObject attributes, AnnotationHolder holder) {
        // process as sequence:
        OMTSequence sequence = omtBlockEntry.getSequence();
        if (sequence == null) {
            return;
        }
        for (OMTSequenceItem sequenceItem : sequence.getSequenceItemList()) {
            if (sequenceItem.getBlock() != null) {
                // sequence item consisting of a block (map) structure
                annotateModelTree(attributes, sequenceItem.getBlock(), holder);
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
        OMTBlock block = omtBlockEntry.getBlock();
        if (block == null) {
            return;
        }
        if (attributes.has(ATTRIBUTESKEY)) {
            // process the block:
            annotateModelTree(attributes.getAsJsonObject(ATTRIBUTESKEY), block, holder);
        }
        if (attributes.has(MAPOFKEY)) {
            // process all entry using the mapOf
            String mappingType = attributes.get(MAPOFKEY).getAsString();
            if (mappingType.endsWith("Def")) {
                mappingType = mappingType.substring(0, mappingType.length() - 3);
            }
            attributes = getAttributes(mappingType);
            for (OMTBlockEntry entryListItem : block.getBlockEntryList()) {
                if (entryListItem.getBlock() != null) {
                    annotateModelTree(attributes, entryListItem.getBlock(), holder);
                }
                entryListItem.setAnnotated(true);
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

    public List<String> getLocalCommands(PsiElement element) {

        List<String> commands = new ArrayList<>();
        if (hasOntologyCommand(element)) {
            commands.add("LOAD_ONTOLOGY");
        }

        List<JsonObject> attributesBranch = getAttributesBranch(element);
        attributesBranch.forEach(jsonObject ->
                {
                    if (jsonObject.has("localCommands")) {
                        jsonObject.getAsJsonArray("localCommands")
                                .forEach(command -> commands.add(command.getAsString()));
                    }
                }
        );
        return commands;
    }

    private List<JsonObject> getAttributesBranch(PsiElement element) {
        List<JsonObject> branch = new ArrayList<>();
        List<OMTBlockEntry> blockEntries = PsiTreeUtil.collectParents(element,
                OMTBlockEntry.class, false,
                parent -> parent == null || parent instanceof OMTFile || parent instanceof OMTModelBlock);
        String modelItemLabel = getModelItemType(element);
        JsonObject member = getAttributes(modelItemLabel);
        if (member == null || member.keySet().isEmpty()) {
            return branch;
        }

        branch.add(member);

        while (!blockEntries.isEmpty()) {
            OMTBlockEntry omtBlockEntry = blockEntries.remove(blockEntries.size() - 1);
            String entryBlockLabel = getEntryBlockLabel(omtBlockEntry);
            JsonObject subMember = getTypeAttributes(
                    member.has(ATTRIBUTESKEY) ?
                            member.get(ATTRIBUTESKEY).getAsJsonObject() :
                            new JsonObject(), entryBlockLabel, blockEntries);
            if (!subMember.keySet().isEmpty()) {
                member = subMember;
                branch.add(member);
            }
        }
        return branch;
    }

    private JsonObject getTypeAttributes(JsonObject attributes, String propertyLabel, List<OMTBlockEntry> blockEntries) {
        if (attributes.has(propertyLabel)) {
            JsonObject member = (JsonObject) attributes.get(propertyLabel);
            if (member.has(TYPE)) {
                String memberType = member.get(TYPE).getAsString();
                if (memberType.endsWith(DEF)) {
                    member = getAttributes(memberType.substring(0, memberType.length() - 3));
                }
            }
            if (member.has(MAPOFKEY)) {
                String type = member.get(MAPOFKEY).getAsString();
                if (type.endsWith(DEF)) {
                    member = getAttributes(type.substring(0, type.length() - 3));
                }
            }
            return member;
        }
        return new JsonObject();
    }

    private boolean hasOntologyCommand(PsiElement element) {
        OMTModelBlock modelBlock = PsiTreeUtil.getTopmostParentOfType(element, OMTModelBlock.class);
        if (modelBlock == null) {
            return false;
        }
        return modelBlock.getModelItemBlockList().stream()
                .map(this::getModelItemType)
                .anyMatch(s -> s.equals("Ontology"));
    }

    /**
     * Retraces the steps of the element back to the top level model and then returns the corresponding
     * JSON attributes collection based on the property keys used in the block entries
     * <p>
     * When the input element is a (scalar) value, it returns the properties / type of that value
     * to get the entry block attributes instead, use getJsonAttributes
     *
     * @param element
     * @return
     */
    public JsonObject getJson(PsiElement element) {
        List<JsonObject> attributesBranch = getAttributesBranch(element);
        if (attributesBranch.isEmpty()) {
            return new JsonObject();
        }

        return attributesBranch.get(attributesBranch.size() - 1);
    }

    /**
     * Retraces the steps of the element back to the top level model and then returns the corresponding
     * JSON attributes collection based on the property keys used in the block entries
     * <p>
     * When the input element is a (scalar) value, it returns the attributes of the entry block
     *
     * @param element
     * @return
     */
    public JsonObject getJsonAttributes(PsiElement element) {
        List<JsonObject> attributesBranch = getAttributesBranch(element);
        if (attributesBranch.isEmpty()) {
            return new JsonObject();
        }

        List<JsonObject> jsonObjectsWithName = attributesBranch.stream().filter(
                jsonObject -> jsonObject.has("name")
        ).collect(Collectors.toList());
        return jsonObjectsWithName.isEmpty() ? new JsonObject() : jsonObjectsWithName.get(jsonObjectsWithName.size() - 1);
    }

    public boolean isOntology(PsiElement element) {
        return element instanceof OMTModelItemBlock &&
                getModelItemType(element).equals("Ontology");
    }
}
