package com.misset.opp.omt;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.Test;
import util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

class OMTLexerTest {

    boolean printLexerLog = false;
    boolean validate = true;

    @Test
    public void testActivityWithImportsPrefixesParamsVariablesGraphsAndPayload() throws IOException {
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

    private void testOMTFile(String name) throws IOException {
        // This method will test an entire OMT file for identical contents with the expected outcome
        // the expected content is based on the output after parsing was finally successful. Therefore, this method is to make
        // sure any changes to the lexer won't mess this minimally parsable file
        String content = Helper.getResourceAsString(String.format("examples/%s.omt", name));
        String[] result = getElements(content).toArray(new String[0]);

        if (validate) {
            assertThat(Arrays.asList(result), not(hasItem("BAD_CHARACTER")));
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
                if (!element.toString().equals("WHITE_SPACE")) {
                    elements.add(element.toString());
                }
            } else {
                cont = false;
            }
        }
        return elements;
    }


    @Test
    public void testSpecificContent() throws IOException {
        printLexerLog = true;
        String contentToTest = "model:\n" +
                "    MijnActiviteit: !Activity\n" +
                "        title: Mijn titel = ${$mijnVariable}\n" +
                "\n" +
                "        variables:\n" +
                "        -   $mijnVariable\n";

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }
}
