package com.pushtechnology.gateway.adapter.csv.source;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pushtechnology.gateway.framework.exceptions.InvalidConfigurationException;

/**
 * Tests for {@link SourceConfigValidator}.
 *
 * @author ndhougoda-hamal
 */
@ExtendWith(MockitoExtension.class)
class SourceConfigValidatorTest {
    private static final String FILE_NAME = "file.csv";
    private static final String DIFFUSION_TOPIC_NAME = "topic";

    private SourceConfigValidator sourceConfigValidator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SourceConfig sourceConfig;

    @Mock
    private Map parameters;

    @BeforeEach
    void setUp() {
        sourceConfigValidator = new SourceConfigValidator(objectMapper);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(objectMapper, sourceConfig, parameters);
    }

    @Test
    void testValidateAndConfig() throws InvalidConfigurationException {
        when(objectMapper.convertValue(parameters,
            SourceConfig.class)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn(FILE_NAME);
        when(sourceConfig.getDiffusionTopicName()).thenReturn(DIFFUSION_TOPIC_NAME);

        SourceConfig result =
            sourceConfigValidator.validateAndGet(parameters);

        assertEquals(result, sourceConfig);

    }

    @Test
    void testValidateAndConfigIfFileNameIsNull() {
        when(objectMapper.convertValue(parameters,
            SourceConfig.class)).thenReturn(sourceConfig);
        when(sourceConfig.getDiffusionTopicName()).thenReturn(DIFFUSION_TOPIC_NAME);

        assertThrows(InvalidConfigurationException.class,
            () -> sourceConfigValidator.validateAndGet(parameters));

        verify(sourceConfig, times(1)).getFileName();
    }

    @Test
    void testValidateAndConfigIfFileNameIsEmpty() {
        when(objectMapper.convertValue(parameters,
            SourceConfig.class)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn("");
        when(sourceConfig.getDiffusionTopicName()).thenReturn(DIFFUSION_TOPIC_NAME);

        assertThrows(InvalidConfigurationException.class,
            () -> sourceConfigValidator.validateAndGet(parameters));
    }

    @Test
    void testValidateAndConfigIfDiffusionTopicNameIsNull() {
        when(objectMapper.convertValue(parameters,
            SourceConfig.class)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn(FILE_NAME);

        assertThrows(InvalidConfigurationException.class,
            () -> sourceConfigValidator.validateAndGet(parameters));

        verify(sourceConfig, times(1)).getDiffusionTopicName();
    }

    @Test
    void testValidateAndConfigIfDiffusionTopicNameIsEmpty() {
        when(objectMapper.convertValue(parameters,
            SourceConfig.class)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn(FILE_NAME);
        when(sourceConfig.getDiffusionTopicName()).thenReturn("");

        assertThrows(InvalidConfigurationException.class,
            () -> sourceConfigValidator.validateAndGet(parameters));
    }
}