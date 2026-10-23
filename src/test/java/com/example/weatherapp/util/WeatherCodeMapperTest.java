package com.example.weatherapp.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WeatherCodeMapper Tests")
class WeatherCodeMapperTest {

    @Test
    @DisplayName("Code 0 maps to Clear sky")
    void testClearSky() {
        assertEquals("Clear sky", WeatherCodeMapper.getConditionDescription(0));
    }

    @ParameterizedTest(name = "Code {0} should map to \"{1}\"")
    @CsvSource({
            "1, Mainly clear",
            "2, Partly cloudy",
            "3, Overcast"
    })
    @DisplayName("Cloudy condition codes (1, 2, 3)")
    void testCloudyConditions(int code, String expected) {
        assertEquals(expected, WeatherCodeMapper.getConditionDescription(code));
    }

    @ParameterizedTest(name = "Fog code {0} should map to \"{1}\"")
    @CsvSource({
            "45, Fog",
            "48, Depositing rime fog"
    })
    @DisplayName("Fog condition codes (45, 48)")
    void testFogConditions(int code, String expected) {
        assertEquals(expected, WeatherCodeMapper.getConditionDescription(code));
    }

    @ParameterizedTest(name = "Rain/snow code {0} should map to \"{1}\"")
    @CsvSource({
            "51, Light drizzle",
            "53, Moderate drizzle",
            "55, Dense drizzle",
            "56, Light freezing drizzle",
            "57, Dense freezing drizzle",
            "61, Slight rain",
            "63, Moderate rain",
            "65, Heavy rain",
            "66, Light freezing rain",
            "67, Heavy freezing rain",
            "71, Slight snow fall",
            "73, Moderate snow fall",
            "75, Heavy snow fall",
            "77, Snow grains",
            "80, Slight rain showers",
            "81, Moderate rain showers",
            "82, Violent rain showers",
            "85, Slight snow showers",
            "86, Heavy snow showers"
    })
    @DisplayName("Rain, drizzle, and snow condition codes")
    void testRainAndSnowConditions(int code, String expected) {
        assertEquals(expected, WeatherCodeMapper.getConditionDescription(code));
    }

    @ParameterizedTest(name = "Thunderstorm code {0} should map to \"{1}\"")
    @CsvSource({
            "95, Thunderstorm",
            "96, Thunderstorm with slight hail",
            "99, Thunderstorm with heavy hail"
    })
    @DisplayName("Thunderstorm condition codes (95, 96, 99)")
    void testThunderstormConditions(int code, String expected) {
        assertEquals(expected, WeatherCodeMapper.getConditionDescription(code));
    }

    @ParameterizedTest
    @ValueSource(ints = {-10, -1, 4, 50, 70, 100, 999})
    @DisplayName("Unknown weather codes fall back safely without error")
    void testUnknownWeatherCodes(int unknownCode) {
        String description = WeatherCodeMapper.getConditionDescription(unknownCode);
        assertNotNull(description);
        assertTrue(description.contains("Unknown condition"),
                "Description should state 'Unknown condition' for code: " + unknownCode);
        assertTrue(description.contains(String.valueOf(unknownCode)),
                "Description should include the numeric code for debugging: " + unknownCode);
    }

    @Test
    @DisplayName("Utility class constructor is private and throws exception")
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<WeatherCodeMapper> constructor = WeatherCodeMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
    }
}
