package com.misset.opp.omt;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

public class OMTParserTest extends LightJavaCodeInsightFixtureTestCase {

    private static String demoFile = "import:\n" +
            "    ../utils/persoon.toon.queries.omt:\n" +
            "    -   kernregisterPrioriteit\n" +
            "\n";

    private static Lexer getLexer() {
        return new Lexer() {
            private OMTLexer lexer = new OMTLexer(null);
            private CharSequence buffer;

            @Override
            public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
                this.buffer = buffer;
                lexer.reset(buffer, startOffset, endOffset, initialState);
            }

            @Override
            public int getState() {
                return lexer.yystate();
            }

            @Override
            public @Nullable IElementType getTokenType() {
                if(currentElement == null) { advance(); }
                return currentElement;
            }

            @Override
            public int getTokenStart() {
                return lexer.getTokenStart();
            }

            @Override
            public int getTokenEnd() {
                return lexer.getTokenEnd();
            }

            private IElementType currentElement;
            @Override
            public void advance() {
                try {
                    currentElement = lexer.advance();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public @NotNull LexerPosition getCurrentPosition() {
                return new LexerPosition() {
                    @Override
                    public int getOffset() {
                        return lexer.yylength();
                    }

                    @Override
                    public int getState() {
                        return lexer.yystate();
                    }
                };
            }

            @Override
            public void restore(@NotNull LexerPosition position) {
                lexer.yypushback(position.getOffset());
                lexer.yybegin(position.getState());
            }

            @Override
            public @NotNull
            CharSequence getBufferSequence() {
                return buffer;
            }

            @Override
            public int getBufferEnd() {
                return buffer.length();
            }
        };
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setName("Test");
        super.setUp();
    }
}
