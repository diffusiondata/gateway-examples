package com.diffusiondata.pretend.example.sportsactivity.feed.client;

import java.util.Collection;

import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;

public interface SportsActivityFeedClient {
    String registerListener(
        SportsActivityFeedListener sportsActivityFeedListener);

    boolean unregisterListener(String listenerIdentifier);

    Collection<SportsActivity> getLatestSportsActivities();
}
