package com.diffusiondata.pretend.example.activity.feed.service.impl;

import static java.util.concurrent.TimeUnit.DAYS;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.diffusiondata.pretend.example.activity.feed.model.Activity;

import net.datafaker.Faker;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public final class ActivityGeneratorSupplier
    implements Supplier<Activity> {

    private final Faker faker;

    public ActivityGeneratorSupplier(Faker faker) {
        this.faker = faker;
    }

    @Override
    public Activity get() {
        final String sport = faker.olympicSport().summerOlympics();
        final String country = faker.country().name();
        final String winner = faker.name().fullName();
        final Instant dateOfActivity = timeAndDatePast(1, DAYS);

        return new Activity(
            sport,
            country,
            winner,
            dateOfActivity);
    }

    /**
     * package for tests.
     */
    Instant timeAndDatePast(
        long atMost,
        TimeUnit timeUnit) {

        // Newer versions of Faker have timeAndDate().past(..).  However,
        // because we're using Java 11, the code from newer version of
        // DataFaker has essentially been copied and put here to provide
        // the same function.
        final Instant aBitEarlierThanNow =
            Instant.now().minusMillis(1);

        final long upperBoundMillis = timeUnit.toMillis(atMost);
        final long aBitFurtherBack =
            faker.random().nextLong(upperBoundMillis - 1);

        final long pastMillis =
            (aBitEarlierThanNow.toEpochMilli() - 1) - aBitFurtherBack;

        return Instant.ofEpochMilli(pastMillis);
    }
}
