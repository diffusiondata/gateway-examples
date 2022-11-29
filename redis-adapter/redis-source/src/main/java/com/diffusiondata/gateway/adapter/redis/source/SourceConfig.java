package com.diffusiondata.gateway.adapter.redis.source;

/**
 * Sample configuration required for source application.
 *
 * @author Diffusion Data
 */
public class SourceConfig {
    private String redisUrl;

    private String diffusionTopicName;

    public String getRedisUrl() {
        return redisUrl;
    }

    public String getDiffusionTopicName() {
        return diffusionTopicName;
    }
}
