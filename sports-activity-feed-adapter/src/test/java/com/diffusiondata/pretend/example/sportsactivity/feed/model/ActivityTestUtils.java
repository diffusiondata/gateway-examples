package com.diffusiondata.pretend.example.sportsactivity.feed.model;

import java.time.Instant;

public final class ActivityTestUtils {
    private ActivityTestUtils() {
        // Private constructor to prevent creation
    }

    public static SportsActivity createPopulatedActivity() {
        return createPopulatedActivity("some-sport");
    }

    public static SportsActivity createPopulatedActivity(String sport) {
        return new SportsActivity(sport, "c", "w", Instant.now());
    }
}
