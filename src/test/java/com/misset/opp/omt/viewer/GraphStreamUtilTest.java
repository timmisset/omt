package com.misset.opp.omt.viewer;

import com.misset.opp.omt.OMTTestSuite;
import org.graphstream.graph.Graph;
import org.graphstream.ui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.misset.opp.omt.viewer.GraphStreamUtil.generateFullGraphViewer;

class GraphStreamUtilTest extends OMTTestSuite {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setName("GraphStreamUtilTest");
        super.setUp();
        setOntologyModel();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    void generateFullGraph() {
        final Graph graph = GraphStreamUtil.generateFullGraph();
        assertTrue(graph.iterator().hasNext());
    }

    @Test
    void getViewer() {
        View view = generateFullGraphViewer().addDefaultView(false);
        assertEquals("defaultView", view.getIdView());
    }
}
