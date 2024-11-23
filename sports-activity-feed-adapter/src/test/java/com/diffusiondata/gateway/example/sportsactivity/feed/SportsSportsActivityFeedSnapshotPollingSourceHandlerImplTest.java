package com.diffusiondata.gateway.example.sportsactivity.feed;

import static com.diffusiondata.gateway.example.sportsactivity.feed.SportsActivityFeedSnapshotPollingSourceHandlerImpl.DEFAULT_POLLING_TOPIC_PATH;
import static com.diffusiondata.pretend.example.sportsactivity.feed.model.ActivityTestUtils.createPopulatedActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.gateway.framework.PollingSourceHandler;
import com.diffusiondata.gateway.framework.Publisher;
import com.diffusiondata.gateway.framework.ServiceDefinition;
import com.diffusiondata.gateway.framework.ServiceHandler.PauseReason;
import com.diffusiondata.gateway.framework.ServiceHandler.ResumeReason;
import com.diffusiondata.gateway.framework.ServiceState;
import com.diffusiondata.gateway.framework.StateHandler;
import com.diffusiondata.gateway.framework.exceptions.DiffusionClientException;
import com.diffusiondata.gateway.util.Util;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SportsSportsActivityFeedSnapshotPollingSourceHandlerImplTest {
    @Mock
    private SportsActivityFeedClient sportsActivityFeedClientMock;

    @Mock
    private ServiceDefinition serviceDefinitionMock;

    @Mock
    Publisher publisherMock;

    @Mock
    private StateHandler stateHandlerMock;

    @Mock
    private ObjectMapper objectMapperMock;

    private PollingSourceHandler handler;

    @BeforeEach
    void beforeEachTest() {
        when(serviceDefinitionMock.getParameters())
            .thenReturn(Map.of("topicPrefix", DEFAULT_POLLING_TOPIC_PATH));

        handler = new SportsActivityFeedSnapshotPollingSourceHandlerImpl(
            sportsActivityFeedClientMock,
            serviceDefinitionMock,
            publisherMock,
            stateHandlerMock,
            objectMapperMock);

        final String topicPrefix =
            ((SportsActivityFeedSnapshotPollingSourceHandlerImpl) handler)
                .getTopicPath();

        assertThat(topicPrefix, notNullValue());
        assertThat(topicPrefix, equalTo(DEFAULT_POLLING_TOPIC_PATH));
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(
            sportsActivityFeedClientMock,
            serviceDefinitionMock,
            publisherMock,
            stateHandlerMock,
            objectMapperMock
        );
    }

    @Test
    void testPollWhenServiceStateIsActiveAndLatestActivitiesIsEmpty() {
        final Collection<SportsActivity> activities = List.of();

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(sportsActivityFeedClientMock.getLatestActivities())
            .thenReturn(activities);

        final CompletableFuture<?> cf = handler.poll();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    void testPollWhenServiceStateIsActiveAndLatestActivitiesHasItems()
        throws Exception {

        final Collection<SportsActivity> activities =
            List.of(createPopulatedActivity());

        final String jsonAsString = "{}";

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(sportsActivityFeedClientMock.getLatestActivities())
            .thenReturn(activities);

        when(objectMapperMock.writeValueAsString(activities))
            .thenReturn(jsonAsString);

        when(publisherMock.publish(DEFAULT_POLLING_TOPIC_PATH, jsonAsString))
            .thenReturn(CompletableFuture.completedFuture(null));

        final CompletableFuture<?> cf = handler.poll();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    void testPollWhenServiceStateIsActiveAndPublishExceptionIsThrown()
        throws Exception {

        final Collection<SportsActivity> activities =
            List.of(createPopulatedActivity());

        final String jsonAsString = "{}";

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(sportsActivityFeedClientMock.getLatestActivities())
            .thenReturn(activities);

        when(objectMapperMock.writeValueAsString(activities))
            .thenReturn(jsonAsString);

        when(publisherMock.publish(DEFAULT_POLLING_TOPIC_PATH, jsonAsString))
            .thenReturn(Util.getCfWithException(
                new DiffusionClientException("ignore this is a test")));

        final CompletionException exception =
            assertThrows(CompletionException.class,
                () -> handler.poll().join());

        assertThat(exception.getCause(),
            instanceOf(DiffusionClientException.class));
    }

    @Test
    void testPollWhenServiceStateIsActiveAndCheckedExceptionIsThrown()
        throws Exception {

        final Collection<SportsActivity> activities =
            List.of(createPopulatedActivity());

        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.ACTIVE);

        when(sportsActivityFeedClientMock.getLatestActivities())
            .thenReturn(activities);

        doThrow(JsonProcessingException.class).when(objectMapperMock)
            .writeValueAsString(activities);

        final CompletionException exception =
            assertThrows(CompletionException.class,
                () -> handler.poll().join());

        assertThat(exception.getCause(),
            instanceOf(JsonProcessingException.class));
    }

    @Test
    void testPollWhenServiceStateIsNotActive() {
        when(stateHandlerMock.getState())
            .thenReturn(ServiceState.PAUSED);

        final CompletableFuture<?> cf = handler.poll();

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    void testPause() {
        final CompletableFuture<?> cf = handler.pause(PauseReason.REQUESTED);

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }

    @Test
    void testResume() {
        final CompletableFuture<?> cf = handler.resume(ResumeReason.REQUESTED);

        assertThat(cf, notNullValue());
        assertThat(cf.join(), nullValue());
    }
}
