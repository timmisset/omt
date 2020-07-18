package com.misset.opp.omt.psi;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OMTCurie {
    private boolean isConstant = false;
    private PsiElement element;
    public OMTCurie(PsiElement element, boolean isConstant) {
        this.element = element;
        this.isConstant = isConstant;
    }
    public PsiElement getElement() {
        return element;
    }
    public String getCuriePrefix() { return isConstant ? getPrefixPart().substring(1) : getPrefixPart();  }
    private String getPrefixPart() {return getText().substring(0, getText().indexOf(':') + 1);}
    public boolean isDefinedByPrefix(OMTPrefix prefix) {
        return prefix.getCuriePrefix().getText().equals(getCuriePrefix());
    }
    public String getText() { return element.getText(); }
}
