package com.diffusiondata.gateway.adapter.csv.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
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

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.SourceHandler.SourceServiceProperties;
import com.diffusiondata.gateway.framework.UpdateMode;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.gateway.framework.exceptions.PayloadConversionException;

/**
 * Tests for {@link CsvPollingSourceHandler}.
 *
 * @author DiffusionData Ltd
 */
@ExtendWith({MockitoExtension.class})
class CsvPollingSourceHandlerTest {
    private static final String FILE_NAME = "file.csv";
    private static final String DIFFUSION_TOPIC_NAME = "csvSourceTopic";

    private CsvPollingSourceHandler csvPollingSourceHandler;

    @Mock
    private Publisher publisher;


    @BeforeEach
    void setUp() {
        csvPollingSourceHandler = new CsvPollingSourceHandler(FILE_NAME,
            DIFFUSION_TOPIC_NAME, publisher);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(publisher);
    }

    @Test
    void testPoll() throws PayloadConversionException, InterruptedException,
        ExecutionException, TimeoutException {

        when(publisher.publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        csvPollingSourceHandler.poll().get(1000, TimeUnit.MILLISECONDS);

    }

    @Test
    void testPollIfPublishFailsWithIllegalArgumentEx() throws PayloadConversionException {
        doThrow(IllegalArgumentException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));
        assertThrows(IllegalArgumentException.class,
            () -> csvPollingSourceHandler.poll());
    }

    @Test
    void testPollIfPublishFailsWithPayloadException() throws PayloadConversionException, InterruptedException, ExecutionException, TimeoutException {
        doThrow(PayloadConversionException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));
        csvPollingSourceHandler.poll().get(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testPollIfPublishFailsWithPayloadExceptionForMaxThreshold() throws PayloadConversionException, InterruptedException, ExecutionException, TimeoutException {
        doThrow(PayloadConversionException.class).when(publisher).publish(eq(DIFFUSION_TOPIC_NAME), nullable(File.class));

        for (int i = 0; i < CsvPollingSourceHandler.CONVERSION_ERROR_THRESHOLD; i++) {
            csvPollingSourceHandler.poll().get(1000, TimeUnit.MILLISECONDS);
        }

        ExecutionException exception = assertThrows(ExecutionException.class,
            () -> csvPollingSourceHandler.poll().get(1000,
                TimeUnit.MILLISECONDS));
        assertTrue(exception.getCause() instanceof PayloadConversionException);
    }

    @Test
    void testGetServiceProperties() throws InvalidConfigurationException {
        SourceServiceProperties serviceProperties =
            csvPollingSourceHandler.getSourceServiceProperties();

        assertEquals("$CSV_to_JSON",
            serviceProperties.getPayloadConvertorName());
        assertEquals(UpdateMode.STREAMING, serviceProperties.getUpdateMode());
    }
}