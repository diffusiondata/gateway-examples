package com.diffusiondata.gateway.adapter.csv.source;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

/**
 * Validator for {@link SourceConfig} instance.
 *
 * @author Push Technology Limited
 */
public final class SourceConfigValidator {

    private final ObjectMapper objectMapper;

    SourceConfigValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    SourceConfig validateAndGet(Map<String, Object> parameters) throws InvalidConfigurationException {
        final SourceConfig sourceConfig =
            objectMapper.convertValue(parameters, SourceConfig.class);

        final String fileName = sourceConfig.getFileName();
        final String diffusionTopicName = sourceConfig.getDiffusionTopicName();

        if (fileName == null ||
            fileName.isEmpty() ||
            diffusionTopicName == null ||
            diffusionTopicName.isEmpty()) {

            throw new InvalidConfigurationException(
                "Invalid config value");
        }

        return sourceConfig;
    }
}
