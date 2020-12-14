package com.misset.opp.omt.psi.util;

import com.misset.opp.omt.OMTTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class QueryUtilTest extends OMTTestSuite {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setName("QueryUtilTest");
        super.setUp();

        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
