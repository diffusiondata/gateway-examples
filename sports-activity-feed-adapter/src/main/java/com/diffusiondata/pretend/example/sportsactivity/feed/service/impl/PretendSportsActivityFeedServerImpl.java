package com.diffusiondata.pretend.example.sportsactivity.feed.service.impl;

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

import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.diffusiondata.pretend.example.sportsactivity.feed.service.SportsActivityFeedServer;

import net.datafaker.Faker;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class PretendSportsActivityFeedServerImpl
    implements SportsActivityFeedServer, Runnable {

    private static final Logger LOG =
        LoggerFactory.getLogger(PretendSportsActivityFeedServerImpl.class);

    private final Random random = new Random();

    @GuardedBy("this")
    private final Map<String, SportsActivityFeedListener> listeners =
        new HashMap<>();

    private final ConcurrentMap<String, SportsActivity> cachedLatestActivities =
        new ConcurrentHashMap<>();

    private final Supplier<SportsActivity> activityGeneratorSupplier;
    private final int maxSleepMillisBetweenActivityGeneration;

    private PretendSportsActivityFeedServerImpl(
        Supplier<SportsActivity> activityGeneratorSupplier,
        int maxSleepMillisBetweenActivityGeneration) {

        this.activityGeneratorSupplier = activityGeneratorSupplier;
        this.maxSleepMillisBetweenActivityGeneration =
            Math.max(maxSleepMillisBetweenActivityGeneration, 1);
    }

    @Override
    public synchronized String registerClientListener(
        SportsActivityFeedListener sportsActivityFeedListener) {

        requireNonNull(sportsActivityFeedListener, "activityFeedListener");

        final String listenerIdentifier = UUID.randomUUID().toString();

        listeners.put(listenerIdentifier, sportsActivityFeedListener);

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
    public Collection<SportsActivity> getLatestSportsActivities() {
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
    Map<String, SportsActivityFeedListener> getListeners() {
        return unmodifiableMap(listeners);
    }

    /**
     * package for tests.
     */
    void runOnce()
        throws InterruptedException {

        final SportsActivity sportsActivity = activityGeneratorSupplier.get();

        internalUpdateStateAndListeners(sportsActivity);

        final int sleepDurationMillis =
            random.nextInt(maxSleepMillisBetweenActivityGeneration);

        MILLISECONDS.sleep(sleepDurationMillis);
    }

    /**
     * package for tests.
     */
    Map<String, SportsActivity> getCachedSportsLatestActivities() {
        return cachedLatestActivities;
    }

    /**
     * package for tests.
     */
    void internalUpdateStateAndListeners(SportsActivity sportsActivity) {
        cachedLatestActivities.put(sportsActivity.getSport(), sportsActivity);

        listeners.values()
            .forEach(listener -> {
                try {
                    listener.onMessage(sportsActivity);
                }
                catch (Exception e) {
                    LOG.error("Exception invoking listener on message", e);
                }
            });
    }

    public static SportsActivityFeedServer createAndStartActivityFeedServer() {
        final Supplier<SportsActivity> activityGeneratorSupplier =
            new RandomSportsActivityGeneratorSupplier(new Faker());

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
    static SportsActivityFeedServer createAndStartActivityFeedServer(
        ExecutorService executorService,
        Supplier<SportsActivity> activityGeneratorSupplier,
        int maxSleepMillisBetweenActivityGeneration) {

        final PretendSportsActivityFeedServerImpl server =
            new PretendSportsActivityFeedServerImpl(
                activityGeneratorSupplier,
                maxSleepMillisBetweenActivityGeneration);

        executorService.submit(server);

        return server;
    }
}
