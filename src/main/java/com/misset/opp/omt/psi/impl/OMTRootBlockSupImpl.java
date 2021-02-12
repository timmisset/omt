package com.misset.opp.omt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.misset.opp.omt.psi.OMTBlockEntry;
import com.misset.opp.omt.psi.OMTRootBlock;
import com.misset.opp.omt.psi.support.OMTBlockSup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class OMTRootBlockSupImpl extends ASTWrapperPsiElement implements OMTBlockSup {
    public OMTRootBlockSupImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public List<OMTBlockEntry> getBlockEntryList() {
        return getNode().getPsi(OMTRootBlock.class).getBlockEntryList();
    }
}
