package com.misset.opp.omt.psi.util;//package com.misset.opp.omt.domain.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTSpecificBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//
//import com.intellij.openapi.project.Project;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.util.PsiTreeUtil;
//import com.misset.opp.omt.domain.OMTModelItem;
//import com.misset.opp.omt.domain.OMTModelItemAttribute;
//import com.misset.opp.omt.psi.*;
//import com.misset.opp.omt.domain.OMTParameter;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
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
}
