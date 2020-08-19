package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.Test;
import util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OMTLexerTest {

    boolean printLexerLog = true;
    boolean validate = false;

    @Test
    public void testKopieerPersoon() throws IOException {
        testOMTFile("testKopieerPersoon");
    }

    @Test
    public void testMaakVerhoor() throws IOException {
        testOMTFile("testMaakVerhoor");
    }

    @Test
    public void testMappingInDictionary() throws IOException {
        testOMTFile("testMappingInDictionary");
    }

    @Test
    public void testPersoonToon() throws IOException {
        testOMTFile("testPersoonToon");
    }

    @Test
    public void testQueries() throws IOException {
        testOMTFile("testQueries");
    }

    @Test
    public void testVoorgeleiding() throws IOException {
        testOMTFile("testVoorgeleiding");
    }

    private void testOMTFile(String name) throws IOException {
        // This method will test an entire OMT file for identical contents with the expected outcome
        // the expected content is based on the output after parsing was finally successful. Therefore, this method is to make
        // sure any changes to the lexer won't mess this minimally parsable file
        String content = Helper.getResourceAsString(String.format("lexer/%s.omt", name));
        String[] result = getElements(content).toArray(new String[0]);

        if (validate) {
            String[] validationContent = Arrays.stream(Helper.getResourceAsString(
                    String.format("lexer/valid/%s.txt", name))
                    .split(",")).map(String::trim).toArray(String[]::new);

            assertArrayEquals(validationContent, result);
        }

    }

    private List<String> getElements(String content) throws IOException {
        OMTLexer lexer = new OMTLexer(null, printLexerLog);
        lexer.reset(content, 0, content.length(), 0);
        List<String> elements = new ArrayList<>();
        boolean cont = true;
        while (cont) {
            IElementType element = lexer.advance();
            if (element != null) {
                elements.add(element.toString());
            } else {
                cont = false;
            }
        }
        return elements;
    }

    private String getLexerStateName(int state) {
        if(state == 0) { return "INITIAL"; }
        if(state == 2) { return "YAML_SCALAR"; }
        if(state == 4) { return "YAML_SEQUENCE"; }
        if(state == 6) { return "INDENT"; }
        if(state == 8) { return "ODT"; }
        return "UNKNOWN";
    }
}
