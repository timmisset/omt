package com.misset.opp.omt.psi;

import com.intellij.psi.PsiElement;

public class OMTCurie {
    private boolean isConstant;
    private PsiElement element;
    public OMTCurie(OMTCurieElement element) {
        this.element = element;
        this.isConstant = false;
    }
    public OMTCurie(OMTCurieConstantElement element) {
        this.element = element;
        this.isConstant = true;
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
    public boolean isPrefixDefinition() {
        return element.getParent() instanceof OMTPrefix;
    }
}
