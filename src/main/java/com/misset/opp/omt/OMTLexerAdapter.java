package com.misset.opp.omt;

import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;

public class OMTLexerAdapter extends FlexAdapter {
    public OMTLexerAdapter(boolean enableLogging) {
        super(new OMTLexer(null, enableLogging));
    }

    public OMTLexerAdapter() {
        super(new OMTLexer(null, false));
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        super.start(buffer, 0, endOffset, initialState);
    }
}
