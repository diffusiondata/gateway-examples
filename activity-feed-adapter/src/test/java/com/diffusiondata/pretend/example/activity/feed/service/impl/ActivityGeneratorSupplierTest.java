package com.diffusiondata.pretend.example.activity.feed.service.impl;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsNull.notNullValue;

import java.time.Instant;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.diffusiondata.pretend.example.activity.feed.model.Activity;

import net.datafaker.Faker;

class ActivityGeneratorSupplierTest {
    private final Supplier<Activity> supplier =
        new ActivityGeneratorSupplier(new Faker());

    @Test
    void testGet() {
        final Activity activity = supplier.get();

        assertThat(activity, notNullValue());
        assertThat(activity.getSport(), notNullValue());
        assertThat(activity.getCountry(), notNullValue());
        assertThat(activity.getWinner(), notNullValue());
        assertThat(activity.getDateOfActivity(), notNullValue());
    }

    @Test
    void testTimeAndDatePast() {
        final ActivityGeneratorSupplier impl =
            (ActivityGeneratorSupplier) supplier;

        final Instant pastDate = impl.timeAndDatePast(2, MINUTES);

        assertThat(pastDate.toEpochMilli(),
            lessThan(System.currentTimeMillis()));
    }
}
