package com.misset.opp.omt.viewer;

import com.intellij.openapi.wm.ToolWindow;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.misset.opp.omt.psi.util.UtilManager.getProjectUtil;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;

public class GraphToolWindow {
    private ViewPanel view;
    private JPanel graphPanel;
    private JPanel toolbar;
    private JComboBox<String> cbClasses;
    private JPanel main;
    private JButton resetViewButton;
    private JSpinner zoomSpinner;
    private Graph graph;
    private GraphicGraph graphicGraph;
    private boolean isLoadingClasses = false;
    private BiConsumer<String, String> eventChange = (id, event) -> {
        cbClasses.setSelectedItem(id);
    };

    public GraphToolWindow(ToolWindow toolWindow) {
        graphPanel.add(getView().orElse(isLoadingPanel()));

        zoomSpinner.setValue(1);
        zoomSpinner.addChangeListener(e -> updateZoom());
        resetViewButton.addActionListener(e -> reset());
        cbClasses.addActionListener(e -> focusOnSelectedClass());

        getProjectUtil().addModelChangeListener(
                rdfModelUtil -> {
                    graphPanel.removeAll();
                    graphPanel.add(getView().orElse(isLoadingPanel()));
                    cbClasses.removeAllItems();
                    rdfModelUtil.getAllClasses().forEach(resource -> cbClasses.addItem(resource.getURI()));
                }
        );
    }

    private void updateZoom() {
        final int value = (Integer) zoomSpinner.getValue();
        final int minValue = Math.max(1, value);
        zoomSpinner.setValue(minValue);
        view.getCamera().setViewPercent(1.0 / value);
    }

    private void reset() {
        view.getCamera().resetView();
    }

    private void focusOnSelectedClass() {
        final List<Edge> edges = graphicGraph.edges().collect(Collectors.toUnmodifiableList());
        edges.forEach(edge -> removeClass(edge, "focused"));
        final Node node = graphicGraph.getNode((String) cbClasses.getSelectedItem());

        if (node != null) {
            final double[] nodePosition = nodePosition(node);
            view.getCamera().setViewCenter(nodePosition[0], nodePosition[1], 0);
            final List<Edge> nodeEdges = node.edges().collect(Collectors.toUnmodifiableList());
            nodeEdges.forEach(edge -> addClass(graphicGraph.getEdge(edge.getId()), "focused"));
        }
    }

    private void addClass(Element element, String className) {
        final ArrayList<String> classes = new ArrayList<>(getClasses(element));
        if (!classes.contains(className)) {
            classes.add(className);
        }
        setClasses(element, classes);
    }

    private void removeClass(Element element, String className) {
        final ArrayList<String> classes = new ArrayList<>(getClasses(element));
        classes.remove(className);
        setClasses(element, classes);
    }

    private void setClasses(Element element, List<String> classes) {
        element.setAttribute("ui.class", String.join(", ", classes));
    }

    private java.util.List<String> getClasses(Element element) {
        String currentClasses = (String) element.getAttribute("ui.class");
        currentClasses = currentClasses != null ? currentClasses : "";
        return Arrays.asList(currentClasses.split(", "));
    }

    private Optional<JComponent> getView() {
        if (view != null) {
            return Optional.of(view);
        }

        graph = GraphStreamUtil.generateFullGraph();
        final Viewer viewer = GraphStreamUtil.getGraphViewer(graph);

        if (viewer == null) {
            return Optional.empty();
        }

        viewer.enableAutoLayout();
        graphicGraph = viewer.getGraphicGraph();

        view = (ViewPanel) viewer.addDefaultView(false);
        view.enableMouseOptions();

        Dimension dimension = new Dimension(600, 600);
        view.setSize(dimension);
        view.setMinimumSize(dimension);
        view.setPreferredSize(dimension);

        GraphListener.register(viewer, eventChange);

        return Optional.of(view);
    }

    private JLabel isLoadingPanel() {
        return new JLabel("Still loading, ontology will be displayed when indexing is done and ontology is loaded");
    }

    public JPanel getContent() {
        return main;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
