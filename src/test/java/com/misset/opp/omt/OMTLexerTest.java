package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.Test;
import util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


class OMTLexerTest {

    boolean printLexerLog = true;
    boolean validate = true;

    @Test
    public void testActivityWithImportsPrefixesParamsVariablesGraphsAndPayload() throws IOException {
        testOMTFile("activity_with_imports_prefixes_params_variables_graphs_payload");
        testOMTFile("activity_with_imports_prefixes_params_variables_graphs_payload");
        testOMTFile("activity_with_imports_prefixes_params_variables_graphs_payload");
    }

    @Test
    public void testActivityWithImports() throws IOException {
        testOMTFile("activity_with_imports");
    }

    @Test
    public void testActivityWithMembers() throws IOException {
        testOMTFile("activity_with_members");
    }

    @Test
    public void testActivityWithQueryWatcher() throws IOException {
        testOMTFile("activity_with_query_watcher");
    }

    @Test
    public void testActivityWithUndeclaredElements() throws IOException {
        testOMTFile("activity_with_undeclared_elements");
    }

    @Test
    public void testActivityWithVariables() throws IOException {
        testOMTFile("activity_with_variables");
    }

    @Test
    public void testActivityWithVariablesAndActions() throws IOException {
        testOMTFile("activity_with_variables_actions");
    }

    @Test
    public void testActivityWithWrongNestedAttribute() throws IOException {
        testOMTFile("activity_with_wrong_nested_attribute");
    }

    @Test
    public void testActivityWithInterpolatedStringTitle() throws IOException {
        testOMTFile("activity_with_interpolated_string_title");
    }

    @Test
    public void testLoadOntology() throws IOException {
        testOMTFile("load_ontology");
    }

    @Test
    public void testModelWithWrongModelItemType() throws IOException {
        testOMTFile("model_with_wrong_model_item_type");
    }

    @Test
    public void testProcedureWithScript() throws IOException {
        testOMTFile("procedure_with_script");
    }


    @Test
    public void testStandaloneQueryWithMissingAttribute() throws IOException {
        testOMTFile("standaloneQuery_with_missing_attribute");
    }

    @Test
    public void testProcedureWithExportingMembers() throws IOException {
        testOMTFile("frontend/libs/procedure_with_exporting_members");
    }

    @Test
    public void testExactContentMatch_ModelWithQueryAndParameter() {
        String contentToTest = "model:\n" +
                "    test: !Activity\n" +
                "        queries: |\n" +
                "            DEFINE QUERY myQuery() => 'mijn query';\n" +
                "\n" +
                "\n" +
                "        params:\n" +
                "            -   $mijnParameter";
        HashMap<String, String> parsedElementsAsTypes = getParsedElementsAsTypes(contentToTest);
        assertTrue(parsedElementsAsTypes.containsKey("params:"));
        assertEquals("OMTTokenType.PROPERTY", parsedElementsAsTypes.get("params:"));
    }

    @Test
    public void testNakedString() {
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: Doe dit, dat, enz";
        List<String> elements = getElements(contentToTest);
        assertThat(elements, not(hasItem("BAD_CHARACTER")));
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
        assertThat(elements, not(hasItem("BAD_CHARACTER")));
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
        assertThat(elements, not(hasItem("BAD_CHARACTER")));
    }

    @Test
    public void testIncompletePushbacks() {
        String contentToTest = "prefixes:\n" +
                "    pol:    <http://ontologie.politie.nl/def/politie#>\n" +
                "    rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#";
        List<String> elements = getElements(contentToTest);
        assertThat(elements, hasItem("BAD_CHARACTER"));
    }

    @Test
    public void testJdComment() throws IOException {
        String contentToTest = "/**\n" +
                "* @param $myQuery (prefix:Type)\n" +
                "*/\n";
        List<String> elements = getElements(contentToTest);
        assertThat(elements, not(hasItem("BAD_CHARACTER")));
    }

