package com.diffusiondata.gateway.adapter.redis.sink;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;

import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceMode;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.exceptions.ApplicationConfigurationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Main Gateway Application implementation for Redis sink application.
 *
 * @author Diffusion Data
 */
final public class RedisSinkApplication implements GatewayApplication {

    @Override public ApplicationDetails getApplicationDetails()
        throws ApplicationConfigurationException {
        return newApplicationDetailsBuilder()
            .addServiceType(
                "REDIS_SINK",
                ServiceMode.SINK,
                "A sink service which writes received string updates from " +
                    "configured Diffusion topics to a Redis instance",
                "{\n" +
                    "    \"$schema\": \"http://json-schema" +
                    ".org/draft-07/schema#\",\n" +
                    "    \"$ref\": \"#/definitions/application\",\n" +
                    "    \"definitions\": {\n" +
                    "        \"application\": {\n" +
                    "            \"type\": \"object\",\n" +
                    "            \"additionalProperties\": false,\n" +
                    "            \"properties\": {\n" +
                    "                \"redisUrl\": {\n" +
                    "                    \"type\": \"string\"\n" +
                    "                }\n" +
                    "            },\n" +
                    "            \"required\": [\n" +
                    "                \"redisUrl\"\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    }\n" +
                    "}")
            .build("REDIS_SINK", 1);
    }

    @Override
    public RedisSinkHandler addSink(
        ServiceDefinition serviceDefinition,
        StateHandler stateHandler) {

        final Map<String, Object> parameters =
            serviceDefinition.getParameters();

        final String redisUrl = (String) parameters.get("redisUrl");

        return new RedisSinkHandler(redisUrl);
    }

    @Override public CompletableFuture<?> stop() {
        return null;
    }
}
