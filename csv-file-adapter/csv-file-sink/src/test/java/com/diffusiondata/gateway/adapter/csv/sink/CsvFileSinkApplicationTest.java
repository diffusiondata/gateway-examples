package com.diffusiondata.gateway.adapter.csv.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.diffusiondata.gateway.framework.GatewayApplication.ApplicationDetails;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.SinkHandler;
import com.diffusiondata.gateway.framework.StateHandler;

/**
 * Tests for {@link CsvFileSinkApplication}.
 *
 * @author DiffusionData Ltd
 */
class CsvFileSinkApplicationTest {

    private CsvFileSinkApplication csvFileSinkApplication;

    @BeforeEach
    void setUp() {
        csvFileSinkApplication = new CsvFileSinkApplication();
    }

    @Test
    void testGetApplicationDetails() {
        ApplicationDetails applicationDetails =
            csvFileSinkApplication.getApplicationDetails();

        assertEquals("CSV_FILE_SINK", applicationDetails.getApplicationType());
        assertTrue(applicationDetails.getSharedConfigTypes().isEmpty());
        assertEquals(1, applicationDetails.getServiceTypes().size());
        assertEquals("CSV_LOCAL_FILE_SINK", applicationDetails.getServiceTypes().get(0).getName());
    }

    @Test
    void testAddSink() {
        ServiceDefinition serviceDefinition = mock(ServiceDefinition.class);
        StateHandler stateHandler = mock(StateHandler.class);

        when(serviceDefinition.getParameters()).thenReturn(Collections.singletonMap("filePath", "path"));
        SinkHandler sinkHandler = csvFileSinkApplication.addSink(
            serviceDefinition, stateHandler);

        assertTrue(sinkHandler instanceof CsvFileSinkHandler);
    }
}