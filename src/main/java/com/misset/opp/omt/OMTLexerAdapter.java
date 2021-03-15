package com.misset.opp.omt;

import com.intellij.lexer.FlexAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OMTLexerAdapter extends FlexAdapter {
    private final String origin;
    private Logger logger = Logger.getAnonymousLogger();

    public OMTLexerAdapter(Logger logger) {
        super(new OMTLexer(null, logger));
        origin = "test";
        this.logger = logger;
    }

    public OMTLexerAdapter(String origin) {
        super(new OMTLexer(null, Level.SEVERE));
        this.origin = origin;
        logger.setLevel(Level.SEVERE);
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        logger.log(Level.INFO, String.format("%s, started with offset %s - %s in state %s%n", origin, startOffset, endOffset, initialState));
        if (startOffset > 0) {
            // only the HighlightingLexer will restart at an offset
            initLexerStartState(buffer.toString(), startOffset);
        }
        super.start(buffer, startOffset, endOffset, initialState);
    }

    private void initLexerStartState(String buffer, int endOffset) {
        // method to ensure that the lexer starts in the right scalar type
        // when restarting (Highlighting only)
        // although quick, it is still a rather expensive workaround:
        // TODO: check if the Highlighting lexer can be forced to always start at offSet 0
        // this cannot be done by overriding the startOffset since this will cause a shifting
        // error in the parser. Probably the number of returned segments mismatched with the expected
        // amount of segments
        OMTLexerAdapter lexer = new OMTLexerAdapter(logger);
        lexer.start(buffer, 0, endOffset, 0);
        boolean cont = true;
        while (cont) {
            lexer.advance();
            cont = lexer.getTokenType() != null;
        }
        final OMTLexer currentLexer = (OMTLexer) super.getFlex();
        final OMTLexer initLexer = (OMTLexer) lexer.getFlex();
        currentLexer.currentBlockLabel = initLexer.currentBlockLabel;
    }
}
