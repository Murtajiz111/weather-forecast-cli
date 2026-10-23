package com.example.weatherapp.model;

import java.util.Collections;
import java.util.List;

/**
 * Aggregates all weather information retrieved for a location:
 * - Selected geographic location
 * - Current weather snapshot
 * - Five-day daily forecast list
 */
public record WeatherReport(
        Location location,
        CurrentWeather current,
        List<DailyForecast> dailyForecasts
) {
    public WeatherReport {
        dailyForecasts = (dailyForecasts != null) ? List.copyOf(dailyForecasts) : Collections.emptyList();
    }
}
