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
        String contentToTest = "import:\n" +
                "    \"@client/melding/src/melding.queries.omt\":\n" +
                "    -   kladblokRegels\n" +
                "    \"@client/instellingen/src/voorkeur.queries.omt\":\n" +
                "    -   gmsParsertermenTonen\n" +
                "    ../activiteit-starten/activiteit-starten.activity.omt:\n" +
                "    -   ActiviteitStarten\n" +
                "    ../utils/verzend-status.queries.omt:\n" +
                "    -   isTeVersturenNaarBvh\n" +
                "    -   VerzendStatus\n" +
                "\n" +
                "prefixes:\n" +
                "    pol:     <http://ontologie.politie.nl/def/politie#>\n" +
                "    gms:    <http://ontologie.politie.nl/def/gms#>\n" +
                "    rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "    xsd:    <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "queries: |\n" +
                "    DEFINE QUERY schermTitel($opvolgingsDossier) => $opvolgingsDossier / pol:incident / pol:incidentSoort / pol:omschrijving;\n" +
                "    DEFINE QUERY signaalDossier($opvolgingsDossier) => $opvolgingsDossier / pol:signaal [ rdf:type == /pol:MeldkamerSignaalDossier ];\n" +
                "    DEFINE QUERY signaal($opvolgingsDossier) => signaalDossier($opvolgingsDossier) / ^pol:signaalDossier / ORDER_BY(pol:tijdstip) / PICK(0);\n" +
                "    DEFINE QUERY registratieOpgenomenInBvhRegel($opvolgingsDossier) =>\n" +
                "            $opvolgingsDossier / pol:verzendStatus [pol:verzendStatusSoort == /pol:VerzendStatusSoort_OvergezetNaarBVH]\n" +
                "            / CHOOSE\n" +
                "                WHEN EXISTS => $opvolgingsDossier / pol:verzendStatus [pol:verzendStatusSoort == /pol:VerzendStatusSoort_TeVerzendenNaarBVH] /\n" +
                "                        ORDER_BY(pol:beginTijdstip, true)/ PICK(0)\n" +
                "                OTHERWISE => null\n" +
                "            END ;\n" +
                "    DEFINE QUERY tijdlijnRegels($opvolgingsDossier) =>\n" +
                "            (kladblokRegels(signaal($opvolgingsDossier))\n" +
                "                / MAP(`{\"type\": \"gms-regel\",\n" +
                "                        \"tijdstip\": \"${gms:tijdstip}\",\n" +
                "                        \"zender\": \"GMS\",\n" +
                "                        \"inhoud\": \"${gms:inhoud}\"\n" +
                "                    }`) / CAST(JSON)\n" +
                "            )\n" +
                "            |\n" +
                "            (registratieOpgenomenInBvhRegel($opvolgingsDossier)\n" +
                "                / MAP(`{\"type\": \"bvh-regel\",\n" +
                "                        \"titel\": \"Overgezet naar BVH\",\n" +
                "                        \"tijdstip\": \"${pol:beginTijdstip}\",\n" +
                "                        ${pol:aangemaaktDoor / CAST(/xsd:string) / CONCAT('\"door\": \"', ., '\"')}\n" +
                "                    }`) / CAST(JSON)\n" +
                "            );\n" +
                "\n" +
                "model:\n" +
                "    Raadplegen: !Activity\n" +
                "        title: $title\n" +
                "\n" +
                "        params:\n" +
                "        -   $opvolgingsDossier (pol:OpvolgingsDossier)\n" +
                "\n" +
                "        graphs:\n" +
                "            live:\n" +
                "                -   $opvolgingsDossier / GRAPH\n" +
                "                -   $opvolgingsDossier / pol:incident / pol:incidentSoort / GRAPH\n" +
                "                -   $opvolgingsDossier / pol:signaal / GRAPH\n" +
                "                -   signaalDossier($opvolgingsDossier) /  pol:incident / pol:incidentSoort / GRAPH\n" +
                "\n" +
                "        variables:\n" +
                "            -   $title = schermTitel($opvolgingsDossier)\n" +
                "\n" +
                "        watchers:\n" +
                "            -   query: schermTitel($opvolgingsDossier)\n" +
                "                onChange: |\n" +
                "                    $title = schermTitel($opvolgingsDossier);\n" +
                "\n" +
                "        payload:\n" +
                "            dossier: $opvolgingsDossier\n" +
                "            opvolgingsDossier: $opvolgingsDossier\n" +
                "            tijdlijnRegels:\n" +
                "                # De tijdlijn-component verwacht een array van JSON-objecten met de attributen:\n" +
                "                #     - type (string) , wordt gebruikt in de data-cy\n" +
                "                #     - tijdstip (dateTime geconverteerd naar string)\n" +
                "                #     - zender (optioneel, degene die de regel veroorzaakt, b.v. 'GMS')\n" +
                "                #     - inhoud (string)\n" +
                "                # De component heeft geen kennis van hoe de informatie verzameld wordt.\n" +
                "                # De component weet hoe deze weer te geven\n" +
                "                value: tijdlijnRegels($opvolgingsDossier)\n" +
                "                list: true\n" +
                "\n" +
                "        actions:\n" +
                "            activiteitStarten:\n" +
                "                precondition: isTeVersturenNaarBvh($opvolgingsDossier)\n" +
                "                onSelect: |\n" +
                "                    @ActiviteitStarten($opvolgingsDossier);\n" +
                "\n";

        System.out.println(
                String.join("\n", getElements(contentToTest))
        );
    }
}
