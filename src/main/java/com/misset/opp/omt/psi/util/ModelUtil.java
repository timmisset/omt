package com.misset.opp.omt.psi.util;

import com.google.gson.JsonObject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTImport;
import com.misset.opp.omt.psi.OMTIndentedBlock;
import com.misset.opp.omt.psi.OMTModelBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTPropertyLabel;
import com.misset.opp.omt.psi.OMTTypes;
import com.misset.opp.util.UtilManager;
import com.sun.istack.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.misset.opp.util.UtilManager.getProjectUtil;

public class ModelUtil {
    public static final String ATTRIBUTES = "attributes";
    public static final String MAP_OF = "mapOf";
    public static final String MAP = "map";
    public static final String NODE = "node";
    public static final String TYPE = "type";
    public static final String SHORTCUT = "shortcut";
    public static final String ASSIGN_TO = "assignTo";
    public static final String DEF = "Def";
    public static final String NAMED_REFERENCE = "namedReference";

    /**
     * Returns the ModelItem block containing the element, for example an Activity or Procedure block
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
        return modelItemBlock.map(OMTModelItemBlock::getType).orElse(null);
    }

    public Optional<OMTBlockEntry> getEntryBlockEntry(PsiElement element, String propertyLabel) {
        return getEntryFromBlock(getEntryBlock(element), propertyLabel);
    }

    public Optional<OMTBlockEntry> getModelItemBlockEntry(PsiElement element, String propertyLabel) {
        Optional<OMTModelItemBlock> modelItemBlock = getModelItemBlock(element);
        if (modelItemBlock.isEmpty()) {
            return Optional.empty();
        }
        return getEntryFromBlock(modelItemBlock.get().getBlock(), propertyLabel);
    }

    private Optional<OMTBlockEntry> getEntryFromBlock(OMTBlock block, String propertyLabel) {
        if (block == null) {
            return Optional.empty();
        }
        return block.getBlockEntryList().stream()
                .filter(omtBlockEntry ->
                        propertyLabel.equals(omtBlockEntry.getName()))
                .findFirst();
    }

    /**
     * Returns the label of the block entry that directly contains this element
     * model:
     * MyActivity: !Activity
     * payload:
     * myPayloadParameter: myValue
     */
    public String getEntryBlockLabel(PsiElement element) {
        PsiElement labelElement = getEntryBlockLabelElement(element);
        return labelElement == null ? "" : labelElement.getText().replace(":", "");
    }

    public OMTBlock getEntryBlock(PsiElement element) {
        return PsiTreeUtil.getParentOfType(element, OMTBlock.class, false);
    }

    public String getEntryBlockValue(OMTBlockEntry blockEntry) {
        final String labelledPrefix = blockEntry.getLabel().getText();
        return blockEntry.getText().substring(labelledPrefix.length()).trim();
    }

