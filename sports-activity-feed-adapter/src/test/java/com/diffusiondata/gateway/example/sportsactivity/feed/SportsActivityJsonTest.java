package com.diffusiondata.gateway.example.sportsactivity.feed;

import static com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivityTestUtils.createPopulatedSportsActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.Test;

import com.diffusiondata.gateway.example.common.jackson.ObjectMapperUtils;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivityTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

class SportsActivityJsonTest {
    private final ObjectMapper objectMapper =
        ObjectMapperUtils.createAndConfigureObjectMapper();

    @Test
    void testWriteValueAsString()
        throws Exception {

        final String result =
            objectMapper.writeValueAsString(SportsActivityTestUtils.createPopulatedSportsActivity());

        assertThat(result, containsString("dateOfActivity"));
    }
}
