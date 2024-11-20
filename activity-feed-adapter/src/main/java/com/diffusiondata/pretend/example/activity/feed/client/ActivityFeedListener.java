package com.diffusiondata.pretend.example.activity.feed.client;

import com.diffusiondata.pretend.example.activity.feed.model.Activity;

public interface ActivityFeedListener {
    void onMessage(Activity activity);
}
