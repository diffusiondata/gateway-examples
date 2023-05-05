package com.diffusiondata.gateway.adapter.redis.source;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSourceServicePropertiesBuilder;

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.UpdateMode;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

/**
 * Implementation of {@link StreamingSourceHandler} which listens to Redis
 * changes and publishes values to Diffusion server.
 *
 * @author Diffusion Data
 */
public class RedisSourceStreamingHandler implements StreamingSourceHandler {

    private static final Logger LOG =
        LoggerFactory.getLogger(RedisSourceStreamingHandler.class);

    private final RedisPubSubAsyncCommands<String, String> redisClient;
    private final Publisher publisher;
    private final String diffusionTopicName;

    RedisSourceStreamingHandler(
        String redisUrl,
        String diffusionTopicName,
        Publisher publisher
    ) {
        this.diffusionTopicName = diffusionTopicName + "*";
        this.publisher = publisher;

        StatefulRedisPubSubConnection<String, String> connection = RedisClient
            .create(redisUrl)
            .connectPubSub();

        connection.addListener(createListener());
        redisClient = connection.async();
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

    @Override
    public CompletableFuture<?> start() {
        return redisClient.psubscribe(diffusionTopicName).toCompletableFuture();
    }

    @Override
    public CompletableFuture<?> stop() {
        return redisClient.punsubscribe(diffusionTopicName).toCompletableFuture();
    }

    @Override public CompletableFuture<?> pause(PauseReason pauseReason) {
        return CompletableFuture.completedFuture(null);
    }

    @Override public CompletableFuture<?> resume(ResumeReason resumeReason) {
        return start();
    }

    private void update(String path, String value) {
        LOG.debug("Streaming update to server from {}", path);
        try {
            publisher.publish(path, value).get();
        }
        catch (InterruptedException | ExecutionException |
            PayloadConversionException ex) {
            LOG.error("updating {} failed", path, ex);
        }
    }

    private RedisPubSubListener<String, String> createListener() {
        return new RedisPubSubAdapter<>() {
            @Override public void message(String pattern, String channel, String message) {
                LOG.debug("Message received from a pattern subscription {} {}", pattern, channel);
                update(channel, message);
            }
        };
    }


}
