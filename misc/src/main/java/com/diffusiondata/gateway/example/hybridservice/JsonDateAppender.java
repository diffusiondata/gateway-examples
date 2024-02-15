package com.diffusiondata.gateway.example.hybridservice;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSinkServicePropertiesBuilder;
import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.HybridHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.TopicProperties;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A Hybrid service handler which gets an update from a JSON Diffusion topic and
 * appends current timestamp to the update and publishes it into another
 * Diffusion topic.
 * <p>
 * This handler only appends the date to the update if the update is of type JSON object.
 *
 * @author DiffusionData Ltd
 */
public final class JsonDateAppender implements HybridHandler<String> {

    private static final Logger LOG =
        LoggerFactory.getLogger(JsonDateAppender.class);

    private static final String DEFAULT_TARGET_TOPIC_PREFIX = "enhanced/";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Publisher publisher;

    private final String targetTopicPrefix;

    public JsonDateAppender(Publisher publisher, String targetTopicPrefix) {
        this.publisher = publisher;
        this.targetTopicPrefix =
            (targetTopicPrefix == null || targetTopicPrefix.isEmpty())
                ? DEFAULT_TARGET_TOPIC_PREFIX
                : (targetTopicPrefix.endsWith("/") ? targetTopicPrefix :
                targetTopicPrefix + "/");
    }

    @Override
    public CompletableFuture<?> update(String path, String value, TopicProperties topicProperties) {

        try {
            final JsonNode jsonNode = OBJECT_MAPPER.readTree(value);

            if (!jsonNode.isObject()) {
                return CompletableFuture.completedFuture(null);
            }

            final ObjectNode objectNode = (ObjectNode) jsonNode;

            objectNode.put("timestamp", Instant.now().toString());

            publisher
                .publish(targetTopicPrefix + path, objectNode)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        LOG.error("Failed to send updated data from {}", path);
                    }
                });
        }
        catch (JsonProcessingException | PayloadConversionException ex) {
            LOG.error("Failed to process update from {}", path, ex);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Class<String> valueType() {
        return String.class;
    }

    @Override
    public CompletableFuture<?> pause(PauseReason pauseReason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason resumeReason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public SourceServiceProperties getSourceServiceProperties() throws InvalidConfigurationException {
        return
            newSourceServicePropertiesBuilder()
                .topicType(TopicType.JSON)
                .build();
    }
}
