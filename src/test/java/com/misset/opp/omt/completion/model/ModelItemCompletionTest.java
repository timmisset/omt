package com.misset.opp.omt.completion.model;

import com.misset.opp.omt.completion.OMTCompletionTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ModelItemCompletionTest extends OMTCompletionTestSuite {

    private static List<String> ALL_MODEL_ITEM_TYPES = Arrays.asList("!Activity", "!Component", "!Procedure", "!StandaloneQuery", "!Ontology");

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setName("ModelItemCompletionTest");
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void modelItemTypesWithoutFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: <caret>";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

    @Test
    void modelItemTypesWithExistingFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: !<caret>";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

    @Test
    void modelItemTypesWithPartial() {
        String content = "model:\n" +
                "   mijnActiviteit: !Act<caret>";
        assertCompletionSameContents(content, Collections.singletonList("!Activity"));
    }

    @Test
    void modelItemTypesWithExistingType() {
        String content = "model:\n" +
                "   mijnActiviteit: !Activity<caret>";
        assertCompletionSameContents(content, Collections.singletonList("!Activity"));
    }

    @Test
    void modelItemTypesWithExistingTypeCaretAtFlag() {
        String content = "model:\n" +
                "   mijnActiviteit: !<caret>Activity";
        assertCompletionSameContents(content, ALL_MODEL_ITEM_TYPES);
    }

}
