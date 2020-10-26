package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
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

    private final String ATTRIBUTES = "attributes";
    private final String MAPOF = "mapOf";
    private final String MAP = "map";
    private final String NODE = "node";
    private final String NAME = "name";
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
                        propertyLabel.equals(omtBlockEntry.getName()))
                .findFirst();
    }

    /**
     * Returns the block entries that are accessible to this element. This is the case when the entry blocks
     * are direct parents (recursive) or their siblings. But not the child entries in the siblings of parents.
     *
     * @param element
     * @param labels
     * @return
     */
    public List<OMTBlockEntry> getConnectedEntries(PsiElement element, List<String> labels) {
        List<OMTBlockEntry> blockEntries = new ArrayList<>();
        List<OMTBlockEntry> parents = PsiTreeUtil.collectParents(element,
                OMTBlockEntry.class, false, parent -> parent instanceof OMTModelItemBlock);
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
        return labelElement == null ? "" : labelElement.getText().replace(":", "");
    }

    private PsiElement getEntryBlockLabelElement(PsiElement element) {
        if (element instanceof OMTModelItemBlock) {
            return ((OMTModelItemBlock) element).getModelItemLabel().getPropertyLabel();
        }
        if (element instanceof OMTBlockEntry) {
            return ((OMTBlockEntry) element).getLabel();
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

    public List<String> getModelRootItems() {
        return projectUtil.getParsedModel().entrySet().stream().map(
                entry -> entry
                        .getValue()
                        .getAsJsonObject())
                .filter(jsonObject -> jsonObject.has("modelRoot") && jsonObject.get("modelRoot").getAsBoolean())
                .map(jsonObject -> jsonObject.get("name").getAsString())
                .collect(Collectors.toList());
    }

    public void annotateModelItemType(OMTModelItemTypeElement omtModelItemTypeElement, AnnotationHolder holder) {
        String modelItemType = getModelItemType(omtModelItemTypeElement);
        JsonObject jsonObject = getAttributes(modelItemType);
        if (jsonObject.keySet().isEmpty()) {
            holder.newAnnotation(HighlightSeverity.ERROR,
                    String.format("Unknown model type: %s", omtModelItemTypeElement.getText()))
                    .range(omtModelItemTypeElement)
                    .create();
        }
    }

    public void annotateBlock(OMTBlock block, AnnotationHolder holder) {
        annotateMissingEntries(getJsonAttributes(block).getAsJsonObject(ATTRIBUTES), block, holder);
    }

    public void annotateBlockEntry(OMTBlockEntry omtBlockEntry, AnnotationHolder holder) {
        String label = getEntryBlockLabel(omtBlockEntry);
        PsiElement targetLabel = getEntryBlockLabelElement(omtBlockEntry);

        JsonObject container = getJsonAtElementLevel(omtBlockEntry);
        Set<String> keys = container.has(ATTRIBUTES) ? container.getAsJsonObject(ATTRIBUTES).keySet() : new JsonObject().keySet();
        String containerName = container.has(NAME) ? container.get(NAME).getAsString() : null;

        if (containerName != null && !keys.contains(label) && !isMapNode(container)) {
            String errorMessage = String.format("%s is not a known attribute for %s",
                    getEntryBlockLabel(targetLabel), containerName);

            IntentionAction remove = removeIntention.getRemoveIntention(omtBlockEntry);
            holder.newAnnotation(HighlightSeverity.ERROR, errorMessage)
                    .range(targetLabel)
                    .withFix(remove)
                    .create();
        }
    }

    private boolean isMapNode(JsonObject jsonInfo) {
        return jsonInfo.has(NODE) && jsonInfo.get(NODE).getAsString().equals(MAP) ||
                jsonInfo.has(MAPOF);
    }

    private void annotateMissingEntries(JsonObject attributes, OMTBlock block, AnnotationHolder holder) {
        List<String> entryLabels = block.getBlockEntryList().stream().map(this::getEntryBlockLabel).collect(Collectors.toList());
        if (attributes == null) {
            return;
        }
        List<String> missingElements = attributes.entrySet().stream()
                .filter(entry ->
                        entry.getValue().getAsJsonObject().has("required") &&
                                entry.getValue().getAsJsonObject().get("required").getAsBoolean() &&
                                !entryLabels.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!missingElements.isEmpty()) {
            String error = String.format("%s is missing attribute(s): %n%s",
                    getEntryBlockLabel(block),
                    String.join(", ", missingElements));
            holder.newAnnotation(HighlightSeverity.ERROR, error)
                    .range(getEntryBlockLabelElement(block)).create();
        }
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

        OMTFile file = (OMTFile) element.getContainingFile();
        String modelItemLabel = file.isModuleFile() ? "module" : getModelItemType(element);
        JsonObject member = getAttributes(modelItemLabel);
        if (member == null || member.keySet().isEmpty()) {
            return branch;
        }

        branch.add(member);

        while (!blockEntries.isEmpty()) {
            OMTBlockEntry omtBlockEntry = blockEntries.remove(blockEntries.size() - 1);
            String entryBlockLabel = getEntryBlockLabel(omtBlockEntry);
            JsonObject subMember = getTypeAttributes(
                    member.has(ATTRIBUTES) ?
                            member.get(ATTRIBUTES).getAsJsonObject() :
                            new JsonObject(), entryBlockLabel);
            if (!subMember.keySet().isEmpty()) {
                member = subMember;
                branch.add(member);
                member = addMapOfKeyElements(member, branch);
            }
        }
        return branch;
    }

    private JsonObject getTypeAttributes(JsonObject attributes, String propertyLabel) {
        if (attributes.has(propertyLabel)) {
            JsonObject member = (JsonObject) attributes.get(propertyLabel);
            if (member.has(TYPE)) {
                String memberType = member.get(TYPE).getAsString();
                if (memberType.endsWith(DEF)) {
                    member = getAttributes(memberType.substring(0, memberType.length() - 3));
                }
            }
            return member;
        }
        return new JsonObject();
    }

    private JsonObject addMapOfKeyElements(JsonObject member, List<JsonObject> branch) {
        while (member.has(MAPOF)) {
            String type = member.get(MAPOF).getAsString();
            if (type.endsWith(DEF)) {
                member = getAttributes(type.substring(0, type.length() - 3));
                branch.add(member);
            }
        }
        return member;
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
        return getJsonAtDepth(element, -1);
    }

    public JsonObject getJsonAtElementLevel(PsiElement element) {
        return getJsonAtDepth(element, getModelDepth(element));
    }

    /**
     * use depth: -1 to get the last item in the tree
     * only use this to get the exact modeltree information
     * for example, to get the Payload: mapOf PayloadPropertyDef information (combine with getModelDepth)
     * To know what attributes a payload property has, use getJson instead
     *
     * @param element
     * @param depth
     * @return
     */
    public JsonObject getJsonAtDepth(PsiElement element, int depth) {
        List<JsonObject> attributesBranch = getAttributesBranch(element);
        if (attributesBranch.isEmpty()) {
            return new JsonObject();
        }
        return attributesBranch.get(depth == -1 ? attributesBranch.size() - 1 : depth);
    }

    /**
     * returns the level this item has in the model by looking at the entryblock
     * 0 based, meaning an entry at the model root,like title, will receive level 0.
     * a payload item will receive 1 etc
     *
     * @param element
     * @return
     */
    public int getModelDepth(PsiElement element) {
        return PsiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTModelBlock).size();
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
