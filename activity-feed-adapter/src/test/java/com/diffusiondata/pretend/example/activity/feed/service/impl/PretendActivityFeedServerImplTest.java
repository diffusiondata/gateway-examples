package com.diffusiondata.pretend.example.activity.feed.service.impl;

import static com.diffusiondata.pretend.example.activity.feed.model.ActivityTestUtils.createPopulatedActivity;
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

import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.diffusiondata.pretend.example.activity.feed.service.ActivityFeedServer;

@ExtendWith(MockitoExtension.class)
class PretendActivityFeedServerImplTest {
    private static final String SPORT = "tennis";

    @Mock
    private ExecutorService executorServiceMock;

    @Mock
    private ActivityGeneratorSupplier activityGeneratorSupplierMock;

    @Mock
    private ActivityFeedListener activityFeedListenerMock;

    private ActivityFeedServer activityFeedServer;

    @BeforeEach
    void beforeEachTest() {
        when(executorServiceMock.submit(any(Runnable.class)))
            .thenReturn(null);

        activityFeedServer =
            PretendActivityFeedServerImpl.createAndStartActivityFeedServer(
                executorServiceMock,
                activityGeneratorSupplierMock,
                0);
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(
            executorServiceMock,
            activityGeneratorSupplierMock,
            activityFeedListenerMock
        );
    }

    @Test
    @Order(10)
    void testRegisterClientListener() {
        final String listenerIdentifier =
            activityFeedServer.registerClientListener(activityFeedListenerMock);

        assertThat(listenerIdentifier, notNullValue());

        final Map<String, ActivityFeedListener> listeners =
            getImpl().getListeners();

        assertThat(listeners, aMapWithSize(1));
        assertThat(listeners, hasKey(listenerIdentifier));
        assertThat(listeners.get(listenerIdentifier),
            sameInstance(activityFeedListenerMock));
    }

    @Test
    @Order(20)
    void testUnregisterClientListenerWhenExists() {
        final String listenerIdentifier =
            activityFeedServer.registerClientListener(activityFeedListenerMock);

        final boolean result =
            activityFeedServer.unregisterClientListener(listenerIdentifier);

        assertThat(result, equalTo(true));

        final Map<String, ActivityFeedListener> listeners =
            getImpl().getListeners();

        assertThat(listeners, anEmptyMap());
    }

    @Test
    void testUnregisterClientListenerWhenDoesNotExists() {
        final boolean result =
            activityFeedServer.unregisterClientListener(
                "unknown-^^-listener-**-identifier");

        assertThat(result, equalTo(false));
    }

    @Test
    void testGetLatestActivitiesWhenNoneGenerated() {
        final Collection<Activity> latestActivities =
            activityFeedServer.getLatestActivities();

        assertThat(latestActivities, emptyIterable());
    }

    @Test
    @Order(30)
    void testInternalUpdateStateAndListenersWhenListenerRegistered() {
        final Activity activity = createPopulatedActivity(SPORT);

        doNothing().when(activityFeedListenerMock)
            .onMessage(activity);

        activityFeedServer.registerClientListener(activityFeedListenerMock);

        getImpl().internalUpdateStateAndListeners(activity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(33)
    void testInternalUpdateStateAndListenersWhenNoListenersRegistered() {
        final Activity activity = createPopulatedActivity(SPORT);

        getImpl().internalUpdateStateAndListeners(activity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(36)
    void testInternalUpdateStateAndListenersWhenListenerRegisterAndExceptions() {
        final Activity activity = createPopulatedActivity(SPORT);

        doThrow(IllegalStateException.class).when(activityFeedListenerMock)
            .onMessage(activity);

        activityFeedServer.registerClientListener(activityFeedListenerMock);

        getImpl().internalUpdateStateAndListeners(activity);

        checkCachedLatestActivitiesAsExpected();
    }

    @Test
    @Order(40)
    void testGetLatestActivitiesWhenSomeGenerated()
        throws Exception {

        final Activity activity = createPopulatedActivity(SPORT);

        when(activityGeneratorSupplierMock.get())
            .thenReturn(activity);

        getImpl().runOnce();

        final Collection<Activity> latestActivities =
            activityFeedServer.getLatestActivities();

        assertThat(latestActivities, iterableWithSize(1));
        assertThat(latestActivities.iterator().next(), equalTo(activity));
    }

    private PretendActivityFeedServerImpl getImpl() {
        return (PretendActivityFeedServerImpl) activityFeedServer;
    }

    private void checkCachedLatestActivitiesAsExpected() {
        final Map<String, Activity> cachedLatestActivities =
            getImpl().getCachedLatestActivities();

        assertThat(cachedLatestActivities, aMapWithSize(1));
        assertThat(cachedLatestActivities, hasKey(SPORT));
    }
}
