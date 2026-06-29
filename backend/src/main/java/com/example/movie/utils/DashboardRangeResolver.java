package com.example.movie.utils;

public final class DashboardRangeResolver {

    private DashboardRangeResolver() {
    }

    public static int normalize(Integer range) {
        if (range == null) {
            return 30;
        }
        return switch (range) {
            case 7, 30, 90 -> range;
            default -> 30;
        };
    }
}
