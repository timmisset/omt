package com.misset.opp.omt.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import com.misset.opp.omt.psi.OMTFile;
import org.jetbrains.annotations.NotNull;

public class OMTStructureViewModel extends StructureViewModelBase implements
        StructureViewModel.ElementInfoProvider {

    public OMTStructureViewModel(PsiFile psiFile) {
        super(psiFile, new OMTStructureViewElement(psiFile));
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[]{Sorter.ALPHA_SORTER};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof OMTFile;
    }

}
