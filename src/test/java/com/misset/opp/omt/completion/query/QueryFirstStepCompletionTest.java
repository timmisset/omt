package com.misset.opp.omt.completion.query;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class QueryFirstStepCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("QueryFirstStepCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void firstQueryStep() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery => <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertCompletionContainsClasses(completionLookupElements);
        assertCompletionContainsGlobalVariables(completionLookupElements);
        assertCompletionContainsBuiltinOperators(completionLookupElements);
    }

    @Test
    void firstQueryStepHasInputParameter() {
        String content = "queries: |\n" +
                "   DEFINE QUERY myQuery($myParam) => <caret>";
        assertCompletionContains(content, "$myParam");
    }

    @Test
    void firstQueryStepHasModelParameterAndVariable() {
        String content = "model:\n" +
                "   MijnActiviteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => <caret>;\n" +
                "       params:\n" +
                "           - $myParam\n" +
                "       variables:\n" +
                "           - $myVariable\n";
        assertCompletionContains(content, "$myParam", "$myVariable");
    }

    @Test
    void sequencePreviousQuery() {
        String content = "queries: |\n" +
                "   DEFINE QUERY previousQuery => 'I should be suggested';\n" +
                "   DEFINE QUERY query => <caret>;\n" +
                "   DEFINE QUERY nextQuery => 'I should NOT be suggested';\n";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "previousQuery");
        assertDoesntContain(completionLookupElements, "nextQuery");
    }
}

