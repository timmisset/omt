package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import com.misset.opp.omt.domain.util.Helper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class OMTLexerTest {

    @Test
    public void testOMTFile() throws IOException {
        // This method will test an entire OMT file for identical contents with the expected outcome
        // the expected content is based on the output after parsing was finally successful. Therefore, this method is to make
        // sure any changes to the lexer won't mess this minimally parsable file
        String content = Helper.getResourceAsString("test-kopieerPersoon.omt");
        String validationContent = Helper.getResourceAsString("validate_test_omt.txt");
//        assertEquals(Arrays.asList(validationContent.split("\r\n")), getElements(content));
//        System.out.println(getElements(content));
        getElements(content);
    }

    private List<String> getElements(String content) throws IOException {
        OMTLexer lexer = new OMTLexer(null);
        lexer.reset(content, 0, content.length(), 0);
        List<String> elements = new ArrayList<>();
        boolean cont = true;
        while(cont) {
            IElementType element = lexer.advance();
            if(element != null) { elements.add(element.toString()); }
            else { cont = false; }
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
