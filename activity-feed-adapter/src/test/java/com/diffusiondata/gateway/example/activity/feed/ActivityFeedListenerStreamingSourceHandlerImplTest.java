package com.diffusiondata.gateway.example.activity.feed;

import static com.diffusiondata.gateway.example.activity.feed.ActivityFeedListenerStreamingSourceHandlerImpl.DEFAULT_STREAMING_TOPIC_PREFIX;
import static com.diffusiondata.pretend.example.activity.feed.model.ActivityTestUtils.createPopulatedActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceHandler.PauseReason;
import com.diffusiondata.gateway.framework.ServiceHandler.ResumeReason;
import com.diffusiondata.gateway.framework.ServiceState;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.StreamingSourceHandler;
import com.diffusiondata.gateway.framework.exceptions.DiffusionClientException;
import com.diffusiondata.gateway.util.Util;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ActivityFeedListenerStreamingSourceHandlerImplTest {
    @Mock
    private ActivityFeedClient activityFeedClientMock;

    @Mock
    private ServiceDefinition serviceDefinitionMock;

    @Mock
    Publisher publisherMock;

    @Mock
    private StateHandler stateHandlerMock;

    @Mock
    private ObjectMapper objectMapperMock;

    private StreamingSourceHandler handler;

    @BeforeEach
    void beforeEachTest() {
        when(serviceDefinitionMock.getParameters())
            .thenReturn(Map.of("topicPrefix", DEFAULT_STREAMING_TOPIC_PREFIX));

        handler = new ActivityFeedListenerStreamingSourceHandlerImpl(
            activityFeedClientMock,
            serviceDefinitionMock,
            publisherMock,
            stateHandlerMock,
            objectMapperMock);

        final String topicPrefix =
            ((ActivityFeedListenerStreamingSourceHandlerImpl) handler)
                .getTopicPrefix();

        assertThat(topicPrefix, notNullValue());
        assertThat(topicPrefix, equalTo(DEFAULT_STREAMING_TOPIC_PREFIX));
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(
            activityFeedClientMock,
            serviceDefinitionMock,
            publisherMock,
            stateHandlerMock,
            objectMapperMock
        );
    }

    @Test
    void testOnMessageWhenServiceStateIsActive()
        throws Exception {

        final Activity activity = createPopulatedActivity();
        final String expectedTopicPath = DEFAULT_STREAMING_TOPIC_PREFIX + "/"
            + activity.getSport();

        final String jsonAsString = "{}";

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(objectMapperMock.writeValueAsString(activity))
            .thenReturn(jsonAsString);

        when(publisherMock.publish(expectedTopicPath, jsonAsString))
            .thenReturn(CompletableFuture.completedFuture(null));

        ((ActivityFeedListener) handler).onMessage(activity);
    }

    @Test
    void testOnMessageWhenServiceStateIsActiveAndPublishExceptionIsThrown()
        throws Exception {

        final Activity activity = createPopulatedActivity();
        final String topicPath = DEFAULT_STREAMING_TOPIC_PREFIX + "/" +
            activity.getSport();

        final String jsonAsString = "{}";

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(objectMapperMock.writeValueAsString(activity))
            .thenReturn(jsonAsString);

        when(publisherMock.publish(topicPath, jsonAsString))
            .thenReturn(Util.getCfWithException(
                new DiffusionClientException("ignore this is a test")));

        ((ActivityFeedListener) handler).onMessage(activity);
    }

    @Test
    void testOnMessageWhenServiceStateIsActiveAndCheckedExceptionIsThrown()
        throws Exception {

        final Activity activity = createPopulatedActivity();

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        doThrow(JsonProcessingException.class).when(objectMapperMock)
            .writeValueAsString(activity);

        ((ActivityFeedListener) handler).onMessage(activity);
    }

    @Test
    void testOnMessageWhenServiceStateIsNotActive() {
        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.PAUSED);

        ((ActivityFeedListener) handler).onMessage(createPopulatedActivity());
    }

    @Test
    @Order(10)
    void testStart() {
        invokeStart();
    }

    @Test
    @Order(20)
    void testStop() {
        final String listenerIdentifier = invokeStart();

        when(activityFeedClientMock.unregisterListener(listenerIdentifier))
            .thenReturn(true);

        final CompletableFuture<?> cf = handler.stop();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    @Order(30)
    void testPause() {
        final String listenerIdentifier = invokeStart();

        when(activityFeedClientMock.unregisterListener(listenerIdentifier))
            .thenReturn(true);

        final CompletableFuture<?> cf = handler.pause(PauseReason.REQUESTED);

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    @Order(40)
    void testResume() {
        final String listenerIdentifier = "listener-identifier";

        final ActivityFeedListener listener = (ActivityFeedListener) handler;

        when(activityFeedClientMock.registerListener(listener))
            .thenReturn(listenerIdentifier);

        final CompletableFuture<?> cf = handler.resume(ResumeReason.REQUESTED);

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    private String invokeStart() {
        final String listenerIdentifier = "listener-identifier";

        final ActivityFeedListener listener = (ActivityFeedListener) handler;

        when(activityFeedClientMock.registerListener(listener))
            .thenReturn(listenerIdentifier);

        final CompletableFuture<?> cf = handler.start();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());

        return listenerIdentifier;
    }
}
