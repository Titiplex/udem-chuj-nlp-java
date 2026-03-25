package org.titiplex.app.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppRefreshCoordinatorTest {

    @Test
    void publishRunsSubscribedListener() {
        AppRefreshCoordinator coordinator = new AppRefreshCoordinator();
        AtomicInteger counter = new AtomicInteger();

        coordinator.subscribe(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES, counter::incrementAndGet);
        coordinator.publish(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES);

        assertEquals(1, counter.get());
    }

    @Test
    void publishDeduplicatesSameListenerAcrossTopics() {
        AppRefreshCoordinator coordinator = new AppRefreshCoordinator();
        AtomicInteger counter = new AtomicInteger();
        Runnable listener = counter::incrementAndGet;

        coordinator.subscribe(AppRefreshCoordinator.Topic.RAW_ENTRIES, listener);
        coordinator.subscribe(AppRefreshCoordinator.Topic.CORRECTED_ENTRIES, listener);

        coordinator.publish(
                AppRefreshCoordinator.Topic.RAW_ENTRIES,
                AppRefreshCoordinator.Topic.CORRECTED_ENTRIES
        );

        assertEquals(1, counter.get());
    }
}