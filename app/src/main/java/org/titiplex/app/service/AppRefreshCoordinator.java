package org.titiplex.app.service;

import org.springframework.stereotype.Service;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AppRefreshCoordinator {
    public enum Topic {
        RAW_ENTRIES,
        CORRECTED_ENTRIES,
        CORRECTION_RULES
    }

    private final Map<Topic, List<Runnable>> listeners = new EnumMap<>(Topic.class);

    public AppRefreshCoordinator() {
        for (Topic topic : Topic.values()) {
            listeners.put(topic, new CopyOnWriteArrayList<>());
        }
    }

    public void subscribe(Topic topic, Runnable listener) {
        if (topic == null || listener == null) {
            return;
        }
        listeners.get(topic).add(listener);
    }

    public void publish(Topic... topics) {
        LinkedHashSet<Runnable> toRun = new LinkedHashSet<>();
        if (topics != null) {
            for (Topic topic : topics) {
                if (topic == null) {
                    continue;
                }
                toRun.addAll(listeners.get(topic));
            }
        }

        for (Runnable runnable : toRun) {
            runOnEdt(runnable);
        }
    }

    private void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while refreshing UI.", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException("UI refresh listener failed.", cause);
        }
    }
}