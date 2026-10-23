package com.example.weatherapp.model;

/**
 * Represents a single day's weather forecast.
 *
 * Open-Meteo returns daily forecasts in columnar arrays (e.g. array of dates,
 * array of max temps). The {@link com.example.weatherapp.service.WeatherService}
 * transforms those arrays into individual, easy-to-use {@code DailyForecast} objects.
 */
public record DailyForecast(
        String date,
        int weatherCode,
        double maxTemperature,
        double minTemperature,
        int precipitationProbability,
        String sunrise,
        String sunset
) {
    /**
     * Extracts only the time portion (HH:mm) from an ISO 8601 date-time string.
     * E.g., "2026-10-24T06:45" becomes "06:45".
     */
    public String formattedSunrise() {
        return extractTime(sunrise);
    }

    /**
     * Extracts only the time portion (HH:mm) from an ISO 8601 date-time string.
     * E.g., "2026-10-24T18:30" becomes "18:30".
     */
    public String formattedSunset() {
        return extractTime(sunset);
    }

    private static String extractTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) {
            return "N/A";
        }
        int tIndex = isoDateTime.indexOf('T');
        if (tIndex != -1 && tIndex + 1 < isoDateTime.length()) {
            return isoDateTime.substring(tIndex + 1);
        }
        return isoDateTime;
    }
}