    public PsiElement getEntryBlockLabelElement(PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element instanceof OMTBlockEntry) {
            return ((OMTBlockEntry) element).getLabel();
        }
        return getEntryBlockLabelElement(PsiTreeUtil.getParentOfType(element, OMTBlockEntry.class));
    }

    /**
     * Returns the label of the block entry that is part of the root level attributes of the ModelItem (MyActivity)
     * model:
     * MyActivity: !Activity
     * payload:
     * myPayloadParameter: myValue
     * will return payload for myValue
     */
    public String getModelItemEntryLabel(PsiElement element) {
        return getEntryBlockLabel(getModelItemBlockEntry(element));
    }

    /**
     * Returns the ModelItem entry of the element
     * model:
     * MyActivity: !Activity
     * payload:
     * myPayloadParameter: myValue
     * will return payload for myValue
     */
    public OMTBlockEntry getModelItemBlockEntry(PsiElement element) {
        return (OMTBlockEntry) PsiTreeUtil.findFirstParent(element, parent ->
                // model item entries are always indented
                // in the tree they are the grandchildren of the modelItemBlock
                parent instanceof OMTBlockEntry &&
                        parent.getParent() instanceof OMTIndentedBlock &&
                        parent.getParent().getParent() instanceof OMTModelItemBlock);
    }

    public JsonObject getAttributes(String memberName) {
        JsonObject parsedModel = getProjectUtil().getParsedModel();
        boolean hasMember = parsedModel.has(memberName);
        return hasMember ? (JsonObject) parsedModel.get(memberName) : new JsonObject();
    }

    public List<String> getModelRootItems() {
        return getProjectUtil().getParsedModel().entrySet().stream().map(
                entry -> entry
                        .getValue()
                        .getAsJsonObject())
                .filter(jsonObject -> jsonObject.has("modelRoot") && jsonObject.get("modelRoot").getAsBoolean())
                .map(jsonObject -> jsonObject.get("name").getAsString())
                .collect(Collectors.toList());
    }

    public List<String> getRootEntries() {
        return new ArrayList<>(getProjectUtil().getParsedModel().get("root")
                .getAsJsonObject()
                .get("attributes")
                .getAsJsonObject()
                .keySet());
    }

    public boolean isMapNode(JsonObject jsonInfo) {
        return jsonInfo.has(NODE) && jsonInfo.get(NODE).getAsString().equals(MAP) ||
                jsonInfo.has(MAP_OF);
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
        List<OMTBlockEntry> blockEntries = getAncestorEntries(element);
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
                    final JsonObject appendAttributes = getAttributes(memberType.substring(0, memberType.length() - 3));
                    appendAttributes.keySet().forEach(
                            key -> member.add(key, appendAttributes.get(key))
                    );
                }
            }
            return member;
        }
        return new JsonObject();
    }

    private JsonObject addMapOfKeyElements(JsonObject member, List<JsonObject> branch) {
        while (member.has(MAP_OF)) {
            String type = member.get(MAP_OF).getAsString();
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
     */
    public JsonObject getJson(PsiElement element) {
        return getJsonAtDepth(element, -1);
    }

    public JsonObject getJsonAtElementLevel(PsiElement element) {
        return getJsonAtDepth(element, getModelDepth(element));
    }

    public JsonObject getJsonAtParentLevel(PsiElement element) {
        return getJsonAtDepth(element, getModelDepth(element) - 1);
    }

    /**
     * use depth: -1 to get the last item in the tree
     * only use this to get the exact ModelTree information
     * for example, to get the Payload: mapOf PayloadPropertyDef information (combine with getModelDepth)
     * To know what attributes a payload property has, use getJson instead
     */
    public JsonObject getJsonAtDepth(PsiElement element, int depth) {
        List<JsonObject> attributesBranch = getAttributesBranch(element);
        if (attributesBranch.isEmpty()) {
            return new JsonObject();
        }
        final int finalDepth = depth == -1 ? attributesBranch.size() - 1 : depth;
        return finalDepth < attributesBranch.size() ? attributesBranch.get(finalDepth) : new JsonObject();
    }

    /**
     * returns the level this item has in the model by looking at the EntryBlock
     * 0 based, meaning an entry at the model root,like title, will receive level 0.
     * a payload item will receive 1 etc
     */
    public int getModelDepth(PsiElement element) {
        int correction = 1; // correction 1 for the modelItem block itself, should not be counted
        final List<OMTBlockEntry> ancestorEntries = getAncestorEntries(element);
        if (element instanceof OMTBlockEntry && ancestorEntries.contains(element)) {
            correction++;
        }
        return ancestorEntries.size() - correction;
    }

    private List<OMTBlockEntry> getAncestorEntries(PsiElement element) {
        return PsiTreeUtil.collectParents(element, OMTBlockEntry.class, true, parent -> parent == null || parent instanceof OMTModelBlock);
    }

    /**
     * Retraces the steps of the element back to the top level model and then returns the corresponding
     * JSON attributes collection based on the property keys used in the block entries
     * <p>
     * When the input element is a (scalar) value, it returns the attributes of the entry block
     */
    @NotNull
    public JsonObject getJsonAttributes(PsiElement element) {
        List<JsonObject> attributesBranch = getAttributesBranch(element);
        JsonObject attributesJson = new JsonObject();
        attributesJson.add(ATTRIBUTES, new JsonObject());

        if (attributesBranch.isEmpty()) {
            return attributesJson;
        }

        List<JsonObject> jsonObjectsWithName = attributesBranch.stream().filter(
                jsonObject -> jsonObject.has("name")
        ).collect(Collectors.toList());
        return jsonObjectsWithName.isEmpty() ? attributesJson : jsonObjectsWithName.get(jsonObjectsWithName.size() - 1);
    }

    public boolean isOntology(PsiElement element) {
        return element != null &&
                getModelItemType(element) != null &&
                getModelItemType(element).equals("Ontology");
    }

    public boolean isSequenceNode(ASTNode node) {
        final IElementType elementType = node.getElementType();
        final IElementType parentType = node.getTreeParent() != null ? node.getTreeParent().getElementType() : null;
        if (elementType == OMTTypes.PROPERTY_LABEL || parentType == OMTTypes.PROPERTY_LABEL) {
            PsiElement psiElement = node.getPsi();
            final OMTPropertyLabel propertyLabel = node.getElementType() == OMTTypes.PROPERTY_LABEL ?
                    (OMTPropertyLabel) node.getPsi() :
                    (OMTPropertyLabel) psiElement.getParent();
            final String propertyLabelName = propertyLabel.getName();
            final JsonObject parentJson = UtilManager.getModelUtil().getJsonAtParentLevel(psiElement);
            final JsonObject attributes = parentJson.getAsJsonObject(ATTRIBUTES);
            if (attributes == null || attributes.isJsonNull() || !attributes.has(propertyLabelName)) {
                return false;
            }

            final JsonObject entryDetails = attributes.getAsJsonObject(propertyLabelName);
            return entryDetails.has("node") &&
                    entryDetails.get("node").getAsString().equals("sequence");
        }
        return false;
    }

    public boolean isImportNode(ASTNode node) {
        return PsiTreeUtil.getParentOfType(node.getPsi(), OMTImport.class) != null;
    }

    public boolean isScalarEntry(PsiElement element) {
        return isEntry(element, "scalar");
    }

    public boolean isQueryEntry(PsiElement element) {
        return isEntry(element, "query");
    }

    public boolean isPrefixEntry(PsiElement element) {
        return isEntry(element, "prefix");
    }

    public boolean isTypeEntry(PsiElement element) {
        return isEntry(element, "type");
    }

    public boolean isIdEntry(PsiElement element) {
        return isEntry(element, "id");
    }

    private boolean isEntry(PsiElement element, String entryType) {
        final JsonObject json = getJson(element);
        if (json == null) return false;
        // check if the element can be mapped as destructed node (with properties) of the specified entry type
        if (isEntry(json, entryType)) {
            return true;
        }

        // no destructed info that match the criteria,
        // check if can be mapped to a shortcut that assigns to such a property:
        if (json.has(SHORTCUT) &&
                json.getAsJsonObject(SHORTCUT).has(ASSIGN_TO) &&
                json.has(ATTRIBUTES)) {
            final String shortcut = json.getAsJsonObject(SHORTCUT).get(ASSIGN_TO).getAsString();
            final JsonObject shortcutAssignee = json.getAsJsonObject(ATTRIBUTES).getAsJsonObject(shortcut);
            return shortcutAssignee != null && isEntry(shortcutAssignee, entryType);
        }
        return false;
    }

    private boolean isEntry(JsonObject json, String entryType) {
        return (json.has(TYPE) && json.get(TYPE).getAsString().equals(entryType)) ||
                (json.has(NODE) && json.get(NODE).getAsString().equals(entryType));
    }

}
