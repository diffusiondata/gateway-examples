package com.pushtechnology.gateway.adapter.csv.sink;

import static com.pushtechnology.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;
import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.pushtechnology.gateway.framework.GatewayApplication;
import com.pushtechnology.gateway.framework.ServiceDefinition;
import com.pushtechnology.gateway.framework.ServiceMode;
import com.pushtechnology.gateway.framework.SinkHandler;
import com.pushtechnology.gateway.framework.StateHandler;
import com.pushtechnology.gateway.framework.exceptions.ApplicationConfigurationException;

/**
 * Main Gateway Application implementation for Csv sink application.
 *
 * @author Push Technology Limited
 */
final class CsvFileSinkApplication implements GatewayApplication {

    @Override
    public ApplicationDetails getApplicationDetails() throws ApplicationConfigurationException {
        return newApplicationDetailsBuilder()
            .addServiceType(
                "CSV_LOCAL_FILE_SINK",
                ServiceMode.SINK,
                "A sink service which writes received string updates from " +
                    "configured Diffusion topics into CSV files",
                "{\n" +
                    "    \"$schema\": \"http://json-schema" +
                    ".org/draft-07/schema#\",\n" +
                    "    \"$ref\": \"#/definitions/application\",\n" +
                    "    \"definitions\": {\n" +
                    "        \"application\": {\n" +
                    "            \"type\": \"object\",\n" +
                    "            \"additionalProperties\": false,\n" +
                    "            \"properties\": {\n" +
                    "                \"filePath\": {\n" +
                    "                    \"type\": \"string\"\n" +
                    "                }\n" +
                    "            },\n" +
                    "            \"required\": [\n" +
                    "                \"filePath\"\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    }\n" +
                    "}")
            .build("CSV_FILE_SINK", 1);
    }

    @Override
    public SinkHandler addSink(
        ServiceDefinition serviceDefinition,
        StateHandler stateHandler) {

        final Map<String, Object> parameters =
            serviceDefinition.getParameters();

        final String filePath = (String) parameters.get("filePath");

        return new CsvFileSinkHandler(filePath);
    }

    @Override
    public CompletableFuture<?> stop() {
        return completedFuture(null);
    }
}
