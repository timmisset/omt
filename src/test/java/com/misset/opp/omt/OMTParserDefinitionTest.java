package com.misset.opp.omt;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.misset.opp.omt.psi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class OMTParserDefinitionTest extends LightJavaCodeInsightFixtureTestCase {

    ExampleFiles exampleFiles;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTParserDefinitionTest");
        super.setUp();
        exampleFiles = new ExampleFiles(this);
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testQueryParserAsQueryPath() {
        OMTFile file = parseQuery("/pol:ClassA");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        assertNotNull(query.getValidQueryPath());
        assertNull(query.getValidBooleanStatement());
    }

    @Test
    void testQueryParserAsBooleanStatement() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        assertNull(query.getValidQueryPath());
        assertNotNull(query.getValidBooleanStatement());
    }

    @Test
    void testQueryParserAND() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA AND /pol:ClassB == /pol:ClassB");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        assertNull(query.getValidQueryPath());
        assertNotNull(query.getValidBooleanStatement());
    }

    @Test
    void testQueryParserOR() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA OR /pol:ClassB == /pol:ClassB");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        assertNull(query.getValidQueryPath());
        assertNotNull(query.getValidBooleanStatement());
    }

    @Test
    void testQueryParserFilter() {
        OMTFile file = parseQuery("/pol:ClassA [rdf:type == /pol:ClassA]");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);

        ApplicationManager.getApplication().runReadAction(() -> {
            assertNotNull(query);
        });
        // parsed as a queryPath, with the filter as nested boolean statement
        assertNotNull(query.getValidQueryPath());
        assertNull(query.getValidBooleanStatement());

        assertEquals(2, query.getValidQueryPath().getChildren().length);

        OMTQueryStep step = (OMTQueryStep) query.getValidQueryPath().getChildren()[1];
        assertNotNull(step.getQueryFilter());
        assertNotNull(step.getQueryFilter().getQuery().getValidBooleanStatement());

    }

    @Test
    void testQueryParserFilterWithAND() {
        OMTFile file = parseQuery("/pol:ClassA [rdf:type == /pol:ClassA AND pol:titel == 'test']");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        // parsed as a queryPath, with the filter as nested boolean statement
        assertNotNull(query.getValidQueryPath());
        assertNull(query.getValidBooleanStatement());

        assertEquals(2, query.getValidQueryPath().getChildren().length);

        OMTQueryStep step = (OMTQueryStep) query.getValidQueryPath().getChildren()[1];
        assertNotNull(step.getQueryFilter());
        assertNotNull(step.getQueryFilter().getQuery().getValidBooleanStatement());

    }

    @Test
    void testQueryParserFilterWithANDasOperator() {
        OMTFile file = parseQuery("/pol:ClassA [AND(rdf:type == /pol:ClassA, pol:titel == 'test')]");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertNotNull(query);
        // parsed as a queryPath, with the filter as nested boolean statement
        assertNotNull(query.getValidQueryPath());
        assertNull(query.getValidBooleanStatement());

        assertEquals(2, query.getValidQueryPath().getChildren().length);

        OMTQueryStep step = (OMTQueryStep) query.getValidQueryPath().getChildren()[1];
        assertNotNull(step.getQueryFilter());
        assertNotNull(step.getQueryFilter().getQuery().getValidQueryPath());
    }

    @Test
    void testQueryParserFilterWithNOT() {
        OMTFile file = parseQuery("/pol:ClassA [NOT rdf:type == /pol:ClassA]");
        final OMTQueryFilter filter = exampleFiles.getPsiElementFromRootDocument(OMTQueryFilter.class, file);
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTNegatedStatement negatedStatement = PsiTreeUtil.findChildOfType(filter, OMTNegatedStatement.class);
            assertNotNull(negatedStatement);
            assertEquals("rdf:type == /pol:ClassA", negatedStatement.getQuery().getText());
        });
    }


    private OMTFile parseQuery(String query) {
        String parsed = String.format("queries: |\n" +
                "    DEFINE QUERY testQuery() => %s;\n", query);
        return (OMTFile) myFixture.configureByText("test.omt", parsed);
    }
}
