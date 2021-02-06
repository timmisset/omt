package com.misset.opp.omt;

import com.intellij.openapi.application.ReadAction;
import com.misset.opp.omt.psi.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTParserDefinitionTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("OMTParserDefinitionTest");
        super.setUp();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testQueryParserAsQueryPath() {
        setQuery("/pol:ClassA");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTQueryPath);
    }

    @Test
    void testQueryParserAsBooleanStatement() {
        setQuery("/pol:ClassA == /pol:ClassA");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTEquationStatement);
    }

    @Test
    void testQueryParserAND() {
        setQuery("/pol:ClassA == /pol:ClassA AND /pol:ClassB == /pol:ClassB");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTBooleanStatement);
    }

    @Test
    void testQueryParserOR() {
        setQuery("/pol:ClassA == /pol:ClassA OR /pol:ClassB == /pol:ClassB");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTBooleanStatement);
    }

    @Test
    void testQueryParserFilterIsBooleanType() {
        setQuery("/pol:ClassA [rdf:type == /pol:ClassA]");
        final OMTQuery query = getElement(OMTQuery.class);

        assertTrue(query instanceof OMTQueryPath);
        OMTQueryPath queryPath = (OMTQueryPath) query;
        assertEquals(1, queryPath.getQueryStepList().size());

        OMTQueryStep step = queryPath.getQueryStepList().get(0);
        assertEquals(1, step.getQueryFilterList().size());
        assertTrue(step.getQueryFilterList().get(0).getQuery().isBooleanType());

    }

    @Test
    void testQueryParserFilterWithANDIsBooleanType() {
        setQuery("/pol:ClassA [rdf:type == /pol:ClassA AND pol:titel == 'test']");
        final OMTQueryFilter filter = getElement(OMTQueryFilter.class);
        assertTrue(filter.getQuery().isBooleanType());
    }

    @Test
    void testQueryParserFilterWithANDasOperator() {
        setQuery("/pol:ClassA [AND(rdf:type == /pol:ClassA, pol:titel == 'test')]");
        final OMTQueryFilter filter = getElement(OMTQueryFilter.class);
        assertTrue(filter.getQuery() instanceof OMTQueryPath);
        // TODO: when the isBooleanType includes resolving the queryPath to a type, validate that it is booleanType
    }

    @Test
    void testQueryParserFilterWithNOTLeading() {
        setQuery("/pol:ClassA [NOT rdf:type == /pol:ClassA]");
        final OMTQueryFilter filter = getElement(OMTQueryFilter.class);
        assertTrue(filter.getQuery().isBooleanType());
    }

    @Test
    void testQueryParserFilterWithNOTTrailing() {
        setQuery("/pol:ClassA / someMethod() / NOT");
        ReadAction.run(() -> {
            final OMTQuery query = getElement(OMTQuery.class);
            assertTrue(query.isBooleanType());
        });
    }

    @Test
    void testQueryParserArray() {
        // the left-side of the array token is considered a regular path that is resolved directly in the query as steps
        // the right-side of the array and all subsequent array delimiters are new OMTQuery instances
        setQuery("/pol:ClassA | /pol:ClassB");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTQueryArray);
        assertEquals(2, ((OMTQueryArray) query).getQueryList().size());
    }

    @Test
    void testQueryParserArrayWith3Parts() {
        // the left-side of the array token is considered a regular path that is resolved directly in the query as steps
        // the right-side of the array and all subsequent array delimiters are new OMTQuery instances
        setQuery("/pol:ClassA | /pol:ClassB | /pol:ClassC");
        final OMTQuery query = getElement(OMTQuery.class);
        assertTrue(query instanceof OMTQueryArray);
        assertEquals(3, ((OMTQueryArray) query).getQueryList().size());
    }

    private void setQuery(String query) {
        String parsed = String.format("queries: |\n" +
                "    DEFINE QUERY testQuery() => %s;\n", query);
        myFixture.configureByText("test.omt", parsed);
    }
}
