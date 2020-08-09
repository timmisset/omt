package com.misset.opp.omt;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightJavaCodeInsightTestCase;
import org.intellij.sdk.language.parser.OMTParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;

public class OMTParserTest {

    @Test
    public void test() {
        String content = "import:\n" +
                "    '@client/medewerker/src/utils/lidmaatschap.queries.omt':\n" +
                "        -   currentLidmaatschap\n" +
                "    '../utils/koppel-dossier-resource.command.omt':\n" +
                "        -   koppelDossierResource\n";

        Lexer lexer = getLexer();
        ParserDefinition definition = new OMTParserDefinition();

        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(definition, lexer, content);
        ASTNode treeBuilt = builder.getTreeBuilt();


        System.out.println(treeBuilt);
    }


    private Lexer getLexer() {
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
            public @NotNull CharSequence getBufferSequence() {
                return buffer;
            }

            @Override
            public int getBufferEnd() {
                return buffer.length();
            }
        };
    }
}
