package com.misset.opp.omt;

import com.intellij.lexer.FlexAdapter;

public class OMTLexerAdapter extends FlexAdapter {
    public OMTLexerAdapter() {
        super(new OMTLexer(null, false));
    }
}
