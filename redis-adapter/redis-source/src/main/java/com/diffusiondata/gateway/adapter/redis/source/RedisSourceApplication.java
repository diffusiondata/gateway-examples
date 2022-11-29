package com.diffusiondata.gateway.adapter.redis.source;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;

import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceMode;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main Gateway Application implementation for Redis source application.
 *
 * @author Diffsion Data
 */
public class RedisSourceApplication implements GatewayApplication {

    static final String POLLING_REDIS_SOURCE = "POLLING_REDIS_SOURCE";
    static final String STREAMING_REDIS_SOURCE = "STREAMING_REDIS_SOURCE";
    static final String APPLICATION_TYPE = "REDIS_SOURCE";

    private final SourceConfigValidator sourceConfigValidator;

    RedisSourceApplication(SourceConfigValidator sourceConfigValidator) {
        this.sourceConfigValidator = sourceConfigValidator;
    }

    @Override
    public ApplicationDetails getApplicationDetails() {
        return newApplicationDetailsBuilder()
            .addServiceType(POLLING_REDIS_SOURCE,
                ServiceMode.POLLING_SOURCE,
                "A polling source service which frequently polls the " +
                    "configured Redis instance for any updates and publishes to " +
                    "Diffusion server",
                null)
            .addServiceType(STREAMING_REDIS_SOURCE,
                ServiceMode.STREAMING_SOURCE,
                "A streaming source which subscribes to a Redis instance and " +
                    "publishes contents to Diffusion server.",
                null)
            .build(APPLICATION_TYPE, 1);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler) throws InvalidConfigurationException {

        final Map<String, Object> parameters =
            serviceDefinition.getParameters();

        final SourceConfig sourceConfig =
            sourceConfigValidator.validateAndGet(parameters);

        return new RedisSourceStreamingHandler(
            sourceConfig.getRedisUrl(),
            sourceConfig.getDiffusionTopicName(),
            publisher);
    }

    @Override
    public PollingSourceHandler addPollingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler) throws InvalidConfigurationException {

        final Map<String, Object> parameters =
            serviceDefinition.getParameters();

        final SourceConfig sourceConfig =
            sourceConfigValidator.validateAndGet(parameters);

        return new RedisSourcePollingHandler(
            sourceConfig.getRedisUrl(),
            sourceConfig.getDiffusionTopicName(),
            publisher);
    }

    @Override public CompletableFuture<?> stop() {
        return CompletableFuture.completedFuture(null);
    }
}