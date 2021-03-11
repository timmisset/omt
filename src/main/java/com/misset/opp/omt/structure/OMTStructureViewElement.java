package com.misset.opp.omt.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.misset.opp.omt.psi.OMTBlock;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTFile;
import com.misset.opp.omt.psi.OMTGenericBlock;
import com.misset.opp.omt.psi.OMTModelBlock;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import com.misset.opp.omt.psi.OMTSequence;
import com.misset.opp.omt.psi.OMTSequenceItem;
import com.misset.opp.omt.psi.OMTSpecificBlock;
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
            presentationData.setPresentableText(((OMTSpecificBlock) myElement).getName());
        }
        if (myElement instanceof OMTBlockEntry) {
            OMTBlockEntry blockEntry = (OMTBlockEntry) this.myElement;
            presentationData.setPresentableText(blockEntry.getName());
        }
        return presentationData;
    }

    private TreeElement[] getFileChildren() {
        OMTBlock block = ((OMTFile) myElement).findChildByClass(OMTBlock.class);
        return block == null ? EMPTY_ARRAY : getBlockChildren(block).toArray(new TreeElement[0]);
    }

    private TreeElement[] getModelChildren() {
        List<TreeElement> treeElements = new ArrayList<>();
        OMTModelBlock modelBlock = (OMTModelBlock) this.myElement;
        List<OMTModelItemBlock> modelItemBlockList = modelBlock.getModelItemBlockList();
        for (OMTModelItemBlock modelItemBlock : modelItemBlockList) {
            treeElements.add(new OMTStructureViewElement((OMTModelItemBlockImpl) modelItemBlock));
        }
        return treeElements.toArray(new TreeElement[0]);
    }

    private TreeElement[] getGenericBlockChildren() {
        List<TreeElement> treeElements = new ArrayList<>();
        OMTGenericBlock genericBlock = (OMTGenericBlock) this.myElement;
        if (genericBlock.getSequence() != null) {
            treeElements.addAll(getSequenceChildren(genericBlock.getSequence()));
        }
        if (genericBlock.getBlock() != null) {
            treeElements.addAll(getBlockChildren(genericBlock.getBlock()));
        }
        return treeElements.toArray(new TreeElement[0]);
    }

    private List<TreeElement> getBlockChildren(OMTBlock block) {
        List<TreeElement> treeElements = new ArrayList<>();
        if (block == null) {
            return treeElements;
        }
        List<OMTBlockEntry> blockEntryList = block.getBlockEntryList();
        for (OMTBlockEntry blockEntry : blockEntryList) {
            treeElements.add(new OMTStructureViewElement((OMTBlockEntryImpl) blockEntry));
        }
        return treeElements;
    }

    private List<TreeElement> getSequenceChildren(OMTSequence sequence) {
        List<TreeElement> treeElements = new ArrayList<>();
        List<OMTSequenceItem> sequenceItemList = sequence.getSequenceItemList();
        for (OMTSequenceItem sequenceItem : sequenceItemList) {
            if (sequenceItem.getBlock() != null) {
                treeElements.add(new OMTStructureViewElement((OMTSequenceItemImpl) sequenceItem));
            }
        }
        return treeElements;
    }

    @NotNull
    @Override
    public TreeElement[] getChildren() {
        if (myElement instanceof OMTFile) {
            return getFileChildren();
        }
        if (myElement instanceof OMTGenericBlock) {
            return getGenericBlockChildren();
        }
        if (myElement instanceof OMTModelBlock) {
            return getModelChildren();
        }
        if (myElement instanceof OMTSequenceItem) {
            return getBlockChildren(((OMTSequenceItem) myElement).getBlock()).toArray(new TreeElement[0]);
        }
        if (myElement instanceof OMTModelItemBlock) {
            return getBlockChildren(((OMTModelItemBlock) myElement).getBlock()).toArray(new TreeElement[0]);
        }
        return EMPTY_ARRAY;
    }
}
