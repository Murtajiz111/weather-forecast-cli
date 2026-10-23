package com.example.weatherapp;

import com.example.weatherapp.service.GeocodingService;
import com.example.weatherapp.service.WeatherService;
import com.example.weatherapp.ui.ConsoleUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Application entry point for the Java Weather Information App.
 *
 * Demonstrates:
 * - Clean composition root
 * - Constructor-based dependency injection
 * - Centralized HttpClient and Gson configuration
 */
public class Main {

    public static void main(String[] args) {
        // Ensure UTF-8 output encoding for console symbols (e.g. ° degree symbol)
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));

        // 1. Configure the shared Java HttpClient with a connection timeout
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        // 2. Configure Gson instance
        Gson gson = new GsonBuilder().create();

        // 3. Instantiate domain services by injecting their dependencies
        GeocodingService geocodingService = new GeocodingService(httpClient, gson);
        WeatherService weatherService = new WeatherService(httpClient, gson);

        // 4. Instantiate the UI and start the application console loop
        ConsoleUI consoleUI = new ConsoleUI(geocodingService, weatherService);
        consoleUI.start();
    }
}
