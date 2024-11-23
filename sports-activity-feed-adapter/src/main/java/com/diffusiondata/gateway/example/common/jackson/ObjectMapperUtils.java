package com.diffusiondata.gateway.example.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMapperUtils {
    private ObjectMapperUtils() {
        // Private method to prevent creation
    }

    public static ObjectMapper createAndConfigureObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
