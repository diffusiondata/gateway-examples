package com.diffusiondata.gateway.example.missingtopicnotification;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.diffusiondata.gateway.framework.MissingTopicNotification;
import com.diffusiondata.gateway.framework.MissingTopicNotificationHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

/**
 * Implementation of a streaming source handler which registers a missing topic
 * notification handler and publishes dummy data to the missing topics.
 *
 * @author ndhougoda-hamal
 */
public final class MissingTopicHandler implements StreamingSourceHandler {
    private static final Logger LOG =
        LoggerFactory.getLogger(MissingTopicHandler.class);

    private final Publisher publisher;
    private final String missingTopicSelector;
    private final MissingTopicNotificationHandlerImpl missingTopicNotificationHandler;

    /**
     * Constructor.
     */
    public MissingTopicHandler(
        String missingTopicSelector,
        Publisher publisher) {
        this.publisher = publisher;

        this.missingTopicSelector = missingTopicSelector;

        this.missingTopicNotificationHandler =
            new MissingTopicNotificationHandlerImpl(publisher);
    }

    @Override
    public CompletableFuture<?> start() {

        return publisher
            .addMissingTopicHandler(
                missingTopicSelector,
                missingTopicNotificationHandler);
    }

    @Override
    public SourceServiceProperties getSourceServiceProperties() throws InvalidConfigurationException {
        return
            newSourceServicePropertiesBuilder()
                .topicType(TopicType.JSON)
                .build();
    }

    @Override
    public CompletableFuture<?> pause(PauseReason reason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason reason) {
        return CompletableFuture.completedFuture(null);
    }

    private static final class MissingTopicNotificationHandlerImpl implements MissingTopicNotificationHandler {

        private final Publisher publisher;

        private MissingTopicNotificationHandlerImpl(Publisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public CompletableFuture<?> onMissingTopic(
            MissingTopicNotification missingTopicNotification) {

            final String missingTopicPath =
                missingTopicNotification.getTopicPath();

            LOG.info("{} topic is missing. Publishing to this topic",
                missingTopicPath);

            try {
                publisher.publish(
                        missingTopicPath,
                        "{\n" +
                            "    \"dummy\": \"data\"\n" +
                            "}")
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            LOG.error(
                                "Failed to publish to {} topic",
                                missingTopicPath);
                        }
                    });
            }
            catch (PayloadConversionException ex) {
                throw new IllegalStateException(ex);
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
