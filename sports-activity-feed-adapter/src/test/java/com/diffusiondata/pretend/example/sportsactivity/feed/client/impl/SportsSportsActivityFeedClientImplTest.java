package com.diffusiondata.pretend.example.sportsactivity.feed.client.impl;

import static com.diffusiondata.pretend.example.sportsactivity.feed.model.ActivityTestUtils.createPopulatedActivity;
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

import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.diffusiondata.pretend.example.sportsactivity.feed.service.SportsActivityFeedServer;

@ExtendWith(MockitoExtension.class)
class SportsSportsActivityFeedClientImplTest {
    @Mock
    private SportsActivityFeedServer sportsActivityFeedServerMock;

    private SportsActivityFeedClient sportsActivityFeedClient;

    @BeforeEach
    void beforeEachTest() {
        sportsActivityFeedClient =
            SportsActivityFeedClientImpl.connectToActivityFeedServer(sportsActivityFeedServerMock);
    }

    @AfterEach
    void afterEachTest() {
        verifyNoMoreInteractions(sportsActivityFeedServerMock);
    }

    @Test
    void testRegisterListener() {
        final String expectedIdentifier = "listener-id-1";
        final SportsActivityFeedListener listener = mock(SportsActivityFeedListener.class);

        when(sportsActivityFeedServerMock.registerClientListener(listener))
            .thenReturn(expectedIdentifier);

        final String listenerIdentifier =
            sportsActivityFeedClient.registerListener(listener);

        assertThat(listenerIdentifier, equalTo(expectedIdentifier));
    }

    @Test
    void testUnregisterListener() {
        when(sportsActivityFeedServerMock.unregisterClientListener("abc"))
            .thenReturn(true);

        final boolean result =
            sportsActivityFeedClient.unregisterListener("abc");

        assertThat(result, equalTo(true));
    }

    @Test
    void testGetActivityFeed() {
        final Collection<SportsActivity> expectedActivities =
            List.of(createPopulatedActivity());

        when(sportsActivityFeedServerMock.getLatestSportsActivities())
            .thenReturn(expectedActivities);

        final Collection<SportsActivity> latestActivities =
            sportsActivityFeedClient.getLatestActivities();

        assertThat(latestActivities, equalTo(expectedActivities));
    }
}
