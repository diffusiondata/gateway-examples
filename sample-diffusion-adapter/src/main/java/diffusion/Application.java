package diffusion;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newApplicationDetailsBuilder;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.GatewayMeterRegistry;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceMode;
import com.diffusiondata.gateway.framework.SinkHandler;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.Subscriber;
import com.diffusiondata.gateway.framework.exceptions.ApplicationConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import io.micrometer.core.instrument.Clock;
import io.micrometer.jmx.JmxMeterRegistry;


public class Application implements GatewayApplication {
    private JmxMeterRegistry meterRegistry =
        new JmxMeterRegistry(s -> null, Clock.SYSTEM);

    @Override
    public GatewayMeterRegistry initializeGatewayMeterRegistry(
        Map<String, Object> globalParams) {
        return () -> meterRegistry;
    }

    @Override
    public ApplicationDetails getApplicationDetails() throws ApplicationConfigurationException {
        return
            newApplicationDetailsBuilder()
                .addServiceType(
                    "REMOTE_STREAMER",
                    ServiceMode.STREAMING_SOURCE,
                    "Consumes from remote Diffusion topics",
                    "{\n" +
                        "  \"$schema\": \"http://json-schema" +
                        ".org/draft-07/schema#\",\n" +
                        "  \"title\": \"Generated schema for Root\",\n" +
                        "  \"type\": \"object\",\n" +
                        "  \"properties\": {\n" +
                        "    \"principal\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    },\n" +
                        "    \"password\": {\n" +
                        "      \"type\": \"string\",\n" +
                        "      \"hidden\": \"true\"\n" +
                        "    },\n" +
                        "    \"topicSelector\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    },\n" +
                        "    \"url\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"required\": [\n" +
                        "    \"principal\",\n" +
                        "    \"password\",\n" +
                        "    \"topicSelector\",\n" +
                        "    \"url\"\n" +
                        "  ]\n" +
                        "}"
                )
                .addServiceType(
                    "LOCAL_STREAMER",
                    ServiceMode.SINK,
                    "Consumes from Local Diffusion topics",
                    null
                )
                .build("Diffusion adapter", 1);
    }

    @Override
    public StreamingSourceHandler addStreamingSource(
        ServiceDefinition serviceDefinition,
        Publisher publisher,
        StateHandler stateHandler) throws InvalidConfigurationException {

        Map<String, Object> parameters = serviceDefinition.getParameters();

        String url = (String) parameters.get("url");
        String principal = (String) parameters.get("principal");
        String password = (String) parameters.get("password");
        String topicSelector = (String) parameters.get("topicSelector");
        String prefix = parameters.get("prefix") == null ? "" :
            (String) parameters.get("prefix");

        return new RemoteStreamer(
            url,
            principal,
            password,
            topicSelector,
            prefix,
            publisher);
    }

    @Override
    public SinkHandler<?> addSink(
        ServiceDefinition serviceDefinition,
        Subscriber subscriber,
        StateHandler stateHandler) throws InvalidConfigurationException {

        return new LocalStreamer();
    }

    @Override
    public CompletableFuture<?> stop() {
        return CompletableFuture.completedFuture(null);
    }

    public static void main(String[] args) {
        DiffusionGatewayFramework.start(new Application());
    }
}
