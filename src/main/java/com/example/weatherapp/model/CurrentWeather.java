package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the current weather data returned from Open-Meteo's forecast API.
 * 
 * Maps directly to the JSON object under the "current" key.
 */
public record CurrentWeather(
        @SerializedName("time") String time,
        @SerializedName("temperature_2m") double temperature,
        @SerializedName("relative_humidity_2m") int relativeHumidity,
        @SerializedName("apparent_temperature") double apparentTemperature,
        @SerializedName("is_day") int isDay,
        @SerializedName("precipitation") double precipitation,
        @SerializedName("weather_code") int weatherCode,
        @SerializedName("wind_speed_10m") double windSpeed,
        @SerializedName("wind_direction_10m") double windDirection
) {
    /**
     * Checks if it is currently daytime at the requested location.
     * Open-Meteo returns 1 for day and 0 for night.
     */
    public boolean isDaytime() {
        return isDay == 1;
    }

    /**
     * Returns a readable description for day or night.
     */
    public String timeOfDayDescription() {
        return isDaytime() ? "Day" : "Night";
    }
}
