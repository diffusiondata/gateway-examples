package com.diffusiondata.gateway.adapter.redis.source;

import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Validator for {@link SourceConfig} instance.
 *
 * @author Diffusion Data
 */
public class SourceConfigValidator {

    private final ObjectMapper objectMapper;

    SourceConfigValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    SourceConfig validateAndGet(Map<String, Object> parameters) throws
        InvalidConfigurationException {
        final SourceConfig sourceConfig =
            objectMapper.convertValue(parameters, SourceConfig.class);

        final String redisUrl = sourceConfig.getRedisUrl();
        final String diffusionTopicName = sourceConfig.getDiffusionTopicName();

        if (redisUrl == null ||
            redisUrl.isEmpty() ||
            diffusionTopicName == null ||
            diffusionTopicName.isEmpty()) {

            throw new InvalidConfigurationException(
                "Invalid config value");
        }
        return sourceConfig;
    }
}
