package com.misset.opp.omt.psi;

import com.intellij.psi.tree.IElementType;

public interface OMTIgnored {
    IElementType END_OF_LINE_COMMENT = new OMTElementType("END_OF_LINE_COMMENT");
}
