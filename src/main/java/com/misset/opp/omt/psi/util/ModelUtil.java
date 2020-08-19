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
public class ModelUtil {

    /**
     * Returns the modelItem block that this element belongs to
     * A modelItem is an element declared directly under model:
     * @param element
     * @return
     */
    public static Optional<OMTModelItemBlock> getModelItemBlock(PsiElement element) {
        OMTModelItemBlock block = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        return block != null ? Optional.of(block) : Optional.empty();
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
        List<PsiElement> blockEntriesOrSpecificBlockParents = getBlockEntriesOrSpecificBlockParents(element);
        for (PsiElement entryBlock : blockEntriesOrSpecificBlockParents) {
            getSiblingEntryBlocks(entryBlock).stream().filter(entry ->
                    labels.contains(entry.getPropertyLabel().getPropertyLabelName()))
                    .forEach(blockEntries::add);
        }
        return blockEntries;
    }

    private static List<OMTBlockEntry> getSiblingEntryBlocks(PsiElement element) {
        return PsiTreeUtil.getChildrenOfTypeAsList(element.getParent(), OMTBlockEntry.class);
    }

    private static List<PsiElement> getBlockEntriesOrSpecificBlockParents(PsiElement element) {
        List<PsiElement> parents = new ArrayList<>();
        while (element != null && (!(element instanceof OMTModelItemBlock))) {
            if (element instanceof OMTBlockEntry || element instanceof OMTSpecificBlock) {
                parents.add(element);
            }
            element = element.getParent();
        }
        return parents;
    }

    /**
     * Returns the label of the block entry that directly contains this element
     *
     * @param element
     * @return
     */
    public static String getBlockEntryLabel(PsiElement element) {
        return getOMTBlockEntryLabel(
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
        return getOMTBlockEntryLabel(PsiTreeUtil.getTopmostParentOfType(element, OMTBlockEntry.class));
    }

    private static String getOMTBlockEntryLabel(OMTBlockEntry omtBlockEntry) {
        if (omtBlockEntry != null) {
            String label = omtBlockEntry.getPropertyLabel().getText();
            return label.endsWith(":") ? label.substring(0, label.length() - 1) : label;
        }
        return null;
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
        }
    }

    public static List<String> getLocalCommands(PsiElement element) {
        List<OMTBlockEntry> blockEntries = PsiTreeUtil.collectParents(element, OMTBlockEntry.class, false, parent -> parent == null || parent instanceof OMTFile);
        OMTModelItemBlock modelItemBlock = PsiTreeUtil.getTopmostParentOfType(element, OMTModelItemBlock.class);
        String modelItemLabel = modelItemBlock.getModelItemLabel().getModelItemTypeElement().getText().substring(1);
        JsonObject member = getAttributes(modelItemLabel);

        List<String> commands = new ArrayList<>();
        while (member != null) {
            if (member.has("localCommands")) {
                JsonArray localCommands = member.getAsJsonArray("localCommands");
                localCommands.forEach(jsonElement -> commands.add(jsonElement.getAsString()));
            }
            if (member.has("attributes") && blockEntries.size() > 0) {
                OMTBlockEntry omtBlockEntry = blockEntries.remove(blockEntries.size() - 1);
                JsonObject attributes = (JsonObject) member.get("attributes");
                String label = omtBlockEntry.getPropertyLabel().getText();
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
}
