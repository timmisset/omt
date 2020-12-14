package com.misset.opp.omt.formatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OMTForwardSlashTest extends OMTFormattingTest {

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setName("OMTForwardSlashTest");
        super.setUp();
    }

    @AfterEach
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
