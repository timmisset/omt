package com.misset.opp.omt.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.misset.opp.omt.psi.*;
import com.misset.opp.omt.psi.impl.OMTBlockEntryImpl;
import com.misset.opp.omt.psi.impl.OMTModelItemBlockImpl;
import com.misset.opp.omt.psi.impl.OMTSequenceItemImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OMTStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement myElement;

    public OMTStructureViewElement(NavigatablePsiElement element) {
        this.myElement = element;
    }

    @Override
    public Object getValue() {
        return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
        myElement.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return myElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return myElement.canNavigateToSource();
    }

    @NotNull
    @Override
    public String getAlphaSortKey() {
        String name = myElement.getName();
        return name != null ? name : "";
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        ItemPresentation presentation = myElement.getPresentation();
        if (presentation != null) {
            return presentation;
        }
        PresentationData presentationData = new PresentationData();
        if (myElement instanceof OMTSpecificBlock) {
            presentationData.setPresentableText(myElement.getName());
        }
        if (myElement instanceof OMTBlockEntry) {
            OMTBlockEntry blockEntry = (OMTBlockEntry) this.myElement;
            if (blockEntry.getSpecificBlock() != null) {
                OMTSpecificBlock specificBlock = blockEntry.getSpecificBlock();
                presentationData.setPresentableText(specificBlock.getName());
            } else {
                OMTPropertyLabel propertyLabel = blockEntry.getPropertyLabel();
                if (propertyLabel != null) {
                    presentationData.setPresentableText(propertyLabel.getPropertyLabelName());
                }
            }
        }
        if (myElement instanceof OMTModelItemBlock) {
            OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) this.myElement;
            presentationData.setPresentableText(modelItemBlock.getModelItemLabel().getText());
        }
        return presentationData;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (myElement instanceof OMTFile) {
            OMTBlock omtBlock = ((OMTFile) myElement).findChildByClass(OMTBlock.class);
            if (omtBlock == null) {
                return EMPTY_ARRAY;
            }

            List<OMTBlockEntry> blockEntryList = omtBlock.getBlockEntryList();
            List<TreeElement> treeElements = new ArrayList<>();
            for (OMTBlockEntry blockEntry : blockEntryList) {
                treeElements.add(new OMTStructureViewElement((OMTBlockEntryImpl) blockEntry));
            }
            return treeElements.toArray(new TreeElement[0]);
        }
        if (myElement instanceof OMTBlockEntry) {
            OMTBlockEntry blockEntry = (OMTBlockEntry) this.myElement;
            List<TreeElement> treeElements = new ArrayList<>();
            if (blockEntry.getSequence() != null) {
                OMTSequence sequence = blockEntry.getSequence();
                List<OMTSequenceItem> sequenceItemList = sequence.getSequenceItemList();
                for (OMTSequenceItem sequenceItem : sequenceItemList) {
                    if (sequenceItem.getBlock() != null) {
                        treeElements.add(new OMTStructureViewElement((OMTSequenceItemImpl) sequenceItem));
                    }
                }
            }
            if (blockEntry.getBlock() != null) {
                OMTBlock block = blockEntry.getBlock();
                for (OMTBlockEntry omtBlockEntry : block.getBlockEntryList()) {
                    treeElements.add(new OMTStructureViewElement((OMTBlockEntryImpl) omtBlockEntry));
                }
            }
            if (blockEntry.getSpecificBlock() != null) {
                OMTSpecificBlock specificBlock = blockEntry.getSpecificBlock();
                if (specificBlock.getModelBlock() != null) {
                    OMTModelBlock modelBlock = specificBlock.getModelBlock();
                    List<OMTModelItemBlock> modelItemBlockList = modelBlock.getModelItemBlockList();
                    for (OMTModelItemBlock modelItemBlock : modelItemBlockList) {
                        treeElements.add(new OMTStructureViewElement((OMTModelItemBlockImpl) modelItemBlock));
                    }
                }
            }
            return treeElements.toArray(new TreeElement[0]);
        }
        if (myElement instanceof OMTSequenceItem) {
            List<TreeElement> treeElements = new ArrayList<>();
            OMTSequenceItem sequenceItem = (OMTSequenceItem) this.myElement;
            addBlockEntries(sequenceItem.getBlock(), treeElements);
            return treeElements.toArray(new TreeElement[0]);
        }
        if (myElement instanceof OMTModelItemBlock) {
            List<TreeElement> treeElements = new ArrayList<>();
            OMTModelItemBlock modelItemBlock = (OMTModelItemBlock) this.myElement;
            addBlockEntries(modelItemBlock.getBlock(), treeElements);
            return treeElements.toArray(new TreeElement[0]);
        }
        return EMPTY_ARRAY;
    }

    private void addBlockEntries(OMTBlock block, List<TreeElement> treeElements) {
        if (block != null) {
            List<OMTBlockEntry> blockEntryList = block.getBlockEntryList();
            for (OMTBlockEntry omtBlockEntry : blockEntryList) {
                treeElements.add(new OMTStructureViewElement((OMTBlockEntryImpl) omtBlockEntry));
            }
        }
    }
}
