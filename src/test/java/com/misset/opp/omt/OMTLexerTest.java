package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OMTLexerTest extends OMTTestSuite {

    Logger logger;

    @BeforeEach
    protected void setUp() {
        logger = Mockito.spy(Logger.getLogger("lexerLogger"));
        logger.setLevel(Level.SEVERE);
    }

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
    void testScalarEntry() {
        // test to make sure the pipe is parsed as YAML_MULTILINE_DECORATOR, not as a PIPE
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        actions:\n" +
                "            actie:\n" +
                "                onSelect: |\n" +
                "                    @LOG('doe iets');\n" +
                "";
        final List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER", "OMTTokenType.PIPE");
        assertContainsElements(elements, "OMTTokenType.YAML_MULTILINE_DECORATOR");
    }

    @Test
    void testModuleName() {
        String contentToTest = "moduleName: Mijn module\n";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void testInterpolatedTitle() {
        String contentToTest = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: \"${ /ont:Class }\"";
        assertDoesntContain(getElements(contentToTest), "BAD_CHARACTER");
    }

    @Test
    void tagBeforeSequenceItem() {
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        variables:\n" +
                "        - !Ref $mijnVariabel";
        final List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
    }

    @Test
    void curieAsScalarValue() {
        String contentToTest =
                "prefixes:\n" +
                        "    abc:   <http://ontology/>\n" +
                        "model:\n" +
                        "    MijnOntologie<caret>: !Ontology\n" +
                        "        prefix: abc\n" +
                        "        classes:\n" +
                        "        -   id: LocalClassA\n" +
                        "            properties:\n" +
                        "                propC:\n" +
                        // the abc:LocalClassB should be returned as property colon namespace_member
                        "                    type: abc:LocalClassB\n";
        final List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
        verify(logger, times(1)).log(eq(Level.INFO), startsWith("Returning LocalClassB as OMTTokenType.NAMESPACE_MEMBER"));
    }

    @Test
    void sequenceItemAsScalarValue() {
        String contentToTest =
                "prefixes:\n" +
                        "    abc:   <http://ontology/>\n" +
                        "model:\n" +
                        "    MijnOntologie<caret>: !Ontology\n" +
                        "        prefix: abc\n" +
                        "        parameters:\n" +
                        "        -   abc:LocalClassB\n";
        final List<String> elements = getElements(contentToTest);
        assertDoesntContain(elements, "BAD_CHARACTER");
        verify(logger, times(1)).log(eq(Level.INFO), startsWith("Returning LocalClassB as OMTTokenType.NAMESPACE_MEMBER"));
    }

    private List<String> getElements(String content, int start, int end) {
        OMTLexerAdapter lexer = new OMTLexerAdapter(logger);
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
