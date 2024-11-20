package com.diffusiondata.pretend.example.activity.feed.model;

import java.time.Instant;

public final class ActivityTestUtils {
    private ActivityTestUtils() {
        // Private constructor to prevent creation
    }

    public static Activity createPopulatedActivity() {
        return createPopulatedActivity("s");
    }

    public static Activity createPopulatedActivity(String sport) {
        return new Activity(sport, "c", "w", Instant.now());
    }
}
