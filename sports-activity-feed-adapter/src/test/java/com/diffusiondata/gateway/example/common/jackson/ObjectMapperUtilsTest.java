package com.diffusiondata.gateway.example.common.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.hamcrest.core.IsNull.notNullValue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectMapperUtilsTest {
    @Test
    void testCreateAndConfigureObjectMapper() {
        final ObjectMapper objectMapper =
            ObjectMapperUtils.createAndConfigureObjectMapper();

        assertThat(objectMapper, notNullValue());
        assertThat(objectMapper.getRegisteredModuleIds(), iterableWithSize(1));
        assertThat(objectMapper.getRegisteredModuleIds(),
            hasItem("jackson-datatype-jsr310"));
    }
}
