package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OMTForwardSlashTest extends OMTFormattingTest {

    @BeforeAll
    @Override
    public void setUp() throws Exception {
        super.setName("OMTForwardSlashTest");
        super.setUp();
    }

    @AfterAll
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void curieConstantSpacing() {
        String content = "" +
                "queries:\n" +
                "    DEFINE QUERY query => / ont:ClassA / ont:propertyA;";
        String expected = "" +
                "queries:\n" +
                "    DEFINE QUERY query => /ont:ClassA / ont:propertyA;";
        assertFormattingApplied(content, expected);
    }

    @Test
    void stepSpacing() {
        String content = "" +
                "queries:\n" +
                "    DEFINE QUERY query => /ont:ClassA/ont:propertyA;";
        String expected = "" +
                "queries:\n" +
                "    DEFINE QUERY query => /ont:ClassA / ont:propertyA;";
        assertFormattingApplied(content, expected);
    }

}
