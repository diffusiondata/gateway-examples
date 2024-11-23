package com.diffusiondata.pretend.example.sportsactivity.feed.client;

import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;

public interface SportsActivityFeedListener {
    void onMessage(SportsActivity sportsActivity);
}
