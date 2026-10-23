package com.example.weatherapp.util;

/**
 * Utility class to map World Meteorological Organization (WMO) weather code integers
 * returned by the Open-Meteo API into human-readable weather descriptions.
 *
 * Reference: https://open-meteo.com/en/docs
 */
public final class WeatherCodeMapper {

    private WeatherCodeMapper() {
        // Private constructor prevents instantiation of utility class
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a numeric WMO weather interpretation code into a descriptive English string.
     * Uses Java modern switch expressions with pattern matching / multiple case labels.
     *
     * @param code The WMO weather integer code.
     * @return Descriptive weather condition, or a fallback string if the code is unknown.
     */
    public static String getConditionDescription(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45 -> "Fog";
            case 48 -> "Depositing rime fog";
            case 51 -> "Light drizzle";
            case 53 -> "Moderate drizzle";
            case 55 -> "Dense drizzle";
            case 56 -> "Light freezing drizzle";
            case 57 -> "Dense freezing drizzle";
            case 61 -> "Slight rain";
            case 63 -> "Moderate rain";
            case 65 -> "Heavy rain";
            case 66 -> "Light freezing rain";
            case 67 -> "Heavy freezing rain";
            case 71 -> "Slight snow fall";
            case 73 -> "Moderate snow fall";
            case 75 -> "Heavy snow fall";
            case 77 -> "Snow grains";
            case 80 -> "Slight rain showers";
            case 81 -> "Moderate rain showers";
            case 82 -> "Violent rain showers";
            case 85 -> "Slight snow showers";
            case 86 -> "Heavy snow showers";
            case 95 -> "Thunderstorm";
            case 96 -> "Thunderstorm with slight hail";
            case 99 -> "Thunderstorm with heavy hail";
            default -> "Unknown condition (code " + code + ")";
        };
    }
}
