package com.diffusiondata.gateway.example.sportsactivity.feed;

import static com.diffusiondata.pretend.example.sportsactivity.feed.model.ActivityTestUtils.createPopulatedActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.Test;

import com.diffusiondata.gateway.example.common.jackson.ObjectMapperUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

class SportsActivityJsonTest {
    private final ObjectMapper objectMapper =
        ObjectMapperUtils.createAndConfigureObjectMapper();

    @Test
    void testWriteValueAsString()
        throws Exception {

        final String result =
            objectMapper.writeValueAsString(createPopulatedActivity());

        assertThat(result, containsString("dateOfActivity"));
    }
}
