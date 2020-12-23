package com.misset.opp.omt.completion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ParameterTypeCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("ParameterTypeCompletionTest");
        super.setUp();
    }

    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void parameterTypeInParameter() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "       - $paramA (<caret>)";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
    }

    @Test
    void parameterTypeInParameterPrefixKnown() {
        String content = "model:\n" +
                "   Activiteit: !Activity\n" +
                "       params:\n" +
                "       - $paramA (ont:<caret>)";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
    }

    @Test
    void parameterTypeInParameterAnnotation() {
        String content = "/**\n" +
                " * @param $myParam (<caret>)\n" +
                " */\n" +
                "queries:|\n" +
                "   DEFINE QUERY query($myParam) => 'test';";

        final List<String> completionLookupElements = getCompletionLookupElements(withPrefixes(content));
        assertCompletionContainsClasses(completionLookupElements, false);
    }

}
