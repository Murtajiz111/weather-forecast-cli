package com.example.weatherapp.service;

import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.DailyForecast;
import com.example.weatherapp.model.Location;
import com.example.weatherapp.util.WeatherCodeMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Offline Weather and Location Parsing Tests")
class WeatherParsingTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().create();
    }

    @Test
    @DisplayName("Parse Location JSON correctly with admin1 and country")
    void testParseLocationWithAdmin1() {
        String json = """
                {
                    "id": 1174872,
                    "name": "Karachi",
                    "latitude": 24.8608,
                    "longitude": 67.0104,
                    "country": "Pakistan",
                    "admin1": "Sindh",
                    "timezone": "Asia/Karachi"
                }
                """;

        Location location = gson.fromJson(json, Location.class);
        assertNotNull(location);
        assertEquals("Karachi", location.name());
        assertEquals("Pakistan", location.country());
        assertEquals("Sindh", location.admin1());
        assertEquals(24.8608, location.latitude());
        assertEquals(67.0104, location.longitude());
        assertEquals("Asia/Karachi", location.timezone());
        assertEquals("Karachi, Sindh, Pakistan", location.displayName());
    }

    @Test
    @DisplayName("Parse Location when admin1 is missing or null")
    void testParseLocationWithoutAdmin1() {
        String json = """
                {
                    "id": 1850147,
                    "name": "Tokyo",
                    "latitude": 35.6895,
                    "longitude": 139.6917,
                    "country": "Japan",
                    "timezone": "Asia/Tokyo"
                }
                """;

        Location location = gson.fromJson(json, Location.class);
        assertNotNull(location);
        assertEquals("Tokyo, Japan", location.displayName());
    }

    @Test
    @DisplayName("Parse CurrentWeather JSON block and verify field mapping")
    void testParseCurrentWeather() {
        String json = """
                {
                    "time": "2026-10-24T14:30",
                    "temperature_2m": 31.5,
                    "relative_humidity_2m": 65,
                    "apparent_temperature": 35.0,
                    "is_day": 1,
                    "precipitation": 0.0,
                    "weather_code": 2,
                    "wind_speed_10m": 18.0,
                    "wind_direction_10m": 220.0
                }
                """;

        CurrentWeather current = gson.fromJson(json, CurrentWeather.class);
        assertNotNull(current);
        assertEquals(31.5, current.temperature());
        assertEquals(35.0, current.apparentTemperature());
        assertEquals(65, current.relativeHumidity());
        assertEquals(0.0, current.precipitation());
        assertEquals(2, current.weatherCode());
        assertEquals(18.0, current.windSpeed());
        assertEquals(220.0, current.windDirection());
        assertTrue(current.isDaytime());
        assertEquals("Day", current.timeOfDayDescription());
        assertEquals("Partly cloudy", WeatherCodeMapper.getConditionDescription(current.weatherCode()));
    }

    @Test
    @DisplayName("DailyForecast extracts time cleanly from ISO string")
    void testDailyForecastFormattedTimes() {
        DailyForecast forecast = new DailyForecast(
                "2026-10-24",
                61,
                15.7,
                7.5,
                90,
                "2026-10-24T06:42",
                "2026-10-24T18:15"
        );

        assertEquals("06:42", forecast.formattedSunrise());
        assertEquals("18:15", forecast.formattedSunset());
    }
}
