package com.pushtechnology.gateway.adapter.csv.sink;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pushtechnology.gateway.framework.GatewayApplication.ApplicationDetails;
import com.pushtechnology.gateway.framework.ServiceDefinition;
import com.pushtechnology.gateway.framework.SinkHandler;
import com.pushtechnology.gateway.framework.StateHandler;

/**
 * Tests for {@link CsvFileSinkApplication}.
 *
 * @author Push Technology Limited
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