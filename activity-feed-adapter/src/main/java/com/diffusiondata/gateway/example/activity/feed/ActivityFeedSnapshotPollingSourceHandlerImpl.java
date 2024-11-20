package com.diffusiondata.gateway.example.activity.feed;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceState;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ActivityFeedSnapshotPollingSourceHandlerImpl
    implements PollingSourceHandler {

    static final String DEFAULT_POLLING_TOPIC_PATH =
        "polling/activity/feed";

    private static final Logger LOG =
        LoggerFactory.getLogger(ActivityFeedSnapshotPollingSourceHandlerImpl.class);

    private final ActivityFeedClient activityFeedClient;
    private final Publisher publisher;
    private final StateHandler stateHandler;
    private final ObjectMapper objectMapper;
    private final String topicPath;

    public ActivityFeedSnapshotPollingSourceHandlerImpl(
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

        topicPath = serviceDefinition.getParameters()
            .getOrDefault("topicPath", DEFAULT_POLLING_TOPIC_PATH)
            .toString();
    }

    @Override
    public CompletableFuture<?> poll() {
        final CompletableFuture<?> pollCf = new CompletableFuture<>();

        if (!stateHandler.getState().equals(ServiceState.ACTIVE)) {
            pollCf.complete(null);

            return pollCf;
        }

        final Collection<Activity> activities =
            activityFeedClient.getLatestActivities();

        if (activities.isEmpty()) {
            pollCf.complete(null);

            return pollCf;
        }

        try {
            final String value = objectMapper.writeValueAsString(activities);

            publisher.publish(topicPath, value)
                .whenComplete((o, throwable) -> {
                    if (throwable != null) {
                        pollCf.completeExceptionally(throwable);
                    }
                    else {
                        pollCf.complete(null);
                    }
                });
        }
        catch (JsonProcessingException |
               PayloadConversionException e) {

            LOG.error("Cannot publish", e);
            pollCf.completeExceptionally(e);

            return pollCf;
        }

        return pollCf;
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        LOG.info("Activity feed polling handler paused");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        LOG.info("Activity feed polling handler resumed");

        return CompletableFuture.completedFuture(null);
    }

    /**
     * package for tests.
     */
    String getTopicPath() {
        return topicPath;
    }
}