    @Test
    public void testImportModule() {
        String contentToTest = "import:\n" +
                "   module:ModuleNaam:\n" +
                "   - member\n" +
                "\n";
        List<String> elements = getElements(contentToTest);
        assertThat(elements, not(hasItem("BAD_CHARACTER")));
    }

    private void testOMTFile(String name) throws IOException {
        // This method will test an entire OMT file for identical contents with the expected outcome
        // the expected content is based on the output after parsing was finally successful. Therefore, this method is to make
        // sure any changes to the lexer won't mess this minimally parsable file
        String content = Helper.getResourceAsString(String.format("examples/%s.omt", name));
        long startTime = System.currentTimeMillis();
        String[] result = getElements(content).toArray(new String[0]);
        long endTime = System.currentTimeMillis();
        System.out.println(name + " took " + (endTime - startTime) + ", " + startTime + " to " + endTime + ", with " + result.length + " results");
        if (validate) {
            assertThat(Arrays.asList(result), not(hasItem("BAD_CHARACTER")));
        }

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

    private HashMap<String, String> getParsedElementsAsTypes(String content) {
        return getParsedElementsAsTypes(content, 0, content.length());
    }

    private HashMap<String, String> getParsedElementsAsTypes(String content, int start, int end) {
        OMTLexerAdapter lexer = new OMTLexerAdapter(false);
        lexer.start(content, start, end, 0);
        HashMap<String, String> elements = new HashMap<>();
        boolean cont = true;
        while (cont) {
            lexer.advance();
            IElementType element = lexer.getTokenType();
            if (element != null) {
                if (!element.toString().equals("WHITE_SPACE")) {
                    elements.put(lexer.getTokenText(), element.toString());
                }
            } else {
                cont = false;
            }
        }
        return elements;
    }


    @Test
    public void testSimpleContent() {
        String contentToTest = "model:\n" +
                "    Activiteit: !Activity\n" +
                "        title: 'TEST'\n" +
                "    Procedure: !Procedure\n";

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }

    @Test
    public void testImportsContent() {
        String contentToTest = "import:\n" +
                "    '@client/bedrijf/src/util/queries.omt':\n" +
                "        -   gesorteerdeBedrijven\n" +
                "        -   weergaveNaamBedrijf\n" +
                "        -   alleHoedanigheidSoorten\n" +
                "    '../utils/verzend-status.queries.omt':\n" +
                "        -   isTeVersturenNaarBvh\n" +
                "    '@client/medewerker/src/utils/medewerker.queries.omt':\n" +
                "        -   minimaalEenMedewerkerAanwezig\n" +
                "\n";

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }

    @Test
    void testScalarValue() {

        String contentToTest = "rules:\n" +
                "   beginTijdstipMoetVoorEindTijdstip:\n" +
                "       $opvolgingsDossier / pol:incident / pol:beginTijdstip / EMPTY OR\n" +
                "       $opvolgingsDossier / pol:incident / pol:eindTijdstip / EMPTY OR\n" +
                "       $opvolgingsDossier / pol:incident / pol:beginTijdstip <= $opvolgingsDossier / pol:incident / pol:eindTijdstip\n" +
                "";

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }


    @Test
    void testUnclosedDocument() {

        String contentToTest = "import:\n" +
                "    module:Registratie:\n" +
                "    -   Raadplegen\n" +
                "        "; // not ending with unindented new line

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }

    @Test
    void testJDCommentOnIndentationPlace() {
        String contentToTest = "queries:|\n" +
                "    /**\n" +
                "    * @param $param1 (string)\n" +
                "    */\n" +
                "    DEFINE QUERY myQuery($param1) => $param1;\n";
        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
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
        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }

    @Test
    void testAdjecentPrefixes() {
        String contentToTest = "prefixes:\n" +
                "    ont:     <http://ontologie> ont2: <http://ontologie2>";
        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }

}
