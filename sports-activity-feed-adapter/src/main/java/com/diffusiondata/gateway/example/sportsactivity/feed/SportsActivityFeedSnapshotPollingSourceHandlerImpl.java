package com.diffusiondata.gateway.example.sportsactivity.feed;

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
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class SportsActivityFeedSnapshotPollingSourceHandlerImpl
    implements PollingSourceHandler {

    static final String DEFAULT_POLLING_TOPIC_PATH =
        "default/sports/activity/feed/snapshot";

    private static final Logger LOG =
        LoggerFactory.getLogger(
            SportsActivityFeedSnapshotPollingSourceHandlerImpl.class);

    private final SportsActivityFeedClient sportsActivityFeedClient;
    private final Publisher publisher;
    private final StateHandler stateHandler;
    private final ObjectMapper objectMapper;
    private final String topicPath;

    public SportsActivityFeedSnapshotPollingSourceHandlerImpl(
        SportsActivityFeedClient sportsActivityFeedClient,
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler,
        ObjectMapper objectMapper) {

        this.sportsActivityFeedClient =
            requireNonNull(sportsActivityFeedClient,
                "sportActivityFeedClient");

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

        final Collection<SportsActivity> activities =
            sportsActivityFeedClient.getLatestSportsActivities();

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
        }

        return pollCf;
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        LOG.info("Paused sports activity feed polling handler");

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        LOG.info("Resumed sports activity feed polling handler");

        return CompletableFuture.completedFuture(null);
    }

    /**
     * package for tests.
     */
    String getTopicPath() {
        return topicPath;
    }
}
