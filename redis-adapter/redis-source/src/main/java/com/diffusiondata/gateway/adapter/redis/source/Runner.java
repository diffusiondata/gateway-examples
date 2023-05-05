package com.diffusiondata.gateway.adapter.redis.source;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.initialize;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main Runner class.
 *
 * @author Diffusion Data
 */
public class Runner {
    public static void main(String[] args) {
        final RedisSourceApplication redisSourceApplication =
            new RedisSourceApplication(new SourceConfigValidator(new ObjectMapper()));

        initialize(redisSourceApplication)
            .connect();
    }
}
