package com.diffusiondata.gateway.example.sportsactivity.feed;

import static com.diffusiondata.gateway.example.common.jackson.ObjectMapperUtils.createAndConfigureObjectMapper;

import com.diffusiondata.gateway.framework.DiffusionGatewayFramework;
import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.impl.SportsActivityFeedClientImpl;

public final class Runner {
    public static void main(String[] args) {
        DiffusionGatewayFramework.start(createGatewayApplication());
    }

    /**
     * package for tests.
     */
    static GatewayApplication createGatewayApplication() {
        return new SportsActivityFeedGatewayApplication(
            SportsActivityFeedClientImpl.connectToActivityFeedServer(),
            createAndConfigureObjectMapper());
    }
}
