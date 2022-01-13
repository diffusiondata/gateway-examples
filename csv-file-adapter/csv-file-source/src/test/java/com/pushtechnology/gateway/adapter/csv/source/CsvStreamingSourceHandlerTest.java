package com.pushtechnology.gateway.adapter.csv.source;

import static com.pushtechnology.gateway.adapter.csv.source.CsvPollingSourceHandler.CONVERSION_ERROR_THRESHOLD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pushtechnology.gateway.framework.Publisher;
import com.pushtechnology.gateway.framework.SourceHandler.SourceServiceProperties;
import com.pushtechnology.gateway.framework.SourceHandler.SourceServiceProperties.UpdateMode;
import com.pushtechnology.gateway.framework.StateHandler;
import com.pushtechnology.gateway.framework.StateHandler.Status;
import com.pushtechnology.gateway.framework.exceptions.GatewayApplicationException;
import com.pushtechnology.gateway.framework.exceptions.InvalidConfigurationException;
import com.pushtechnology.gateway.framework.exceptions.PayloadConversionException;

/**
 * Tests for {@link CsvStreamingSourceHandler}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
class CsvStreamingSourceHandlerTest {
    private static final String FILE_NAME = "./weather.csv";
    private static final String DIFFUSION_TOPIC_NAME = "streamingSourceTopic";

    private CsvStreamingSourceHandler csvStreamingSourceHandler;

    @Mock
    private StateHandler stateHandler;

    @Mock
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        csvStreamingSourceHandler = new CsvStreamingSourceHandler(FILE_NAME,
            DIFFUSION_TOPIC_NAME, stateHandler, publisher);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(stateHandler, publisher);
    }

    @Test
    void testStart() throws GatewayApplicationException, InterruptedException, ExecutionException, TimeoutException {
        when(publisher.publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        csvStreamingSourceHandler.start().get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testStartIfPublishFailsWithIllegalArgumentEx() throws PayloadConversionException {
        doThrow(IllegalArgumentException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));
        assertThrows(IllegalArgumentException.class,
            () -> csvStreamingSourceHandler.start());
    }

    @Test
    void testStartIfPublishFailsWithPayloadException() throws GatewayApplicationException, InterruptedException, ExecutionException, TimeoutException {
        doThrow(PayloadConversionException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));
        csvStreamingSourceHandler.start().get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testStartIfPublishFailsWithPayloadExceptionForMaxThreshold() throws GatewayApplicationException, InterruptedException, ExecutionException, TimeoutException {
        doThrow(PayloadConversionException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));

        for (int i = 0; i < CONVERSION_ERROR_THRESHOLD; i++) {
            csvStreamingSourceHandler.start().get(1000, TimeUnit.MILLISECONDS);
        }

        csvStreamingSourceHandler.start().get(1000,
            TimeUnit.MILLISECONDS);
        verify(stateHandler, times(1)).reportStatus(
            Status.RED,
            "Error in sourceFile",
            "Content in " + FILE_NAME + " failed to be converted to " +
                "JSON " + CONVERSION_ERROR_THRESHOLD + " times");
    }

    @Test
    void testGetServiceProperties() throws InvalidConfigurationException {
        SourceServiceProperties serviceProperties =
            csvStreamingSourceHandler.getServiceProperties();

        assertEquals("$CSV_to_JSON", serviceProperties.getPayloadConvertorName());
        assertEquals(UpdateMode.STREAMING, serviceProperties.getUpdateMode());
    }
}