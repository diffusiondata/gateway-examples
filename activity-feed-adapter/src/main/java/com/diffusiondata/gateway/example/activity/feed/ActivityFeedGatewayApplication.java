package com.diffusiondata.gateway.example.activity.feed;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceMode;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.ApplicationConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jcip.annotations.Immutable;

@Immutable
public final class ActivityFeedGatewayApplication
    implements GatewayApplication {

    static final String APPLICATION_TYPE =
        "activity-feed-application";

    static final String STREAMING_ACTIVITY_FEED_SERVICE_TYPE_NAME =
        "streaming-activity-feed-service";

    static final String POLLING_ACTIVITY_FEED_SERVICE_TYPE_NAME =
        "polling-activity-feed-service";

    private static final Logger LOG =
        LoggerFactory.getLogger(ActivityFeedGatewayApplication.class);

    private final ObjectMapper objectMapper;

    private final ActivityFeedClient activityFeedClient;

    public ActivityFeedGatewayApplication(
        ActivityFeedClient activityFeedClient,
        ObjectMapper objectMapper) {

        this.objectMapper =
            requireNonNull(objectMapper, "objectMapper");

        this.activityFeedClient =
            requireNonNull(activityFeedClient, "activityFeedClient");
    }

    @Override
    public ApplicationDetails getApplicationDetails()
        throws ApplicationConfigurationException {

        return DiffusionGatewayFramework.newApplicationDetailsBuilder()
            .addServiceType(
                STREAMING_ACTIVITY_FEED_SERVICE_TYPE_NAME,
                ServiceMode.STREAMING_SOURCE,
                "Streaming activity feed",
                null)
            .addServiceType(
                POLLING_ACTIVITY_FEED_SERVICE_TYPE_NAME,
                ServiceMode.POLLING_SOURCE,
                "Polled activity feed snapshot",
                null
            )
            .build(APPLICATION_TYPE, 1);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler)
        throws InvalidConfigurationException {

/*
        final String serviceType =
            serviceDefinition.getServiceType().getName();

        if (STREAMING_ACTIVITY_FEED_SERVICE_TYPE_NAME.equals(serviceType)) {
            return new ActivityFeedListenerStreamingSourceHandlerImpl(
                activityFeedClient,
                serviceDefinition,
                publisher,
                stateHandler,
                objectMapper);
        }
*/

        throw new InvalidConfigurationException(
            "Unknown service type: ");
//                + serviceType);
    }

    @Override
    public PollingSourceHandler addPollingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler)
        throws InvalidConfigurationException {

/*
        final String serviceType =
            serviceDefinition.getServiceType().getName();

        if (POLLING_ACTIVITY_FEED_SERVICE_TYPE_NAME.equals(serviceType)) {
            return new ActivityFeedSnapshotPollingSourceHandlerImpl(
                activityFeedClient,
                serviceDefinition,
                publisher,
                stateHandler,
                objectMapper);
        }
*/

        throw new InvalidConfigurationException(
            "Unknown service type: ");
//                + serviceType);
    }

    @Override
    public CompletableFuture<?> stop() {
        LOG.info("Application stop");

        return CompletableFuture.completedFuture(null);
    }
}
