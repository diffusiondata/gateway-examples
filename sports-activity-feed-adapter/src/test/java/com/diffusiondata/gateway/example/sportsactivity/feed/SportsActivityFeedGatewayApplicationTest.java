package com.diffusiondata.gateway.example.sportsactivity.feed;

import static com.diffusiondata.gateway.example.sportsactivity.feed.SportsActivityFeedGatewayApplication.APPLICATION_TYPE;
import static com.diffusiondata.gateway.example.sportsactivity.feed.SportsActivityFeedGatewayApplication.SPORTS_ACTIVITY_FEED_POLLER_SERVICE_TYPE_NAME;
import static com.diffusiondata.gateway.example.sportsactivity.feed.SportsActivityFeedGatewayApplication.SPORTS_ACTIVITY_FEED_STREAMER_SERVICE_TYPE_NAME;
import static com.diffusiondata.gateway.example.sportsactivity.feed.SportsActivityFeedStreamingSourceHandler.DEFAULT_STREAMING_TOPIC_PREFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.gateway.framework.GatewayApplication;
import com.diffusiondata.gateway.framework.GatewayApplication.ApplicationDetails;
import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceType;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.InvalidConfigurationException;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SportsActivityFeedGatewayApplicationTest {
    @Mock
    private SportsActivityFeedClient sportsActivityFeedClientMock;

    @Mock
    private ObjectMapper objectMapperMock;

    @Mock
    private ServiceDefinition serviceDefinitionMock;

    @Mock
    private ServiceType serviceTypeMock;

    @Mock
    private Publisher publisherMock;

    @Mock
    private StateHandler stateHandlerMock;

    private GatewayApplication application;

    @BeforeEach
    void beforeEachTest() {
        application = new SportsActivityFeedGatewayApplication(
            sportsActivityFeedClientMock,
            objectMapperMock);
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(
            sportsActivityFeedClientMock,
            objectMapperMock,
            serviceDefinitionMock,
            serviceTypeMock,
            publisherMock,
            stateHandlerMock
        );
    }

    @Test
    void testGetApplicationDetails() {
        final ApplicationDetails applicationDetails =
            application.getApplicationDetails();

        assertThat(applicationDetails, notNullValue());

        assertThat(applicationDetails.getApplicationType(),
            equalTo(APPLICATION_TYPE));

        final List<ServiceType> serviceTypes =
            applicationDetails.getServiceTypes();

        assertThat(serviceTypes, iterableWithSize(2));
    }

    @Test
    void testAddStreamingSourceWhenServiceTypeExists()
        throws Exception {

        when(serviceDefinitionMock.getServiceType())
            .thenReturn(serviceTypeMock);

        when(serviceTypeMock.getName())
            .thenReturn(SPORTS_ACTIVITY_FEED_STREAMER_SERVICE_TYPE_NAME);

        when(serviceDefinitionMock.getParameters())
            .thenReturn(Map.of("topicPrefix", DEFAULT_STREAMING_TOPIC_PREFIX));

        final StreamingSourceHandler handler =
            application.addStreamingSource(
                serviceDefinitionMock,
                publisherMock,
                stateHandlerMock);

        assertThat(handler, notNullValue());
        assertThat(handler,
            instanceOf(SportsActivityFeedStreamingSourceHandler.class));
    }

    @Test
    void testAddStreamingSourceWhenServiceTypeDoesNotExist() {
        when(serviceDefinitionMock.getServiceType())
            .thenReturn(serviceTypeMock);

        when(serviceTypeMock.getName())
            .thenReturn("some-unknown-server-type");

        assertThrows(
            InvalidConfigurationException.class,
            () -> application.addStreamingSource(
                serviceDefinitionMock,
                publisherMock,
                stateHandlerMock));
    }

    @Test
    void testAddPollingSourceWhenServiceTypeExists()
        throws Exception {

        when(serviceDefinitionMock.getServiceType())
            .thenReturn(serviceTypeMock);

        when(serviceTypeMock.getName())
            .thenReturn(SPORTS_ACTIVITY_FEED_POLLER_SERVICE_TYPE_NAME);

        when(serviceDefinitionMock.getParameters())
            .thenReturn(Map.of("topicPrefix", DEFAULT_STREAMING_TOPIC_PREFIX));

        final PollingSourceHandler handler =
            application.addPollingSource(
                serviceDefinitionMock,
                publisherMock,
                stateHandlerMock);

        assertThat(handler, notNullValue());
        assertThat(handler,
            instanceOf(SportsActivityFeedPollingSourceHandler.class));
    }

    @Test
    void testAddPollingSourceWhenServiceTypeDoesNotExist() {
        when(serviceDefinitionMock.getServiceType())
            .thenReturn(serviceTypeMock);

        when(serviceTypeMock.getName())
            .thenReturn("some-unknown-server-type");

        assertThrows(
            InvalidConfigurationException.class,
            () -> application.addPollingSource(
                serviceDefinitionMock,
                publisherMock,
                stateHandlerMock));
    }

    @Test
    void testStop() {
        final CompletableFuture<?> cf = application.stop();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }
}
