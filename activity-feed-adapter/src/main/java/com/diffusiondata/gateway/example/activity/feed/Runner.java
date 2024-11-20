package com.diffusiondata.gateway.example.activity.feed;

import static com.diffusiondata.gateway.example.common.jackson.ObjectMapperUtils.createAndConfigureObjectMapper;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.pretend.example.activity.feed.client.impl.ActivityFeedClientImpl;

public final class Runner {
    public static void main(String[] args) {
        DiffusionGatewayFramework.start(createGatewayApplication());
    }

    /**
     * package for tests.
     */
    static GatewayApplication createGatewayApplication() {
        return new ActivityFeedGatewayApplication(
            ActivityFeedClientImpl.connectToActivityFeedServer(),
            createAndConfigureObjectMapper());
    }
}
