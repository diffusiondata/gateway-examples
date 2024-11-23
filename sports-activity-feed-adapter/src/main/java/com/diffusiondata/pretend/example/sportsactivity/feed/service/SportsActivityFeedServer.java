package com.diffusiondata.pretend.example.sportsactivity.feed.service;

import java.util.Collection;

import com.diffusiondata.pretend.example.sportsactivity.feed.client.SportsActivityFeedListener;
import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;

public interface SportsActivityFeedServer {
    String registerClientListener(SportsActivityFeedListener sportsActivityFeedListener);

    boolean unregisterClientListener(String listenerIdentifier);

    Collection<SportsActivity> getLatestSportsActivities();
}
