package com.diffusiondata.pretend.example.sportsactivity.feed.client.impl;

import java.util.Collection;

import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedClient;
import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;
import com.diffusiondata.pretend.example.sportsactivity.feed.service.SportsActivityFeedServer;
import com.diffusiondata.pretend.example.sportsactivity.feed.service.impl.PretendSportsActivityFeedServerImpl;

import net.jcip.annotations.Immutable;

@Immutable
public final class SportsActivityFeedClientImpl
    implements SportsActivityFeedClient {

    private final SportsActivityFeedServer sportsActivityFeedServer;

    private SportsActivityFeedClientImpl(
        SportsActivityFeedServer sportsActivityFeedServer) {

        this.sportsActivityFeedServer = sportsActivityFeedServer;
    }

    @Override
    public String registerListener(
        SportsActivityFeedListener sportsActivityFeedListener) {

        return sportsActivityFeedServer.registerClientListener(
            sportsActivityFeedListener);
    }

    @Override
    public boolean unregisterListener(String listenerIdentifier) {
        return sportsActivityFeedServer.unregisterClientListener(
            listenerIdentifier);
    }

    @Override
    public Collection<SportsActivity> getLatestSportsActivities() {
        return sportsActivityFeedServer.getLatestSportsActivities();
    }

    public static SportsActivityFeedClient connectToActivityFeedServer() {
        return connectToActivityFeedServer(PretendSportsActivityFeedServerImpl
            .createAndStartActivityFeedServer());
    }

    /**
     * package for tests.
     */
    static SportsActivityFeedClient connectToActivityFeedServer(
        SportsActivityFeedServer sportsActivityFeedServer) {

        return new SportsActivityFeedClientImpl(sportsActivityFeedServer);
    }
}
