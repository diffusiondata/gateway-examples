package com.diffusiondata.pretend.example.sportsactivity.feed.service.impl;

import static com.diffusiondata.pretend.example.sportsactivity.feed.model.ActivityTestUtils.createPopulatedActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.collection.IsMapWithSize.anEmptyMap;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.diffusiondata.pretend.example.sportsactivity.feed.service.SportsActivityFeedServer;

@ExtendWith(MockitoExtension.class)
class PretendSportsSportsActivityFeedServerImplTest {
    private static final String SPORT = "tennis";

    @Mock
    private ExecutorService executorServiceMock;

    @Mock
    private RandomSportsActivityGeneratorSupplier
        randomSportsActivityGeneratorSupplierMock;

    @Mock
    private SportsActivityFeedListener sportsActivityFeedListenerMock;

    private SportsActivityFeedServer sportsActivityFeedServer;

    @BeforeEach
    void beforeEachTest() {
        when(executorServiceMock.submit(any(Runnable.class)))
            .thenReturn(null);

        sportsActivityFeedServer =
            PretendSportsActivityFeedServerImpl
                .createAndStartActivityFeedServer(
                    executorServiceMock,
                    randomSportsActivityGeneratorSupplierMock,
                    0);
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(
            executorServiceMock,
            randomSportsActivityGeneratorSupplierMock,
            sportsActivityFeedListenerMock
        );
    }

    @Test
    @Order(10)
    void testRegisterClientListener() {
        final String listenerIdentifier =
            sportsActivityFeedServer.registerClientListener(
                sportsActivityFeedListenerMock);

        assertThat(listenerIdentifier, notNullValue());

        final Map<String, SportsActivityFeedListener> listeners =
            getImpl().getListeners();

        assertThat(listeners, aMapWithSize(1));
        assertThat(listeners, hasKey(listenerIdentifier));
        assertThat(listeners.get(listenerIdentifier),
            sameInstance(sportsActivityFeedListenerMock));
    }

    @Test
    @Order(20)
    void testUnregisterClientListenerWhenExists() {
        final String listenerIdentifier =
            sportsActivityFeedServer.registerClientListener(
                sportsActivityFeedListenerMock);

        final boolean result =
            sportsActivityFeedServer.unregisterClientListener(
                listenerIdentifier);

        assertThat(result, equalTo(true));

        final Map<String, SportsActivityFeedListener> listeners =
            getImpl().getListeners();

        assertThat(listeners, anEmptyMap());
    }

    @Test
    void testUnregisterClientListenerWhenDoesNotExists() {
        final boolean result =
            sportsActivityFeedServer.unregisterClientListener(
                "unknown-^^-listener-**-identifier");

        assertThat(result, equalTo(false));
    }

    @Test
    void testGetLatestSportsActivitiesWhenNoneGenerated() {
        final Collection<SportsActivity> latestActivities =
            sportsActivityFeedServer.getLatestSportsActivities();

        assertThat(latestActivities, emptyIterable());
    }

    @Test
    @Order(30)
    void testInternalUpdateStateAndListenersWhenListenerRegistered() {
        final SportsActivity sportsActivity = createPopulatedActivity(SPORT);

        doNothing().when(sportsActivityFeedListenerMock)
            .onMessage(sportsActivity);

        sportsActivityFeedServer.registerClientListener(
            sportsActivityFeedListenerMock);

        getImpl().internalUpdateStateAndListeners(sportsActivity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(33)
    void testInternalUpdateStateAndListenersWhenNoListenersRegistered() {
        final SportsActivity sportsActivity = createPopulatedActivity(SPORT);

        getImpl().internalUpdateStateAndListeners(sportsActivity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(36)
    void testInternalUpdateStateAndListenersWhenListenerRegisterAndExceptions() {
        final SportsActivity sportsActivity = createPopulatedActivity(SPORT);

        doThrow(IllegalStateException.class)
            .when(sportsActivityFeedListenerMock)
            .onMessage(sportsActivity);

        sportsActivityFeedServer.registerClientListener(
            sportsActivityFeedListenerMock);

        getImpl().internalUpdateStateAndListeners(sportsActivity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(40)
    void testGetLatestSportsActivitiesWhenSomeGenerated()
        throws Exception {

        final SportsActivity sportsActivity = createPopulatedActivity(SPORT);

        when(randomSportsActivityGeneratorSupplierMock.get())
            .thenReturn(sportsActivity);

        getImpl().runOnce();

        final Collection<SportsActivity> latestActivities =
            sportsActivityFeedServer.getLatestSportsActivities();

        assertThat(latestActivities, iterableWithSize(1));
        assertThat(latestActivities.iterator().next(), equalTo(sportsActivity));
    }

    private PretendSportsActivityFeedServerImpl getImpl() {
        return (PretendSportsActivityFeedServerImpl) sportsActivityFeedServer;
    }

    private void checkCachedLatestActivitiesAsExpected() {
        final Map<String, SportsActivity> cachedLatestSportsActivities =
            getImpl().getCachedSportsLatestActivities();

        assertThat(cachedLatestSportsActivities, aMapWithSize(1));
        assertThat(cachedLatestSportsActivities, hasKey(SPORT));
    }
}
