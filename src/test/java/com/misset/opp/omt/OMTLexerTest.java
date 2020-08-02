package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.ParsingTestCase;
import com.misset.opp.omt.psi.util.Helper;
import org.intellij.sdk.language.parser.OMTParser;
import org.junit.jupiter.api.Test;

import java.io.*;

class OMTLexerTest extends ParsingTestCase {

    public OMTLexerTest() {
        super("", "omt", new OMTParserDefinition());
    }

    /**
     *
     * @return path to test data file directory relative to root of this module.
     */
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/lexer";
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }

    @Test
    public void testParsingTestData() {
        doTest(true);
    }

    @Test
    void QueryTest() throws IOException {
        Reader reader = new BufferedReader(new FileReader(Helper.getResource("testData/lexer/queries.omt")));
        OMTLexer omtLexer = new OMTLexer(reader);
        int tokenStart = omtLexer.getTokenStart();

        String buffer = "";
        omtLexer.reset(buffer, 0, 100, omtLexer.yystate());
        IElementType advance = omtLexer.advance();
        System.out.println(advance);
    }
}
