package com.diffusiondata.pretend.example.sportsactivity.feed.service.impl;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsNull.notNullValue;

import java.time.Instant;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.diffusiondata.pretend.example.sportsactivity.feed.model.SportsActivity;

import net.datafaker.Faker;

class SportsRandomSportsActivitySupplierTest {
    private final Supplier<SportsActivity> supplier =
        new RandomSportsActivitySupplier(new Faker());

    @Test
    void testGet() {
        final SportsActivity sportsActivity = supplier.get();

        assertThat(sportsActivity, notNullValue());
        assertThat(sportsActivity.getSport(), notNullValue());
        assertThat(sportsActivity.getCountry(), notNullValue());
        assertThat(sportsActivity.getWinner(), notNullValue());
        assertThat(sportsActivity.getDateOfActivity(), notNullValue());
    }

    @Test
    void testTimeAndDatePast() {
        final RandomSportsActivitySupplier impl =
            (RandomSportsActivitySupplier) supplier;

        final Instant pastDate = impl.timeAndDatePast(2, MINUTES);

        assertThat(pastDate.toEpochMilli(),
            lessThan(System.currentTimeMillis()));
    }
}
