package com.diffusiondata.gateway.adapter.csv.source;

import static com.diffusiondata.gateway.adapter.csv.source.CsvFileSourceApplication.APPLICATION_TYPE;
import static com.diffusiondata.gateway.adapter.csv.source.CsvFileSourceApplication.POLLING_JSON_SOURCE;
import static com.diffusiondata.gateway.adapter.csv.source.CsvFileSourceApplication.STREAMING_JSON_SOURCE;
import static com.diffusiondata.gateway.framework.ServiceMode.POLLING_SOURCE;
import static com.diffusiondata.gateway.framework.ServiceMode.STREAMING_SOURCE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.gateway.framework.GatewayApplication.ApplicationDetails;
import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceType;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;

/**
 * Tests for {@link CsvFileSourceApplication}.
 *
 * @author DiffusionData Ltd
 */
@ExtendWith(MockitoExtension.class)
class CsvFileSourceApplicationTest {

    private CsvFileSourceApplication csvFileSourceApplication;

    @Mock
    private SourceConfigValidator sourceConfigValidator;

    @Mock
    private ServiceDefinition serviceDefinition;

    @Mock
    private StateHandler stateHandler;

    @Mock
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        csvFileSourceApplication =
            new CsvFileSourceApplication(sourceConfigValidator);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(sourceConfigValidator, serviceDefinition,
            stateHandler, publisher);
    }

    @Test
    void testGetApplicationDetails() {
        ApplicationDetails applicationDetails =
            csvFileSourceApplication.getApplicationDetails();

        assertEquals(
            APPLICATION_TYPE,
            applicationDetails.getApplicationType());
        assertTrue(applicationDetails.getSharedConfigTypes().isEmpty());
        List<ServiceType> serviceTypes =
            applicationDetails.getServiceTypes();

        assertEquals(2, serviceTypes.size());

        assertAll(
            () -> {
                ServiceType serviceType = serviceTypes.get(0);
                assertAll(
                    () -> assertEquals(POLLING_JSON_SOURCE,
                        serviceType.getName()),
                    () -> assertEquals(POLLING_SOURCE, serviceType.getMode()),
                    () -> assertEquals("{}", serviceType.getSchema())
                );
            },
            () -> {
                ServiceType serviceType = serviceTypes.get(1);
                assertAll(
                    () -> assertEquals(STREAMING_JSON_SOURCE,
                        serviceType.getName()),
                    () -> assertEquals(STREAMING_SOURCE, serviceType.getMode()),
                    () -> assertEquals("{}", serviceType.getSchema())
                );
            }
        );
    }

    @Test
    void testAddPollingSource() throws InvalidConfigurationException {
        Map parameters = mock(Map.class);
        SourceConfig sourceConfig = mock(SourceConfig.class);

        when(serviceDefinition.getParameters()).thenReturn(parameters);
        when(sourceConfigValidator.validateAndGet(parameters)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn("./weather.csv");

        PollingSourceHandler pollingSourceHandler =
            csvFileSourceApplication.addPollingSource(
                serviceDefinition, publisher, stateHandler);
        assertTrue(pollingSourceHandler instanceof CsvPollingSourceHandler);

        verify(sourceConfig, times(1)).getFileName();
        verify(sourceConfig, times(1)).getDiffusionTopicName();

        verifyNoMoreInteractions(parameters, sourceConfig);
    }

    @Test
    void testAddPollingSourceIfSourceConfigValidationFails() throws InvalidConfigurationException {
        Map parameters = mock(Map.class);

        when(serviceDefinition.getParameters()).thenReturn(parameters);
        doThrow(InvalidConfigurationException.class).when(sourceConfigValidator).validateAndGet(parameters);

        assertThrows(InvalidConfigurationException.class,
            () -> csvFileSourceApplication.addPollingSource(
            serviceDefinition, publisher, stateHandler));

        verifyNoMoreInteractions(parameters);
    }

    @Test
    void testAddStreamingSource() throws InvalidConfigurationException {
        Map parameters = mock(Map.class);
        SourceConfig sourceConfig = mock(SourceConfig.class);

        when(serviceDefinition.getParameters()).thenReturn(parameters);
        when(sourceConfigValidator.validateAndGet(parameters)).thenReturn(sourceConfig);
        when(sourceConfig.getFileName()).thenReturn("./weather.csv");

        StreamingSourceHandler streamingSourceHandler =
            csvFileSourceApplication.addStreamingSource(
                serviceDefinition, publisher, stateHandler);
        assertTrue(streamingSourceHandler instanceof CsvStreamingSourceHandler);

        verify(sourceConfig, times(1)).getFileName();
        verify(sourceConfig, times(1)).getDiffusionTopicName();

        verifyNoMoreInteractions(parameters, sourceConfig);
    }

    @Test
    void testAddStreamingSourceIfSourceConfigValidationFails() throws InvalidConfigurationException {
        Map parameters = mock(Map.class);

        when(serviceDefinition.getParameters()).thenReturn(parameters);
        doThrow(InvalidConfigurationException.class).when(sourceConfigValidator).validateAndGet(parameters);

        assertThrows(InvalidConfigurationException.class,
            () -> csvFileSourceApplication.addStreamingSource(
            serviceDefinition, publisher, stateHandler));

        verifyNoMoreInteractions(parameters);
    }

    @Test
    void testStop() throws InterruptedException, ExecutionException,
        TimeoutException {
        csvFileSourceApplication.stop().get(1000, TimeUnit.MILLISECONDS);
    }
}