package com.diffusiondata.pretend.example.activity.feed.client.impl;

import static com.diffusiondata.pretend.example.activity.feed.model.ActivityTestUtils.createPopulatedActivity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.diffusiondata.pretend.example.activity.feed.service.ActivityFeedServer;

@ExtendWith(MockitoExtension.class)
class ActivityFeedClientImplTest {
    @Mock
    private ActivityFeedServer activityFeedServerMock;

    private ActivityFeedClient activityFeedClient;

    @BeforeEach
    void beforeEachTest() {
        activityFeedClient =
            ActivityFeedClientImpl.connectToActivityFeedServer(activityFeedServerMock);
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(activityFeedServerMock);
    }

    @Test
    void testRegisterListener() {
        final String expectedIdentifier = "listener-id-1";
        final ActivityFeedListener listener = mock(ActivityFeedListener.class);

        when(activityFeedServerMock.registerClientListener(listener))
            .thenReturn(expectedIdentifier);

        final String listenerIdentifier =
            activityFeedClient.registerListener(listener);

        assertThat(listenerIdentifier, equalTo(expectedIdentifier));
    }

    @Test
    void testUnregisterListener() {
        when(activityFeedServerMock.unregisterClientListener("abc"))
            .thenReturn(true);

        final boolean result =
            activityFeedClient.unregisterListener("abc");

        assertThat(result, equalTo(true));
    }

    @Test
    void testGetActivityFeed() {
        final Collection<Activity> expectedActivities =
            List.of(createPopulatedActivity());

        when(activityFeedServerMock.getLatestActivities())
            .thenReturn(expectedActivities);

        final Collection<Activity> latestActivities =
            activityFeedClient.getLatestActivities();

        assertThat(latestActivities, equalTo(expectedActivities));
    }
}
