package com.example.weatherapp.service;

import com.example.weatherapp.exception.WeatherApiException;
import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.DailyForecast;
import com.example.weatherapp.model.Location;
import com.example.weatherapp.model.WeatherReport;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Service responsible for fetching real-time weather information and 5-day daily forecasts
 * from the Open-Meteo Forecast API.
 */
public class WeatherService {

    private static final String FORECAST_BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int FORECAST_DAYS = 5;

    private static final String CURRENT_VARS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m,wind_direction_10m";
    private static final String DAILY_VARS =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset";

    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Constructs a WeatherService with injected dependencies.
     *
     * @param httpClient Configured Java HttpClient.
     * @param gson       Gson serializer/deserializer.
     */
    public WeatherService(HttpClient httpClient, Gson gson) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    /**
     * Fetches current weather and a 5-day daily forecast for the specified location.
     *
     * @param location The geographic location containing latitude and longitude.
     * @return Complete {@link WeatherReport} containing current conditions and daily forecasts.
     * @throws WeatherApiException If network, HTTP status, or JSON parsing errors occur.
     */
    public WeatherReport getWeatherReport(Location location) throws WeatherApiException {
        Objects.requireNonNull(location, "location must not be null");

        // Format query URL using Locale.ROOT to ensure latitude/longitude use '.' decimal separators
        String endpointUrl = String.format(Locale.ROOT,
                "%s?latitude=%.6f&longitude=%.6f&current=%s&daily=%s&temperature_unit=celsius&wind_speed_unit=kmh&precipitation_unit=mm&forecast_days=%d&timezone=auto",
                FORECAST_BASE_URL,
                location.latitude(),
                location.longitude(),
                CURRENT_VARS,
                DAILY_VARS,
                FORECAST_DAYS
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Accept", "application/json")
                .header("User-Agent", "JavaWeatherApp/1.0")
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new WeatherApiException("Weather API service returned HTTP status " + response.statusCode(),
                        response.statusCode(), null);
            }

            ForecastApiResponse apiResponse = gson.fromJson(response.body(), ForecastApiResponse.class);
            if (apiResponse == null || apiResponse.current == null) {
                throw new WeatherApiException("Incomplete weather data received from the weather service.");
            }

            List<DailyForecast> dailyForecasts = convertDailyForecast(apiResponse.daily);

            return new WeatherReport(location, apiResponse.current, dailyForecasts);

        } catch (HttpTimeoutException e) {
            throw new WeatherApiException("The weather forecast request timed out. Please check your connection.", e);
        } catch (ConnectException | UnknownHostException e) {
            throw new WeatherApiException("Unable to connect to the weather service. Check your internet connection.", e);
        } catch (JsonParseException e) {
            throw new WeatherApiException("Failed to parse weather forecast response (malformed JSON).", e);
        } catch (IOException e) {
            throw new WeatherApiException("Network error while communicating with the weather service: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherApiException("The weather request was interrupted.", e);
        }
    }

    /**
     * Converts Open-Meteo's columnar daily forecast lists into a clean List of {@link DailyForecast} objects.
     */
    private List<DailyForecast> convertDailyForecast(DailyApiResponse daily) {
        if (daily == null || daily.time == null || daily.time.isEmpty()) {
            return Collections.emptyList();
        }

        List<DailyForecast> list = new ArrayList<>();
        int count = daily.time.size();

        for (int i = 0; i < count; i++) {
            String date = daily.time.get(i);
            int code = (daily.weatherCode != null && i < daily.weatherCode.size() && daily.weatherCode.get(i) != null)
                    ? daily.weatherCode.get(i) : -1;
            double maxTemp = (daily.tempMax != null && i < daily.tempMax.size() && daily.tempMax.get(i) != null)
                    ? daily.tempMax.get(i) : Double.NaN;
            double minTemp = (daily.tempMin != null && i < daily.tempMin.size() && daily.tempMin.get(i) != null)
                    ? daily.tempMin.get(i) : Double.NaN;
            int prob = (daily.precipProb != null && i < daily.precipProb.size() && daily.precipProb.get(i) != null)
                    ? daily.precipProb.get(i) : 0;
            String sunrise = (daily.sunrise != null && i < daily.sunrise.size()) ? daily.sunrise.get(i) : "N/A";
            String sunset = (daily.sunset != null && i < daily.sunset.size()) ? daily.sunset.get(i) : "N/A";

            list.add(new DailyForecast(date, code, maxTemp, minTemp, prob, sunrise, sunset));
        }

        return list;
    }

    /**
     * Internal DTO matching Open-Meteo's forecast response payload.
     */
    private static class ForecastApiResponse {
        @SerializedName("current")
        private CurrentWeather current;

        @SerializedName("daily")
        private DailyApiResponse daily;
    }

    /**
     * Internal DTO matching Open-Meteo's columnar daily forecast structure.
     */
    private static class DailyApiResponse {
        @SerializedName("time")
        private List<String> time;

        @SerializedName("weather_code")
        private List<Integer> weatherCode;

        @SerializedName("temperature_2m_max")
        private List<Double> tempMax;

        @SerializedName("temperature_2m_min")
        private List<Double> tempMin;

        @SerializedName("precipitation_probability_max")
        private List<Integer> precipProb;

        @SerializedName("sunrise")
        private List<String> sunrise;

        @SerializedName("sunset")
        private List<String> sunset;
    }
}
