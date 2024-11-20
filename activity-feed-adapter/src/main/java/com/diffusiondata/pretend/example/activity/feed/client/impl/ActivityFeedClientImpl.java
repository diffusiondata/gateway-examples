package com.diffusiondata.pretend.example.activity.feed.client.impl;

import java.util.Collection;

import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedClient;
import com.diffusiondata.pretend.example.activity.feed.client.ActivityFeedListener;
import com.diffusiondata.pretend.example.activity.feed.model.Activity;
import com.diffusiondata.pretend.example.activity.feed.service.ActivityFeedServer;
import com.diffusiondata.pretend.example.activity.feed.service.impl.PretendActivityFeedServerImpl;

import net.jcip.annotations.Immutable;

@Immutable
public final class ActivityFeedClientImpl
    implements ActivityFeedClient {

    private final ActivityFeedServer activityFeedServer;

    private ActivityFeedClientImpl(ActivityFeedServer activityFeedServer) {
        this.activityFeedServer = activityFeedServer;
    }

    @Override
    public String registerListener(ActivityFeedListener activityFeedListener) {
        return activityFeedServer.registerClientListener(activityFeedListener);
    }

    @Override
    public boolean unregisterListener(String listenerIdentifier) {
        return activityFeedServer.unregisterClientListener(listenerIdentifier);
    }

    @Override
    public Collection<Activity> getLatestActivities() {
        return activityFeedServer.getLatestActivities();
    }

    public static ActivityFeedClient connectToActivityFeedServer() {
        return connectToActivityFeedServer(PretendActivityFeedServerImpl
            .createAndStartActivityFeedServer());
    }

    /**
     * package for tests.
     */
    static ActivityFeedClient connectToActivityFeedServer(
        ActivityFeedServer activityFeedServer) {

        return new ActivityFeedClientImpl(activityFeedServer);
    }
}
