package com.misset.opp.omt.viewer;

import com.intellij.openapi.application.ApplicationManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * The GraphListener is running in a separate Thread and is used to "pipe" the Events from the visual graph
 * running in another Thread to the UI.
 * In order to gather events in the visual graph, the pump command is called in this non-UI thread
 * and will pass it to the BiConsumer
 */
public class GraphListener implements ViewerListener, Runnable {

    private static List<GraphListener> listeners = new ArrayList<>();
    private final ViewerPipe pipe;
    private final BiConsumer<String, String> eventChange;
    private boolean pump = true;

    public GraphListener(Viewer viewer, BiConsumer<String, String> eventChange) {
        this.eventChange = eventChange;
        pipe = viewer.newViewerPipe();
        pipe.addViewerListener(this);
    }

    public static void register(Viewer viewer, BiConsumer<String, String> eventChange) {
        final GraphListener graphListener = new GraphListener(viewer, eventChange);
        Thread thread = new Thread(graphListener);
        listeners.add(graphListener);
        thread.start();
    }

    public static void stopAll() {
        listeners.forEach(
                GraphListener::doStop
        );
    }

    public void viewClosed(String id) {
        pump = false;
    }

    public void buttonPushed(String id) {
        ApplicationManager.getApplication().invokeAndWait(
                () -> eventChange.accept(id, "button.pushed")
        );
    }

    public void buttonReleased(String id) {
        // ignore
    }

    public void mouseOver(String id) {
        // ignore
    }

    public void mouseLeft(String id) {
        // ignore
    }

    public void pump() {
        try {
            pipe.blockingPump(40);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    public void doStop() {
        pump = false;
    }

    @Override
    public void run() {
        while (pump) {
            pump();
        }
    }
}
