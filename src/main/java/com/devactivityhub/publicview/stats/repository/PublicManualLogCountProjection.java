package com.devactivityhub.publicview.stats.repository;

public interface PublicManualLogCountProjection {

    String getActivityType();

    long getCount();
}
