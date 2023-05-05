package com.diffusiondata.gateway.adapter.redis.source;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.UpdateMode;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;

/**
 * Polling source handler implementation for Redis source.
 *
 * @author Diffusion Data
 */
public class RedisSourcePollingHandler implements PollingSourceHandler {

    private static final Logger LOG =
        LoggerFactory.getLogger(RedisSourcePollingHandler.class);

    private final RedisAsyncCommands<String, String> redisClient;
    private final Publisher publisher;
    private final String diffusionTopicName;

    RedisSourcePollingHandler(
        String redisUrl,
        String diffusionTopicName,
        Publisher publisher) {
        this.diffusionTopicName = diffusionTopicName;
        this.publisher = publisher;
        redisClient = RedisClient
            .create(redisUrl)
            .connect()
            .async();
    }

    @Override public CompletableFuture<?> poll() {
        LOG.debug("Polled");
        CompletableFuture<?> pollCf = new CompletableFuture<>();
        try {
            final String value = redisClient.get(diffusionTopicName).get();
            pollCf = publisher.publish(diffusionTopicName, value);
        }
        catch (InterruptedException | ExecutionException |
            PayloadConversionException ex) {
            LOG.error("polling {} failed", diffusionTopicName, ex);
            pollCf.completeExceptionally(ex);
        }
        return pollCf;
    }

    @Override public CompletableFuture<?> pause(PauseReason pauseReason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override public CompletableFuture<?> resume(ResumeReason resumeReason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public SourceServiceProperties getSourceServiceProperties() throws
        InvalidConfigurationException {
        return
            newSourceServicePropertiesBuilder()
                .topicType(TopicType.STRING)
                .updateMode(UpdateMode.STREAMING)
                .build();
    }
}