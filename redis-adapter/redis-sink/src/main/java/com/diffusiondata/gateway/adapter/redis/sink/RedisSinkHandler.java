package com.diffusiondata.gateway.adapter.redis.sink;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.newSinkServicePropertiesBuilder;
import static java.util.concurrent.CompletableFuture.completedFuture;

import com.diffusiondata.gateway.framework.SinkHandler;
import com.diffusiondata.gateway.framework.TopicType;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

import java.util.concurrent.CompletableFuture;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import net.jcip.annotations.Immutable;

/**
 * Redis sink handler to write received string update into a Redis instance.
 *
 * @author Diffusion Data
 */
@Immutable
final class RedisSinkHandler implements SinkHandler<String> {

    private final RedisAsyncCommands<String, String> redisClient;

    public RedisSinkHandler(String redisUrl) {
        redisClient = RedisClient
            .create(redisUrl)
            .connect()
            .async();
    }

    @Override
    public SinkServiceProperties getSinkServiceProperties()
        throws InvalidConfigurationException {
        return newSinkServicePropertiesBuilder()
            .topicType(TopicType.JSON)
            .payloadConvertorName("$Default_JSON")
            .build();
    }

    @Override
    public CompletableFuture<?> update(String diffusionTopic, String value) {
        return redisClient.set(diffusionTopic, value).toCompletableFuture();
    }

    @Override
    public CompletableFuture<?> pause(PauseReason pauseReason) {
        return completedFuture(null);
    }

    @Override
    public CompletableFuture<?> resume(ResumeReason resumeReason) {
        return completedFuture(null);
    }
}
