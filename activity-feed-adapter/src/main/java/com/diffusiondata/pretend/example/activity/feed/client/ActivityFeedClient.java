package com.diffusiondata.pretend.example.activity.feed.client;

import java.util.Collection;

import com.diffusiondata.pretend.example.activity.feed.model.Activity;

public interface ActivityFeedClient {
    String registerListener(ActivityFeedListener activityFeedListener);

    boolean unregisterListener(String listenerIdentifier);

    Collection<Activity> getLatestActivities();
}
