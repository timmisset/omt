package com.misset.opp.omt.psi.impl;

import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTModelItemBlock;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class OMTLocalOntologyTest extends OMTTestSuite {

    private static String defaultContent =
            "prefixes:\n" +
                    "    abc:   <http://ontology/>\n" +
                    "model:\n" +
                    "    MijnOntologie<caret>: !Ontology\n" +
                    "        prefix: abc\n" +
                    "        classes:\n" +
                    "        -   id: LocalClassA\n" +
                    "            properties:\n" +
                    "                propA: integer\n" +
                    "                propB: string\n" +
                    "                propC:\n" +
                    "                    type: abc:LocalClassB\n" +
                    "\n" +
                    "        -   id: LocalClassB\n" +
                    "            properties:\n" +
                    "                propD: string\n" +
                    "                propE: date";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testLocalOntologyGetPrefix() {
        getElementAtCaret(defaultContent, modelItemBlock -> {
            final OMTLocalOntologyImpl omtLocalOntology = new OMTLocalOntologyImpl(modelItemBlock);
            assertEquals("abc", omtLocalOntology.getPrefix());
        }, OMTModelItemBlock.class, true);
    }

    @Test
    void testLocalOntologyGetClasses() {
        getElementAtCaret(defaultContent, modelItemBlock -> {
            final OMTLocalOntologyImpl omtLocalOntology = new OMTLocalOntologyImpl(modelItemBlock);
            final List<Resource> classes = omtLocalOntology.getClasses();
            assertEquals(classes.size(), 2);
        }, OMTModelItemBlock.class, true);
    }

    @Test
    void testLocalOntologyGetStatements() {
        getElementAtCaret(defaultContent, modelItemBlock -> {
            final OMTLocalOntologyImpl omtLocalOntology = new OMTLocalOntologyImpl(modelItemBlock);
            final List<Statement> statements = omtLocalOntology.getStatements();
            assertEquals(statements.size(), 2);
        }, OMTModelItemBlock.class, true);
    }

}
