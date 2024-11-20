package com.diffusiondata.gateway.example.activity.feed;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import org.junit.jupiter.api.Test;

import com.diffusiondata.gateway.framework.GatewayApplication;

class RunnerTest {
    @Test
    void testCreateGatewayApplication() {
        final GatewayApplication gatewayApplication =
            Runner.createGatewayApplication();

        assertThat(gatewayApplication, notNullValue());
    }
}
