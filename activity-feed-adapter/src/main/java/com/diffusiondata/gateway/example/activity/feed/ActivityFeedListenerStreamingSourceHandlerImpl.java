package com.diffusiondata.gateway.example.activity.feed;

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
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class ActivityFeedListenerStreamingSourceHandlerImpl
    implements ActivityFeedListener,
    StreamingSourceHandler {

    static final String DEFAULT_STREAMING_TOPIC_PREFIX =
        "streaming/activity/feed";

    private static final Logger LOG =
        LoggerFactory.getLogger(ActivityFeedListenerStreamingSourceHandlerImpl.class);

    private final ActivityFeedClient activityFeedClient;
    private final Publisher publisher;
    private final StateHandler stateHandler;
    private final ObjectMapper objectMapper;
    private final String topicPrefix;

    private String listenerIdentifier;

    public ActivityFeedListenerStreamingSourceHandlerImpl(
        ActivityFeedClient activityFeedClient,
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler,
        ObjectMapper objectMapper) {

        this.activityFeedClient =
            requireNonNull(activityFeedClient, "activityFeedClient");

        this.publisher = requireNonNull(publisher, "publisher");
        this.stateHandler = requireNonNull(stateHandler, "stateHandler");
        requireNonNull(serviceDefinition, "serviceDefinition");
        this.objectMapper = requireNonNull(objectMapper, "objectMapper");

        topicPrefix = serviceDefinition.getParameters()
            .getOrDefault("topicPrefix", DEFAULT_STREAMING_TOPIC_PREFIX)
            .toString();
    }

    @Override
    public void onMessage(Activity activity) {
        requireNonNull(activity, "activity");

        if (stateHandler.getState().equals(ServiceState.ACTIVE)) {
            try {
                final String topicPath = topicPrefix + "/" + activity.getSport();
                final String value = objectMapper.writeValueAsString(activity);

                publisher.publish(topicPath, value)
                    .exceptionally(throwable -> {
                        LOG.error("Cannot publish to topic: '{}'",
                            topicPath, throwable);

                        return null;
                    });
            }
            catch (JsonProcessingException |
                   PayloadConversionException e) {

                LOG.error("Cannot publish", e);
            }
        }
    }

    @Override
    public CompletableFuture<?> start() {
        listenerIdentifier =
            activityFeedClient.registerListener(this);

        LOG.info("Started activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> stop() {
        activityFeedClient.unregisterListener(listenerIdentifier);

        LOG.info("Stopped activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        activityFeedClient.unregisterListener(listenerIdentifier);

        LOG.info("Paused activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        listenerIdentifier =
            activityFeedClient.registerListener(this);

        LOG.info("Resumed activity feed streaming handler");

        return CompletableFuture.completedFuture(null);
    }

    /**
     * package for tests.
     */
    String getTopicPrefix() {
        return topicPrefix;
    }
}
