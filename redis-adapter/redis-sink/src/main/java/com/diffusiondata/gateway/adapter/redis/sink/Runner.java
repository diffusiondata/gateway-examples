package com.diffusiondata.gateway.adapter.redis.sink;

import static com.diffusiondata.gateway.framework.DiffusionGatewayFramework.initialize;

/**
 * Main Runner class.
 *
 * @author Diffusion Data
 */
public class Runner {
    public static void main(String[] args) {
        final RedisSinkApplication redisSinkApplication =
            new RedisSinkApplication();

        initialize(redisSinkApplication)
            .connect();


    }
}
