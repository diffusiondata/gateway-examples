package com.diffusiondata.pretend.example.sportsactivity.feed.model;

import java.time.Instant;

public final class SportsActivityTestUtils {
    private SportsActivityTestUtils() {
        // Private constructor to prevent creation
    }

    public static SportsActivity createPopulatedSportsActivity() {
        return createPopulatedSportsActivity("some-sport");
    }

    public static SportsActivity createPopulatedSportsActivity(String sport) {
        return new SportsActivity(sport, "c", "w", Instant.now());
    }
}
