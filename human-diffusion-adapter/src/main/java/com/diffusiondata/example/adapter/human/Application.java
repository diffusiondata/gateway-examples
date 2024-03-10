package com.diffusiondata.example.adapter.human;

import static com.diffusiondata.example.adapter.human.Common.getParam;
import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;
import static com.diffusiondata.gateway.framework.ServiceMode.STREAMING_SOURCE;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.ApplicationConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Application implements GatewayApplication  {

    private static final String GUI_SERVICE_SCHEMA = Common.getResourceFileAsString(Application.class, "human.schema.json");

    @Override
    public ApplicationDetails getApplicationDetails() throws ApplicationConfigurationException {
        return
            newApplicationDetailsBuilder()
                .addServiceType(
                    "HumanToDiffusion",
                    STREAMING_SOURCE,
                    "A human being, acting as a 3rd party streaming service",
                    GUI_SERVICE_SCHEMA)
                .build("Human Adapter", 1);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(ServiceDefinition serviceDefinition, Publisher publisher, StateHandler stateHandler) throws InvalidConfigurationException {

        final Map<String, Object> params = serviceDefinition.getParameters();
        final String greeting = getParam(params, "greeting", String.class, "Hello Human!");
        final String topicPath = getParam(params, "topic", String.class, "human/says");

        return new HumanStreamingSourceHandler(publisher, stateHandler, greeting, topicPath);
    }

    @Override
    public CompletableFuture<?> stop() {
        return CompletableFuture.completedFuture(null);
    }

    public static void main(String[] args) {
        DiffusionGatewayFramework.start(new Application());
    }

}
