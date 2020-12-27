package com.misset.opp.omt.psi.support;

import com.intellij.psi.PsiElement;
import com.misset.opp.omt.psi.OMTBlockEntry;

import java.util.List;

public interface OMTBlockSup extends PsiElement {

    List<OMTBlockEntry> getBlockEntryList();

}
