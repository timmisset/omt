package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ScalarValueCompletionTest extends OMTCompletionTestSuite {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("ScalarValueCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void modelNewEntry() {
        setReasons();
        String content = "model:\n" +
                "   mijnActiviteit: !Activity\n" +
                "       reason: <caret>";
        final List<String> completionLookupElements = getCompletionLookupElements(content);
        assertContainsElements(completionLookupElements, "Naam", "Naam2");
    }

}

