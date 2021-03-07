package com.misset.opp.omt;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.misset.opp.omt.psi.OMTBooleanStatement;
import com.misset.opp.omt.psi.OMTEquationStatement;
import com.misset.opp.omt.psi.OMTIgnored;
import com.misset.opp.omt.psi.OMTQuery;
import com.misset.opp.omt.psi.OMTQueryArray;
import com.misset.opp.omt.psi.OMTQueryFilter;
import com.misset.opp.omt.psi.OMTQueryPath;
import com.misset.opp.omt.psi.OMTQueryStep;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collection;
import java.util.Objects;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTParserDefinitionTest extends OMTTestSuite {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("OMTParserDefinitionTest");
        super.setUp();
    }

    @Override
    @AfterAll
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

    @Test
    void testCommentBlockParserMultilineWithLineStarter() {
        String content = "/*\n" +
                " * Test\n" +
                " */";
        myFixture.configureByText(getFileName(), content);
        assertTrue(ReadAction.compute(() -> Objects.requireNonNull(PsiTreeUtil.findChildOfType(getFile(), PsiComment.class)).getTokenType() == OMTIgnored.MULTILINE_COMMENT));
    }

    @Test
    void testCommentBlockParserMultilineWithoutLineStarter() {
        String content = "/*\n" +
                " Test\n" +
                " */";
        myFixture.configureByText(getFileName(), content);
        assertTrue(ReadAction.compute(() -> Objects.requireNonNull(PsiTreeUtil.findChildOfType(getFile(), PsiComment.class)).getTokenType() == OMTIgnored.MULTILINE_COMMENT));
    }

    @Test
    void testCommentBlockParserMultiline() {
        String commentBlock = "" +
                "    /*\n" +
                "     * Inside the comment someone decides to use an asterix (*)\n" +
                "     * which should not bother the parsing\n" +
                "     * neither should using a forward slash like so: /.\n" +
                "     \n" +
                "      And lines without an asterix should still be parsed correctly.\n" +
                "     */";
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery => . ;\n" +
                "\n" +
                commentBlock +
                "     DEFINE QUERY mySecondQuery => . ;";
        myFixture.configureByText(getFileName(), content);
        ReadAction.run(() -> {
            final PsiComment comment = PsiTreeUtil.findChildOfType(getFile(), PsiComment.class);
            assertNotNull(comment);
            assertEquals(OMTIgnored.MULTILINE_COMMENT, comment.getTokenType());
            assertEquals(commentBlock.trim(), comment.getText()); // the text should be parsed as a single comment block
        });
    }

    @Test
    void testCommentBlockParserMultilineMultipleBlocks() {
        String content = "queries: |\n" +
                "    DEFINE QUERY myQuery => . ;\n" +
                "\n" +
                "    /*\n" +
                "     * Inside the comment someone decides to use an asterix (*)\n" +
                "     * which should not bother the parsing\n" +
                "     * neither should using a forward slash like so: /.\n" +
                "     */\n" +
                "     DEFINE QUERY mySecondQuery => . ;" +
                "     /*\n" +
                "      And lines without an asterix should still be parsed correctly.\n" +
                "     */\n" +
                "     DEFINE QUERY myThirdQuery => . ;";
        myFixture.configureByText(getFileName(), content);
        ReadAction.run(() -> {
            final Collection<PsiComment> comments = PsiTreeUtil.findChildrenOfType(getFile(), PsiComment.class);
            assertNotEmpty(comments);
            assertEquals(2, comments.size());
            comments.forEach(
                    comment -> assertEquals(OMTIgnored.MULTILINE_COMMENT, comment.getTokenType())
            );
        });
    }

    private void setQuery(String query) {
        String parsed = String.format("queries: |\n" +
                "    DEFINE QUERY testQuery() => %s;\n", query);
        myFixture.configureByText(getFileName(), parsed);
    }
}
