package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class OMTLexerTest extends OMTTestSuite {

    boolean printLexerLog = false;

    @Test
    void testStringEntry() {
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: Doe dit, dat, enz";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void testGraphs() {
        String contentToTest = "prefixes:\n" +
                "    pol:    <http://ontologie.politie.nl/def/politie#>\n" +
                "    dat:    <http://data.politie.nl/>\n" +
                "\n" +
                "model:\n" +
                "    Activiteit: !Activity\n" +
                "        queries: |\n" +
                "            DEFINE QUERY wijzeMeldenGraph => / dat:19000000000000_WijzeMelden;\n" +
                "        graphs:\n" +
                "            live:\n" +
                "            -   wijzeMeldenGraph\n" +
                "            -   /pol:Test\n";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void testModelPath() {
        String contentToTest = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       title: Test titel\n" +
                "       variables:\n" +
                "       - $test\n" +
                "";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void testIncompletePushbacks() {
        String contentToTest = "prefixes:\n" +
                "    pol:    <http://ontologie.politie.nl/def/politie#>\n" +
                "    rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#";
        List<String> elements = getElements(contentToTest);
        assertContainsElements(elements, "OMTTokenType.PREFIX_BLOCK_START", "BAD_CHARACTER");
    }

    @Test
    void testJdComment() {
        String contentToTest = "/**\n" +
                "* @param $myQuery (prefix:Type)\n" +
                "*/\n";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void testImportModule() {
        String contentToTest = "import:\n" +
                "   module:ModuleNaam:\n" +
                "   - member\n" +
                "\n";
        List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void testSimpleContent() {
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: 'TEST'\n" +
                "    Procedure: !Procedure\n";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testImportsContent() {
        String contentToTest = "import:\n" +
                "    '@client/bedrijf/src/com.misset.opp.util/queries.omt':\n" +
                "        -   gesorteerdeBedrijven\n" +
                "        -   weergaveNaamBedrijf\n" +
                "        -   alleHoedanigheidSoorten\n" +
                "    '../utils/verzend-status.queries.omt':\n" +
                "        -   isTeVersturenNaarBvh\n" +
                "    '@client/medewerker/src/utils/medewerker.queries.omt':\n" +
                "        -   minimaalEenMedewerkerAanwezig\n" +
                "\n";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testScalarValue() {

        String contentToTest = "rules:\n" +
                "   beginTijdstipMoetVoorEindTijdstip:\n" +
                "       $opvolgingsDossier / pol:incident / pol:beginTijdstip / EMPTY OR\n" +
                "       $opvolgingsDossier / pol:incident / pol:eindTijdstip / EMPTY OR\n" +
                "       $opvolgingsDossier / pol:incident / pol:beginTijdstip <= $opvolgingsDossier / pol:incident / pol:eindTijdstip\n" +
                "";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testUnclosedDocument() {

        String contentToTest = "import:\n" +
                "    module:Registratie:\n" +
                "    -   Raadplegen\n" +
                "        "; // not ending with unindented new line
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testJDCommentOnIndentationPlace() {
        String contentToTest = "queries:|\n" +
                "    /**\n" +
                "    * @param $param1 (string)\n" +
                "    */\n" +
                "    DEFINE QUERY myQuery($param1) => $param1;\n";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testJDCommentOnNewLine() {
        String contentToTest = "import:\n" +
                "    '@client/registratie/src/utils/bvh-nummer.queries.omt':\n" +
                "    -   heeftBvhNummer\n" +
                "\n" +
                "/**\n" +
                "* test\n" +
                "*/\n" +
                "prefixes:\n" +
                "    rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testAdjecentPrefixes() {
        String contentToTest = "prefixes:\n" +
                "    ont:     <http://ontologie> ont2: <http://ontologie2>";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testModuleName() {
        String contentToTest = "moduleName: Mijn module\n";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    private List<String> getElements(String content, int start, int end) {
        OMTLexerAdapter lexer = new OMTLexerAdapter(printLexerLog);
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
