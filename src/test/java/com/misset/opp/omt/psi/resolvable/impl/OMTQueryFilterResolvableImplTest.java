package com.misset.opp.omt.psi.resolvable.impl;

import com.misset.opp.omt.OMTTestSuite;
import com.misset.opp.omt.psi.OMTQueryFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OMTQueryFilterResolvableImplTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        setBuiltinAndModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void testFilterIsSubSelectionWhen2Values() {
        String content = "queries: |" +
                "DEFINE QUERY query => $something[<caret>0, 1]";
        getElementAtCaret(content, element -> assertTrue(element.isSubSelection()), OMTQueryFilter.class, true);
    }

    @Test
    void testFilterIsSubSelectionWhen1NumericValue() {
        String content = "queries: |" +
                "DEFINE QUERY query => $something[<caret>1]";
        getElementAtCaret(content, element -> assertTrue(element.isSubSelection()), OMTQueryFilter.class, true);
    }

    @Test
    void testFilterIsNotSubSelectionWhen1NonNumericValue() {
        String content = "queries: |" +
                "DEFINE QUERY query => $something[<caret>filterLogic]";
        getElementAtCaret(content, element -> assertFalse(element.isSubSelection()), OMTQueryFilter.class, true);
    }
}
