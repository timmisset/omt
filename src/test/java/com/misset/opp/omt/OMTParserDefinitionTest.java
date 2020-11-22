package com.misset.opp.omt;

import com.intellij.openapi.application.ApplicationManager;
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
        exampleFiles = new ExampleFiles(this, myFixture);
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
        assertTrue(query instanceof OMTQueryPath);
    }

    @Test
    void testQueryParserAsBooleanStatement() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertTrue(query instanceof OMTEquationStatement);
    }

    @Test
    void testQueryParserAND() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA AND /pol:ClassB == /pol:ClassB");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertTrue(query instanceof OMTBooleanStatement);
    }

    @Test
    void testQueryParserOR() {
        OMTFile file = parseQuery("/pol:ClassA == /pol:ClassA OR /pol:ClassB == /pol:ClassB");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertTrue(query instanceof OMTBooleanStatement);
    }

    @Test
    void testQueryParserFilterIsBooleanType() {
        OMTFile file = parseQuery("/pol:ClassA [rdf:type == /pol:ClassA]");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);

        assertTrue(query instanceof OMTQueryPath);
        OMTQueryPath queryPath = (OMTQueryPath) query;
        assertEquals(1, queryPath.getQueryStepList().size());

        OMTQueryStep step = queryPath.getQueryStepList().get(0);
        assertEquals(1, step.getQueryFilterList().size());
        assertTrue(step.getQueryFilterList().get(0).getQuery().isBooleanType());

    }

    @Test
    void testQueryParserFilterWithANDIsBooleanType() {
        OMTFile file = parseQuery("/pol:ClassA [rdf:type == /pol:ClassA AND pol:titel == 'test']");
        final OMTQueryFilter filter = exampleFiles.getPsiElementFromRootDocument(OMTQueryFilter.class, file);
        assertTrue(filter.getQuery().isBooleanType());
    }

    @Test
    void testQueryParserFilterWithANDasOperator() {
        OMTFile file = parseQuery("/pol:ClassA [AND(rdf:type == /pol:ClassA, pol:titel == 'test')]");
        final OMTQueryFilter filter = exampleFiles.getPsiElementFromRootDocument(OMTQueryFilter.class, file);
        assertTrue(filter.getQuery() instanceof OMTQueryPath);
        // TODO: when the isBooleanType includes resolving the queryPath to a type, validate that it is booleanType
    }

    @Test
    void testQueryParserFilterWithNOTLeading() {
        OMTFile file = parseQuery("/pol:ClassA [NOT rdf:type == /pol:ClassA]");
        final OMTQueryFilter filter = exampleFiles.getPsiElementFromRootDocument(OMTQueryFilter.class, file);
        assertTrue(filter.getQuery().isBooleanType());
    }

    @Test
    void testQueryParserFilterWithNOTTrailing() {
        OMTFile file = parseQuery("/pol:ClassA / someMethod() / NOT");
        ApplicationManager.getApplication().runReadAction(() -> {
            final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
            assertTrue(query.isBooleanType());
        });
    }

    @Test
    void testQueryParserArray() {
        // the left-side of the array token is considered a regular path that is resolved directly in the query as steps
        // the right-side of the array and all subsequent array delimiters are new OMTQuery instances
        OMTFile file = parseQuery("/pol:ClassA | /pol:ClassB");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertTrue(query instanceof OMTQueryArray);
        assertEquals(2, ((OMTQueryArray) query).getQueryList().size());
    }

    @Test
    void testQueryParserArrayWith3Parts() {
        // the left-side of the array token is considered a regular path that is resolved directly in the query as steps
        // the right-side of the array and all subsequent array delimiters are new OMTQuery instances
        OMTFile file = parseQuery("/pol:ClassA | /pol:ClassB | /pol:ClassC");
        final OMTQuery query = exampleFiles.getPsiElementFromRootDocument(OMTQuery.class, file);
        assertTrue(query instanceof OMTQueryArray);
        assertEquals(2, ((OMTQueryArray) query).getQueryList().size());
    }


    private OMTFile parseQuery(String query) {
        String parsed = String.format("queries: |\n" +
                "    DEFINE QUERY testQuery() => %s;\n", query);
        return (OMTFile) myFixture.configureByText("test.omt", parsed);
    }
}
