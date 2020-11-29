package com.misset.opp.omt;

import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;

public class OMTLexerAdapter extends FlexAdapter {
    private final String origin;
    private boolean logging = false;

    public OMTLexerAdapter(boolean enableLogging) {
        super(new OMTLexer(null, enableLogging));
        origin = "test";
        this.logging = enableLogging;
    }

    public OMTLexerAdapter(String origin) {
        super(new OMTLexer(null, false));
        this.origin = origin;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        if (logging) {
            System.out.printf("%s, started with offset %s - %s in state %s%n", origin, startOffset, endOffset, initialState);
        }
        super.start(buffer, startOffset, endOffset, initialState);
    }
}
