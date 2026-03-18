package org.titiplex.desktop.ui.common;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class BackgroundTasks {
    private BackgroundTasks() {
    }

    public static <T> void run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return work.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (Throwable throwable) {
                    onError.accept(throwable);
                }
            }
        };
        worker.execute();
    }
}
