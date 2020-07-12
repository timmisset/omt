package com.misset.opp.omt;

import com.intellij.lexer.FlexAdapter;
import java.io.Reader;

public class OMTLexerAdapter extends FlexAdapter {
    public OMTLexerAdapter() {
        super(new OMTLexer((Reader) null));
    }
}
