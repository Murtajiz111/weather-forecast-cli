package com.example.weatherapp.ui;

import com.example.weatherapp.exception.WeatherApiException;
import com.example.weatherapp.model.CurrentWeather;
import com.example.weatherapp.model.DailyForecast;
import com.example.weatherapp.model.Location;
import com.example.weatherapp.model.WeatherReport;
import com.example.weatherapp.service.GeocodingService;
import com.example.weatherapp.service.WeatherService;
import com.example.weatherapp.util.WeatherCodeMapper;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

/**
 * Handles all user interactions, console menus, input validation, and output formatting.
 */
public class ConsoleUI {

    private final GeocodingService geocodingService;
    private final WeatherService weatherService;
    private final Scanner scanner;

    /**
     * Constructs the ConsoleUI with injected services.
     */
    public ConsoleUI(GeocodingService geocodingService, WeatherService weatherService) {
        this.geocodingService = Objects.requireNonNull(geocodingService, "geocodingService must not be null");
        this.weatherService = Objects.requireNonNull(weatherService, "weatherService must not be null");
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main application loop.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = readLine("Enter your choice: ");
            if (choice == null) {
                System.out.println("\nExiting application.");
                break;
            }

            switch (choice) {
                case "1" -> handleSearchFlow();
                case "2" -> {
                    System.out.println("\nThank you for using Weather Information App. Goodbye!");
                    running = false;
                }
                default -> System.out.println("\n[!] Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    /**
     * Guides the user through searching for a city, picking a match, and viewing weather.
     */
    private void handleSearchFlow() {
        boolean searchAgain = true;

        while (searchAgain) {
            String cityName = promptForCityName();
            if (cityName == null) {
                // User entered empty string or stream closed
                return;
            }

            try {
                System.out.println("\nSearching for '" + cityName + "'...");
                List<Location> locations = geocodingService.searchCity(cityName);

                if (locations.isEmpty()) {
                    System.out.println("City not found. Please check the spelling or try another city.");
                } else {
                    Location selectedLocation = resolveLocation(locations, cityName);
                    if (selectedLocation != null) {
                        displayWeatherForLocation(selectedLocation);
                    }
                }

                // After search, offer navigation options
                searchAgain = promptPostSearchNavigation();

            } catch (WeatherApiException e) {
                System.out.println("\n[Error] Unable to retrieve weather data: " + e.getMessage());
                searchAgain = promptPostSearchNavigation();
            }
        }
    }

    /**
     * Prompts the user to enter a non-empty city name.
     */
    private String promptForCityName() {
        while (true) {
            System.out.println("\n========================================");
            String input = readLine("Enter city name (e.g. Karachi, London, Tokyo) or '0' to cancel: ");
            if (input == null || input.equals("0")) {
                return null;
            }
            if (input.isBlank()) {
                System.out.println("[!] City name cannot be empty. Please enter a valid name.");
                continue;
            }
            return input.trim();
        }
    }

    /**
     * Resolves the desired location if multiple matches were found.
     */
    private Location resolveLocation(List<Location> locations, String query) {
        if (locations.size() == 1) {
            return locations.get(0);
        }

        System.out.println("\nMultiple locations matched '" + query + "'. Please choose one:");
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            String tzInfo = (loc.timezone() != null && !loc.timezone().isBlank())
                    ? " [Timezone: " + loc.timezone() + "]"
                    : "";
            System.out.printf("  %d. %s (Lat: %.2f, Lon: %.2f)%s%n",
                    (i + 1), loc.displayName(), loc.latitude(), loc.longitude(), tzInfo);
        }

        int selectedIndex = readIntChoice("Select location number (1-" + locations.size() + "): ", 1, locations.size());
        return locations.get(selectedIndex - 1);
    }

    /**
     * Fetches and prints current conditions and 5-day forecast for the given location.
     */
    private void displayWeatherForLocation(Location location) throws WeatherApiException {
        System.out.println("\nFetching weather data for " + location.displayName() + "...");
        WeatherReport report = weatherService.getWeatherReport(location);

        printCurrentWeather(report.location(), report.current());
        printDailyForecast(report.dailyForecasts());
    }

    /**
     * Prints the current weather block in the requested format.
     */
    private void printCurrentWeather(Location location, CurrentWeather current) {
        String condition = WeatherCodeMapper.getConditionDescription(current.weatherCode());

        System.out.println("\n========================================");
        System.out.println("            CURRENT WEATHER             ");
        System.out.println("========================================");
        System.out.println("Location:       " + location.displayName());
        System.out.printf(Locale.ROOT, "Temperature:    %.1f °C%n", current.temperature());
        System.out.printf(Locale.ROOT, "Feels Like:     %.1f °C%n", current.apparentTemperature());
        System.out.println("Condition:      " + condition);
        System.out.println("Humidity:       " + current.relativeHumidity() + "%");
        System.out.printf(Locale.ROOT, "Wind Speed:     %.1f km/h%n", current.windSpeed());
        System.out.printf(Locale.ROOT, "Wind Direction: %.0f°%n", current.windDirection());
        System.out.printf(Locale.ROOT, "Precipitation:  %.1f mm%n", current.precipitation());
        System.out.println("Time of Day:    " + current.timeOfDayDescription());
        System.out.println("========================================");
    }

    /**
     * Prints the 5-day daily forecast in a neat tabular layout.
     */
    private void printDailyForecast(List<DailyForecast> dailyList) {
        System.out.println("\n---------------------------------------------------------------------------------------------------------");
        System.out.println("                                         5-DAY WEATHER FORECAST                                          ");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-26s | %-10s | %-10s | %-14s | %-8s | %-8s%n",
                "Date", "Condition", "Max Temp", "Min Temp", "Precip. Prob.", "Sunrise", "Sunset");
        System.out.println("-------------+----------------------------+------------+------------+----------------+----------+--------");

        if (dailyList.isEmpty()) {
            System.out.println("No daily forecast data available.");
        } else {
            for (DailyForecast day : dailyList) {
                String condition = WeatherCodeMapper.getConditionDescription(day.weatherCode());
                String maxStr = Double.isNaN(day.maxTemperature()) ? "N/A" : String.format(Locale.ROOT, "%.1f °C", day.maxTemperature());
                String minStr = Double.isNaN(day.minTemperature()) ? "N/A" : String.format(Locale.ROOT, "%.1f °C", day.minTemperature());
                String probStr = day.precipitationProbability() + "%";

                System.out.printf("%-12s | %-26s | %-10s | %-10s | %-14s | %-8s | %-8s%n",
                        day.date(),
                        truncate(condition, 26),
                        maxStr,
                        minStr,
                        probStr,
                        day.formattedSunrise(),
                        day.formattedSunset()
                );
            }
        }
        System.out.println("---------------------------------------------------------------------------------------------------------\n");
    }

    /**
     * Displays post-search navigation choices and returns true if the user wants to search another city.
     */
    private boolean promptPostSearchNavigation() {
        while (true) {
            System.out.println("Next action:");
            System.out.println("  1. Search another city");
            System.out.println("  2. Return to main menu");
            System.out.println("  3. Exit application");
            String choice = readLine("Enter your choice: ");
            if (choice == null) {
                System.out.println("\nExiting application. Goodbye!");
                System.exit(0);
                return false;
            }

            switch (choice) {
                case "1" -> {
                    return true;
                }
                case "2" -> {
                    return false;
                }
                case "3" -> {
                    System.out.println("\nThank you for using Weather Information App. Goodbye!");
                    System.exit(0);
                    return false;
                }
                default -> System.out.println("[!] Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void printBanner() {
        System.out.println("==================================================");
        System.out.println("         Java Weather Information App             ");
        System.out.println("   Powered by Open-Meteo Public Weather API       ");
        System.out.println("==================================================");
    }

    private void printMainMenu() {
        System.out.println("\nMAIN MENU");
        System.out.println("  1. Search weather by city");
        System.out.println("  2. Exit");
    }

    /**
     * Reads a full line of text safely from the console.
     * Returns null if End-Of-File (EOF) is reached.
     */
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return null;
    }

    /**
     * Validates that user input is an integer within [min, max].
     */
    private int readIntChoice(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println("\n[!] End of input reached. Exiting.");
                System.exit(0);
            }
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("[!] Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("[!] '%s' is not a valid number. Please try again.%n", input);
            }
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
