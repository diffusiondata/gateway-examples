package com.diffusiondata.gateway.example.sportsactivity.feed;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceState;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class SportsActivityFeedStreamingSourceHandler
    implements SportsActivityFeedListener,
    StreamingSourceHandler {

    static final String DEFAULT_STREAMING_TOPIC_PREFIX =
        "default/sports/activity/feed/stream";

    private static final Logger LOG =
        LoggerFactory.getLogger(
            SportsActivityFeedStreamingSourceHandler.class);

    private final SportsActivityFeedClient sportsActivityFeedClient;
    private final Publisher publisher;
    private final StateHandler stateHandler;
    private final ObjectMapper objectMapper;
    private final String topicPrefix;

    @GuardedBy("this")
    private String listenerIdentifier;

    public SportsActivityFeedStreamingSourceHandler(
        SportsActivityFeedClient sportsActivityFeedClient,
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler,
        ObjectMapper objectMapper) {

        this.sportsActivityFeedClient =
            requireNonNull(sportsActivityFeedClient,
                "sportsActivityFeedClient");

        this.publisher = requireNonNull(publisher, "publisher");
        this.stateHandler = requireNonNull(stateHandler, "stateHandler");
        requireNonNull(serviceDefinition, "serviceDefinition");
        this.objectMapper = requireNonNull(objectMapper, "objectMapper");

        topicPrefix = serviceDefinition.getParameters()
            .getOrDefault("topicPrefix", DEFAULT_STREAMING_TOPIC_PREFIX)
            .toString();
    }

    @Override
    public void onMessage(SportsActivity sportsActivity) {
        requireNonNull(sportsActivity, "sportsActivity");

        if (stateHandler.getState().equals(ServiceState.ACTIVE)) {
            try {
                final String topicPath = topicPrefix + "/" +
                    sportsActivity.getSport();

                final String value =
                    objectMapper.writeValueAsString(sportsActivity);

                publisher.publish(topicPath, value)
                    .exceptionally(throwable -> {
                        LOG.error("Cannot publish to topic: '{}'",
                            topicPath, throwable);

                        return null;
                    });
            }
            catch (JsonProcessingException |
                   PayloadConversionException e) {

                LOG.error(
                    "Failed to convert sports activity to configured type", e);
            }
        }
    }

    @Override
    public synchronized CompletableFuture<?> start() {
        listenerIdentifier =
            sportsActivityFeedClient.registerListener(this);

        LOG.info("Started sports activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletableFuture<?> stop() {
        sportsActivityFeedClient.unregisterListener(listenerIdentifier);
        listenerIdentifier = null;

        LOG.info("Stopped sports activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletableFuture<?> pause(PauseReason reason) {
        sportsActivityFeedClient.unregisterListener(listenerIdentifier);
        listenerIdentifier = null;

        LOG.info("Paused sports activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletableFuture<?> resume(ResumeReason reason) {
        listenerIdentifier =
            sportsActivityFeedClient.registerListener(this);

        LOG.info("Resumed sports activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    /**
     * package for tests.
     */
    String getTopicPrefix() {
        return topicPrefix;
    }

    /**
     * package for tests.
     */
    public String getListenerIdentifier() {
        return listenerIdentifier;
    }
}
