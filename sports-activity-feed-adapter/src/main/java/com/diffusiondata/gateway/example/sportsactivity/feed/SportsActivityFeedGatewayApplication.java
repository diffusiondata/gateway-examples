package com.diffusiondata.gateway.example.sportsactivity.feed;

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
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.jcip.annotations.Immutable;

@Immutable
public final class SportsActivityFeedGatewayApplication
    implements GatewayApplication {

    static final String APPLICATION_TYPE =
        "sports-activity-feed-application";

    static final String SPORTS_ACTIVITY_FEED_STREAMER_SERVICE_TYPE_NAME =
        "SPORTS_ACTIVITY_FEED_STREAMER";

    static final String SPORTS_ACTIVITY_FEED_POLLER_SERVICE_TYPE_NAME =
        "SPORTS_ACTIVITY_FEED_POLLER";

    private static final Logger LOG =
        LoggerFactory.getLogger(SportsActivityFeedGatewayApplication.class);

    private final SportsActivityFeedClient sportsActivityFeedClient;
    private final ObjectMapper objectMapper;

    public SportsActivityFeedGatewayApplication(
        SportsActivityFeedClient sportsActivityFeedClient,
        ObjectMapper objectMapper) {

        this.sportsActivityFeedClient =
            requireNonNull(sportsActivityFeedClient,
                "sportsActivityFeedClient");

        this.objectMapper =
            requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public ApplicationDetails getApplicationDetails()
        throws ApplicationConfigurationException {

        return DiffusionGatewayFramework.newApplicationDetailsBuilder()
            .addServiceType(
                SPORTS_ACTIVITY_FEED_POLLER_SERVICE_TYPE_NAME,
                ServiceMode.POLLING_SOURCE,
                "Polls the sports activity feed at a regular interval",
                null) // For this example we won't use schema validation
            .addServiceType(
                SPORTS_ACTIVITY_FEED_STREAMER_SERVICE_TYPE_NAME,
                ServiceMode.STREAMING_SOURCE,
                "Streams the sports activities as they are available",
                null) // For this example we won't use schema validation
            .build(APPLICATION_TYPE, 1);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler)
        throws InvalidConfigurationException {

        final String serviceType =
            serviceDefinition.getServiceType().getName();

        if (SPORTS_ACTIVITY_FEED_STREAMER_SERVICE_TYPE_NAME.equals(serviceType)) {
            return new SportsActivityFeedStreamingSourceHandler(
                sportsActivityFeedClient,
                serviceDefinition,
                publisher,
                stateHandler,
                objectMapper);
        }

        throw new InvalidConfigurationException(
            "Unknown service type: " + serviceType);
    }

    @Override
    public PollingSourceHandler addPollingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler)
        throws InvalidConfigurationException {

        final String serviceType =
            serviceDefinition.getServiceType().getName();

        if (SPORTS_ACTIVITY_FEED_POLLER_SERVICE_TYPE_NAME.equals(serviceType)) {
            return new SportsActivityFeedPollingSourceHandler(
                sportsActivityFeedClient,
                serviceDefinition,
                publisher,
                stateHandler,
                objectMapper);
        }

        throw new InvalidConfigurationException(
            "Unknown service type: " + serviceType);
    }

    @Override
    public CompletableFuture<?> stop() {
        LOG.info("Application stop");

        return CompletableFuture.completedFuture(null);
    }
}
