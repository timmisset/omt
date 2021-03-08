package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTDefineName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DefinedStatementUsageIT extends ReferenceTest {

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("DefinedStatementUsageIT");
        super.setUp(OMTDefineName.class);
        setBuiltinAndModel();
    }

    @Test
    void testQueryDefinedInScriptHasUsageWhenOverShadowingModelItemQuery() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |" +
                "           DEFINE QUERY query => 'myQuery';\n" +
                "       onStart: |\n" +
                "           DEFINE QUERY qu<caret>ery => 'myQuery';\n" +
                "           @LOG(query);";
        assertHasUsages(content, 1);
    }

    @Test
    void testQueryDefinedInModelItemHasNoUsageWhenOvershadowedByScriptQuery() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |" +
                "           DEFINE QUERY qu<caret>ery => 'myQuery';\n" +
                "       onStart: |\n" +
                "           DEFINE QUERY query => 'myQuery';\n" +
                "           @LOG(query);";
        assertHasUsages(content, 0);
    }

}
