package com.misset.opp.omt.psi.references;

import com.misset.opp.omt.psi.OMTPropertyLabel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PropertyLabelReferenceIT extends ReferenceTest {

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setName("PropertyLabelReferenceIT");
        super.setUp(OMTPropertyLabel.class);
        setOntologyModel();
    }

    @Override
    @AfterAll
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void findUsageInOtherFile() {
        String content = "" +
                "model:\n" +
                "   Activiteit<caret>: !Activity";
        String usageFile = "" +
                "import:\n" +
                "   ./activityFile.omt:\n" +
                "       - Activiteit\n" +
                "\n" +
                "model:\n" +
                "   Activiteit2: !Activity\n" +
                "       onStart: |\n" +
                "           @Activiteit();";
        addFile("usageFile.omt", usageFile);
        assertHasUsages("activityFile.omt", content, 2);
    }

}
