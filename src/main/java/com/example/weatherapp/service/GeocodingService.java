package com.example.weatherapp.service;

import com.example.weatherapp.exception.WeatherApiException;
import com.example.weatherapp.model.Location;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for resolving city names to geographic coordinates
 * using the Open-Meteo Geocoding API.
 */
public class GeocodingService {

    private static final String GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int RESULT_COUNT = 5;

    private final HttpClient httpClient;
    private final Gson gson;

    /**
     * Constructs a GeocodingService with injected dependencies for testability.
     *
     * @param httpClient Configured Java HttpClient instance.
     * @param gson       Gson serializer/deserializer.
     */
    public GeocodingService(HttpClient httpClient, Gson gson) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.gson = Objects.requireNonNull(gson, "gson must not be null");
    }

    /**
     * Searches for locations matching the given city name.
     *
     * @param cityName The name of the city to look up (must not be empty).
     * @return A list of matching {@link Location} objects, up to 5 entries. Returns empty list if no matches.
     * @throws WeatherApiException If a network error, timeout, HTTP error, or parsing failure occurs.
     */
    public List<Location> searchCity(String cityName) throws WeatherApiException {
        if (cityName == null || cityName.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or blank");
        }

        // Construct query parameter with URL encoding to handle special characters, spaces, accents
        String encodedCity = URLEncoder.encode(cityName.trim(), StandardCharsets.UTF_8);
        String endpointUrl = String.format("%s?name=%s&count=%d&language=en&format=json",
                GEOCODING_BASE_URL, encodedCity, RESULT_COUNT);

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
                throw new WeatherApiException("Geocoding service returned an unexpected HTTP error: " + response.statusCode(),
                        response.statusCode(), null);
            }

            GeocodingApiResponse apiResponse = gson.fromJson(response.body(), GeocodingApiResponse.class);
            if (apiResponse == null || apiResponse.results == null) {
                return Collections.emptyList();
            }

            return apiResponse.results;

        } catch (HttpTimeoutException e) {
            throw new WeatherApiException("The request to the geocoding service timed out. Please check your connection.", e);
        } catch (ConnectException | UnknownHostException e) {
            throw new WeatherApiException("Unable to reach the geocoding server. Please verify your internet connection.", e);
        } catch (JsonParseException e) {
            throw new WeatherApiException("Failed to parse response from geocoding server (malformed JSON).", e);
        } catch (IOException e) {
            throw new WeatherApiException("A network error occurred while communicating with the geocoding service: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            // Restore interrupted status as per Java concurrency best practices
            Thread.currentThread().interrupt();
            throw new WeatherApiException("The geocoding search operation was interrupted.", e);
        }
    }

    /**
     * Internal DTO to match the Open-Meteo Geocoding JSON response root structure.
     */
    private static class GeocodingApiResponse {
        @SerializedName("results")
        private List<Location> results;
    }
}
