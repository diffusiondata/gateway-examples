package com.diffusiondata.pretend.example.activity.feed.service;

import java.util.Collection;

import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;

public interface ActivityFeedServer {
    String registerClientListener(ActivityFeedListener activityFeedListener);

    boolean unregisterClientListener(String listenerIdentifier);

    Collection<Activity> getLatestActivities();
}
