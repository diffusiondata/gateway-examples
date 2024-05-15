package com.diffusiondata.gateway.example;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import com.diffusiondata.gateway.example.hybridservice.JsonDateAppender;
import com.diffusiondata.gateway.example.missingtopicnotification.MissingTopicHandler;
import com.diffusiondata.gateway.framework.ApplicationContext;
import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.GatewayMeterRegistry;
import com.diffusiondata.gateway.framework.HybridHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceMode;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.Subscriber;
import com.diffusiondata.gateway.framework.exceptions.ApplicationConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.ApplicationInitializationException;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * A simple Gateway application to demonstrate creation of Hybrid service
 * handler and Missing topic notification handler.
 *
 * @author DiffusionData Ltd
 */
public class ExampleGatewayApplication implements GatewayApplication {

    private PrometheusServer prometheusServer;
    private PrometheusMeterRegistry prometheusMeterRegistry;
    private final CompositeMeterRegistry meterRegistry =
        new CompositeMeterRegistry();
    private boolean isMetricsEnabled;
    private ExecutorService executorService;

    @Override
    public void initialize(
        final ApplicationContext applicationContext) throws ApplicationConfigurationException {

        this.executorService = applicationContext.getExecutorService();
        isMetricsEnabled = applicationContext.isMetricsEnabled();

        if (!isMetricsEnabled) {
            return;
        }

        prometheusMeterRegistry =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        meterRegistry.add(prometheusMeterRegistry);
        meterRegistry.add(new JmxMeterRegistry(s -> null, Clock.SYSTEM));
    }

    @Override
    public GatewayMeterRegistry initializeGatewayMeterRegistry(
        Map<String, Object> globalParams) {
        return () -> meterRegistry;
    }

    @Override
    public ApplicationDetails getApplicationDetails() throws ApplicationConfigurationException {

        return newApplicationDetailsBuilder()
            .addServiceType(
                "DATE_APPENDER",
                ServiceMode.HYBRID,
                "A hybrid service which consumes from JSON Diffusion topic " +
                    "and adds current timestamp into the Json object and " +
                    "publishes it into another Diffusion topic",
                "{\n" +
                    "  \"$schema\": \"http://json-schema" +
                    ".org/draft-04/schema#\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"properties\": {\n" +
                    "    \"targetTopicPrefix\": {\n" +
                    "      \"type\": \"string\",\n" +
                    "      \"default\": \"enhanced/\",\n" +
                    "      \"description\": \"The target Diffusion topic " +
                    "prefix to publish updates to. This prefix is appended to" +
                    " the topic path from which the update is received, " +
                    "creating a new topic path. The updated data is then " +
                    "published to the newly created topic path\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}")
            .addServiceType(
                "MISSING_TOPIC_HANDLER",
                ServiceMode.STREAMING_SOURCE,
                "A streaming source service which gets notified if configured" +
                    " JSON topic is subscribed to, but is not present in " +
                    "server. Following notification, this service would " +
                    "publish dummy data to this topic",
                "{\n" +
                    "  \"$schema\": \"http://json-schema" +
                    ".org/draft-04/schema#\",\n" +
                    "  \"type\": \"object\",\n" +
                    "  \"properties\": {\n" +
                    "    \"missingTopicSelector\": {\n" +
                    "      \"type\": \"string\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"required\": [\n" +
                    "    \"missingTopicSelector\"\n" +
                    "  ]\n" +
                    "}")
            .build("EXAMPLE_APPLICATION", 1);
    }

    @Override
    public HybridHandler<?> addHybrid(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        Subscriber subscriber,
        StateHandler stateHandler) throws InvalidConfigurationException {


        final Map<String, Object> parameters =
            serviceDefinition.getParameters();

        final Object targetTopicPrefixValue =
            parameters.get("targetTopicPrefix");

        String targetTopicPrefix = null;

        if (targetTopicPrefixValue instanceof String) {
            targetTopicPrefix = (String) targetTopicPrefixValue;
        }
        return new JsonDateAppender(publisher, targetTopicPrefix);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler) throws InvalidConfigurationException {


        final Object missingTopicSelector =
            serviceDefinition.getParameters().get("missingTopicSelector");

        if (!(missingTopicSelector instanceof String) || ((String) missingTopicSelector).isEmpty()) {
            throw new InvalidConfigurationException(
                "`missingTopicSelector` in the configuration for" + serviceDefinition.getServiceName() + " is not valid");
        }
        return new MissingTopicHandler((String) missingTopicSelector,
            publisher);
    }

    @Override
    public CompletableFuture<?> stop() {
        if (prometheusServer != null) {
            prometheusServer.close();
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> start() {
        if (isMetricsEnabled) {

            try {
                prometheusServer =
                    new PrometheusServer(prometheusMeterRegistry);
            }
            catch (IOException ex) {
                throw new ApplicationInitializationException("Failed to " +
                    "initialize Prometheus server", ex);
            }

            executorService.submit(prometheusServer);
        }

        return CompletableFuture.completedFuture(null);
    }

    void initializeFramework() {
        DiffusionGatewayFramework.start(this);
    }
}
