package com.misset.opp.omt.psi.references;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.misset.opp.omt.OMTTestSuite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MemberReferenceIT extends OMTTestSuite {

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("MemberReferenceIT");
        super.setUp();
        setBuiltinAndModel();
    }

    @Test
    void testModelItemQueryInPayload() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => '';\n" +
                "       payload:\n" +
                "            payloadItem:\n" +
                "                query: myQuery\n" +
                "                list: true";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEmpty(highlighting);
    }

    @Test
    void testModelItemQueryInPayloadUnknown() {
        String content = "" +
                "model:\n" +
                "   Activiteit: !Activity\n" +
                "       queries: |\n" +
                "           DEFINE QUERY myQuery => '';\n" +
                "       payload:\n" +
                "            payloadItem:\n" +
                "                query: myUnknownQuery\n" +
                "                list: true";
        myFixture.configureByText(getFileName(), content);
        final List<HighlightInfo> highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR);
        assertEquals(1, highlighting.size());
        assertEquals("myUnknownQuery could not be resolved", highlighting.get(0).getDescription());
    }
}
