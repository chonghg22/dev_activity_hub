package com.devactivityhub.publicview.stats.repository;

public interface PublicProjectCountProjection {

    String getProjectSlug();

    String getProjectName();

    long getCount();
}
