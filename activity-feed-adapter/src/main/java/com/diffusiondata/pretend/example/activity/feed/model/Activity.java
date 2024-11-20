package com.diffusiondata.pretend.example.activity.feed.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import net.jcip.annotations.Immutable;

@Immutable
public class Activity {
    private final String sport;
    private final String country;
    private final String winner;
    private final Instant dateOfActivity;

    public Activity(
        String sport,
        String country,
        String winner,
        Instant dateOfActivity) {

        this.sport = sport;
        this.country = country;
        this.winner = winner;
        this.dateOfActivity = dateOfActivity;
    }

    public String getSport() {
        return sport;
    }

    public String getCountry() {
        return country;
    }

    public String getWinner() {
        return winner;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    public Instant getDateOfActivity() {
        return dateOfActivity;
    }
}
