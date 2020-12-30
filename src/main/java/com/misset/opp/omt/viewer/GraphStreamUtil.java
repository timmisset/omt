package com.misset.opp.omt.viewer;

import com.intellij.util.ui.UIUtil;
import com.misset.opp.omt.util.RDFModelUtil;
import org.apache.jena.rdf.model.Resource;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;
import static com.misset.opp.omt.psi.util.UtilManager.getRDFModelUtil;
import static util.Helper.getResources;

public class GraphStreamUtil {

    public static Graph generateFullGraph() {
        System.setProperty("org.graphstream.ui", "swing");

        if (getProjectUtil().getOntologyModel() == null) {
            return null;
        } // not loaded yet
        Graph graph = new MultiGraph("ontology");
        final RDFModelUtil rdfModelUtil = getRDFModelUtil();

        final List<Resource> allClasses = rdfModelUtil.getAllClasses();
        // add all the classes to this graph
        allClasses.forEach(resource -> {
            final Node node = graph.addNode(resource.toString());
            node.setAttribute("ui.label", resource.getLocalName());
        });

        allClasses.forEach(
                className -> {
                    // create any subClassOf edges that might exist for this class
                    rdfModelUtil.getParents(className).forEach(
                            // add the subClass
                            superClass -> {
                                if (graph.getNode(superClass.getURI()) != null) {
                                    final Edge edge = graph.addEdge(className.getURI() + "." + superClass.getURI(), className.getURI(), superClass.getURI(), true);
                                    edge.setAttribute("ui.class", "subClass");
                                }
                            }
                    );
                    // and finally, all predicates to other classes:
                    rdfModelUtil.listSubClassOwnPredicates(className).forEach(
                            predicate -> rdfModelUtil.getPredicateObjects(predicate, false).forEach(
                                    object -> {
                                        if (graph.getNode(object.getURI()) != null) {
                                            // add edges to existing nodes only
                                            final Edge edge = graph.addEdge(className.getURI() + "." + predicate.getURI() + "." + object.getURI(), className.getURI(), object.getURI(), true);
                                            edge.setAttribute("ui.label", predicate.getLocalName());
                                        }
                                    }
                            )
                    );
                }
        );

        setLookAndFeel(graph);

        return graph;
    }

    public static Viewer generateFullGraphViewer() {
        final Graph graph = GraphStreamUtil.generateFullGraph();
        return getGraphViewer(graph);
    }

    public static SwingViewer getGraphViewer(Graph graph) {
        if (graph == null) {
            return null;
        }
        return new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
    }

    private static void setLookAndFeel(Graph graph) {
        String styleSheet = getResources(Collections.singletonList("graphstream.css"), "").get(0);
//        final EditorColorsScheme schemeForCurrentUITheme = EditorColorsManager.getInstance().getSchemeForCurrentUITheme();

        styleSheet = styleSheet.replace("GRAPH_BACKGROUND_COLOR", toHexaColor(UIUtil.getPanelBackground()));
        styleSheet = styleSheet.replace("TEXT_COLOR", toHexaColor(UIUtil.getTextAreaForeground()));
        styleSheet = styleSheet.replace("NODE_COLOR", toHexaColor(UIUtil.getFocusedFillColor()));
//        styleSheet = styleSheet.replace("SELECTED_NODE_COLOR", toHexaColor(UIUtil.getButtonSelectColor()));

        graph.setAttribute("ui.stylesheet", styleSheet);
    }

    private static String toHexaColor(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }
}
