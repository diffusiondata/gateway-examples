package com.diffusiondata.pretend.example.activity.feed.service.impl;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.diffusiondata.pretend.example.activity.feed.service.ActivityFeedServer;

import net.datafaker.Faker;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class PretendActivityFeedServerImpl
    implements ActivityFeedServer, Runnable {

    private static final Logger LOG =
        LoggerFactory.getLogger(PretendActivityFeedServerImpl.class);

    private final Random random = new Random();

    @GuardedBy("this")
    private final Map<String, ActivityFeedListener> listeners =
        new HashMap<>();

    private final ConcurrentMap<String, Activity> cachedLatestActivities =
        new ConcurrentHashMap<>();

    private final Supplier<Activity> activityGeneratorSupplier;
    private final int maxSleepMillisBetweenActivityGeneration;

    private PretendActivityFeedServerImpl(
        Supplier<Activity> activityGeneratorSupplier,
        int maxSleepMillisBetweenActivityGeneration) {

        this.activityGeneratorSupplier = activityGeneratorSupplier;
        this.maxSleepMillisBetweenActivityGeneration =
            Math.max(maxSleepMillisBetweenActivityGeneration, 1);
    }

    @Override
    public synchronized String registerClientListener(
        ActivityFeedListener activityFeedListener) {

        requireNonNull(activityFeedListener, "activityFeedListener");

        final String listenerIdentifier = UUID.randomUUID().toString();

        listeners.put(listenerIdentifier, activityFeedListener);

        LOG.info("Registered client listener: '{}'", listenerIdentifier);

        return listenerIdentifier;
    }

    @Override
    public synchronized boolean unregisterClientListener(
        String listenerIdentifier) {

        requireNonNull(listenerIdentifier, "listenerIdentifier");

        if (!listeners.containsKey(listenerIdentifier)) {
            LOG.warn("Cannot unregister listener with unknown identifier: '{}'",
                listenerIdentifier);

            return false;
        }

        listeners.remove(listenerIdentifier);

        LOG.info("Unregistered client listener: '{}'", listenerIdentifier);

        return true;
    }

    @Override
    public Collection<Activity> getLatestActivities() {
        return unmodifiableCollection(cachedLatestActivities.values());
    }

    @Override
    public void run() {
        LOG.info("Started activity feed server");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                runOnce();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * package for tests.
     */
    Map<String, ActivityFeedListener> getListeners() {
        return unmodifiableMap(listeners);
    }

    /**
     * package for tests.
     */
    void runOnce()
        throws InterruptedException {

        final Activity activity = activityGeneratorSupplier.get();

        internalUpdateStateAndListeners(activity);

        final int sleepDurationMillis =
            random.nextInt(maxSleepMillisBetweenActivityGeneration);

        MILLISECONDS.sleep(sleepDurationMillis);
    }

    /**
     * package for tests.
     */
    Map<String, Activity> getCachedLatestActivities() {
        return cachedLatestActivities;
    }

    /**
     * package for tests.
     */
    void internalUpdateStateAndListeners(Activity activity) {
        cachedLatestActivities.put(activity.getSport(), activity);

        listeners.values()
            .forEach(listener -> {
                try {
                    listener.onMessage(activity);
                }
                catch (Exception e) {
                    LOG.error("Exception invoking listener on message", e);
                }
            });
    }

    public static ActivityFeedServer createAndStartActivityFeedServer() {
        final Supplier<Activity> activityGeneratorSupplier =
            new ActivityGeneratorSupplier(new Faker());

        return createAndStartActivityFeedServer(
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("PretendActivityFeedServer-thread");
                t.setDaemon(true);

                return t;
            }),
            activityGeneratorSupplier,
            250);
    }

    /**
     * package for tests.
     */
    static ActivityFeedServer createAndStartActivityFeedServer(
        ExecutorService executorService,
        Supplier<Activity> activityGeneratorSupplier,
        int maxSleepMillisBetweenActivityGeneration) {

        final PretendActivityFeedServerImpl server =
            new PretendActivityFeedServerImpl(
                activityGeneratorSupplier,
                maxSleepMillisBetweenActivityGeneration);

        executorService.submit(server);

        return server;
    }
}
