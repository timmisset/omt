package com.misset.opp.ttl;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.testFramework.UsefulTestCase.assertDoesntContain;

class TTLLexerTest {

    @Test
    void testStringEntry() {
        String contentToTest = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
                "@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
                "@prefix ex: <http://example.org/stuff/1.0/> .\n" +
                "\n" +
                "<http://www.w3.org/TR/rdf-syntax-grammar>\n" +
                "  dc:title \"RDF/XML Syntax Specification (Revised)\" ;\n" +
                "  ex:editor [\n" +
                "    ex:fullname \"Dave Beckett\";\n" +
                "    ex:homePage <http://purl.org/net/dajobe/>\n" +
                "  ] .";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    private List<String> getElements(String content, int start, int end) {
        TTLLexerAdapter lexer = new TTLLexerAdapter();
        lexer.start(content, start, end, 0);
        List<String> elements = new ArrayList<>();
        IElementType element = lexer.getTokenType();
        if (element != null) {
            elements.add(element.toString());
        }
        boolean cont = true;
        while (cont) {
            lexer.advance();
            element = lexer.getTokenType();
            if (element != null) {
                elements.add(element.toString());
            } else {
                cont = false;
            }
        }
        return elements;
    }

    private List<String> getElements(String content) {
        return getElements(content, 0, content.length());
    }

}
