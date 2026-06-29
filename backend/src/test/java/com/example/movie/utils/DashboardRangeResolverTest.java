package com.example.movie.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardRangeResolverTest {

    @Test
    void shouldKeepAllowedRanges() {
        assertEquals(7, DashboardRangeResolver.normalize(7));
        assertEquals(30, DashboardRangeResolver.normalize(30));
        assertEquals(90, DashboardRangeResolver.normalize(90));
    }

    @Test
    void shouldFallbackToThirtyForInvalidValues() {
        assertEquals(30, DashboardRangeResolver.normalize(null));
        assertEquals(30, DashboardRangeResolver.normalize(0));
        assertEquals(30, DashboardRangeResolver.normalize(45));
    }
}
