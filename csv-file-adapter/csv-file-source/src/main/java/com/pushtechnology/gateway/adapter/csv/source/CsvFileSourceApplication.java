package com.pushtechnology.gateway.adapter.csv.source;

import static com.pushtechnology.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.pushtechnology.gateway.framework.GatewayApplication;
import com.pushtechnology.gateway.framework.PollingSourceHandler;
import com.pushtechnology.gateway.framework.Publisher;
import com.pushtechnology.gateway.framework.ServiceDefinition;
import com.pushtechnology.gateway.framework.ServiceMode;
import com.pushtechnology.gateway.framework.StateHandler;
import com.pushtechnology.gateway.framework.StreamingSourceHandler;
import com.pushtechnology.gateway.framework.exceptions.InvalidConfigurationException;

/**
 * Main Gateway Application implementation for Csv source application.
 *
 * @author Push Technology Limited
 */
final class CsvFileSourceApplication implements GatewayApplication {

    static final String POLLING_JSON_SOURCE = "POLLING_JSON_SOURCE";
    static final String STREAMING_JSON_SOURCE = "STREAMING_JSON_SOURCE";
    static final String APPLICATION_TYPE = "CSV_FILE_SOURCE";

    private final SourceConfigValidator sourceConfigValidator;

    CsvFileSourceApplication(
        SourceConfigValidator sourceConfigValidator) {
        this.sourceConfigValidator = sourceConfigValidator;
    }

    @Override
    public ApplicationDetails getApplicationDetails() {
        return newApplicationDetailsBuilder()
            .addServiceType(POLLING_JSON_SOURCE,
                ServiceMode.POLLING_SOURCE,
                "A polling source service which frequently polls the " +
                    "configured CSV file for any updates and publishes to " +
                    "Diffusion server",
                null)
            .addServiceType(STREAMING_JSON_SOURCE,
                ServiceMode.STREAMING_SOURCE,
                "A streaming source which listens to CSV file changes and " +
                    "publishes contents to Diffusion server.",
                null)
            .build(APPLICATION_TYPE, 1);
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

        return new CsvPollingSourceHandler(
            sourceConfig.getFileName(),
            sourceConfig.getDiffusionTopicName(),
            publisher);
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

        return new CsvStreamingSourceHandler(
            sourceConfig.getFileName(),
            sourceConfig.getDiffusionTopicName(),
            stateHandler,
            publisher);
    }


    @Override
    public CompletableFuture<?> stop() {
        return CompletableFuture.completedFuture(null);
    }
}
