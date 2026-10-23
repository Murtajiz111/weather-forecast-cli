# Java Weather Information App

A clean, modern, beginner-friendly yet intermediate-level console weather application written in **Java 21**. It searches for any city in the world and retrieves real-time weather and 5-day daily forecasts from Open-Meteo's free, public APIs without requiring external API keys.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Key Features](#key-features)
3. [Technologies & Libraries](#technologies--libraries)
4. [Prerequisites & Environment Setup](#prerequisites--environment-setup)
5. [Project Architecture & Directory Structure](#project-architecture--directory-structure)
6. [How the APIs Work](#how-the-apis-work)
7. [Building and Running the Application](#building-and-running-the-application)
8. [Running Unit Tests](#running-unit-tests)
9. [Error Handling & Edge Cases](#error-handling--edge-cases)
10. [Troubleshooting Guide](#troubleshooting-guide)
11. [Official API Documentation](#official-api-documentation)

---

## Project Overview

This project was built to illustrate real-world, idiomatic Java development principles:
- **Separation of Concerns:** Distinct layers for domain models, UI, business/network services, and utilities.
- **Modern Java Idioms:** Uses Java 16+ `record` types, Java 11+ `java.net.http.HttpClient`, and Java 14+ `switch expressions`.
- **Zero API Keys:** Relies exclusively on Open-Meteo's open-access endpoints.
- **Fail-Safe Robustness:** Catches timeouts, connection drops, and malformed JSON payloads gracefully without crashing the console loop.

---

## Key Features

1. **Interactive Console Menu:**
   - Option 1: Search weather by city
   - Option 2: Exit
   - Strict input validation prevents crashes when non-numeric or blank inputs are entered.

2. **City Geocoding & Disambiguation:**
   - Queries Open-Meteo Geocoding API (`https://geocoding-api.open-meteo.com/v1/search`).
   - If multiple cities match (e.g., *London, UK* vs. *London, Ontario, Canada*), presents a numbered list for the user to select.
   - Handles missing cities with clear error messages.

3. **Current Weather Conditions:**
   - Reports: Temperature (°C), Apparent/Feels-Like Temperature (°C), WMO Condition Description, Relative Humidity (%), Wind Speed (km/h), Wind Direction (°), Precipitation (mm), and Time of Day (Day / Night).

4. **WMO Weather Code Mapping:**
   - Translates numeric WMO codes (0 through 99) into human-readable descriptions (e.g. *Clear sky*, *Heavy rain*, *Thunderstorm with hail*).

5. **5-Day Weather Forecast:**
   - Formatted ASCII table showing Date, Weather Condition, High/Low Temperatures, Precipitation Probability (%), and Sunrise/Sunset times.

6. **Post-Search Navigation:**
   - Easily search another city, return to the main menu, or exit the program.

---

## Technologies & Libraries

- **Java 21 (or newer):** Utilizes language features like Records, Modern Switch Expressions, and Text Blocks.
- **Apache Maven 3.9+:** Project build management and dependency resolution.
- **Java HttpClient (`java.net.http`):** Standard built-in HTTP client introduced in Java 11 with built-in connection and request timeouts.
- **Google Gson (2.11.0):** Lightweight JSON serialization and deserialization library.
- **JUnit 5 (Jupiter 5.11.0):** Modern test framework for deterministic, offline unit testing.

---

## Prerequisites & Environment Setup

Verify that Java (version 21 or higher) and Maven are installed and accessible in your system PATH:

### Windows PowerShell:
```powershell
java -version
mvn -version
```

### Linux / macOS:
```bash
java -version
mvn -version
```

If Maven or Java is not installed:
- Download Java: [Adoptium Eclipse Temurin](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- Download Maven: [Apache Maven](https://maven.apache.org/download.cgi)

---

## Project Architecture & Directory Structure

```
weather-app/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── example/
    │               └── weatherapp/
    │                   ├── Main.java                        # Application entry point & dependency composition
    │                   ├── ui/
    │                   │   └── ConsoleUI.java               # Console loops, user prompts, table formatting
    │                   ├── model/
    │                   │   ├── Location.java                # Immutable record for geographic coordinates
    │                   │   ├── CurrentWeather.java          # Immutable record for current weather metrics
    │                   │   ├── DailyForecast.java           # Immutable record for single-day forecast data
    │                   │   └── WeatherReport.java           # Composite record binding location & weather
    │                   ├── service/
    │                   │   ├── GeocodingService.java        # Coordinates lookup via Open-Meteo Geocoding
    │                   │   └── WeatherService.java          # Weather data retrieval & columnar array mapping
    │                   ├── util/
    │                   │   └── WeatherCodeMapper.java       # WMO weather code to description mapper
    │                   └── exception/
    │                       └── WeatherApiException.java     # Custom checked exception for network & API errors
    └── test/
        └── java/
            └── com/
                └── example/
                    └── weatherapp/
                        ├── util/
                        │   └── WeatherCodeMapperTest.java   # Parameterized tests for WMO weather codes
                        └── service/
                            └── WeatherParsingTest.java      # Offline tests for JSON parsing & model methods
```

---

## How the APIs Work

### 1. Geocoding API
When a user searches for `"Tokyo"`, `GeocodingService` sends a GET request:
```
https://geocoding-api.open-meteo.com/v1/search?name=Tokyo&count=5&language=en&format=json
```
- Parameters:
  - `name`: URL-encoded city name.
  - `count=5`: Returns up to 5 matching geographic coordinates.
  - `language=en`: English country and region names.

### 2. Forecast API
Once coordinates are selected (e.g. `latitude=35.6895`, `longitude=139.6917`), `WeatherService` fetches current conditions and the 5-day daily forecast in a single request:
```
https://api.open-meteo.com/v1/forecast?latitude=35.6895&longitude=139.6917&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m,wind_direction_10m&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset&temperature_unit=celsius&wind_speed_unit=kmh&precipitation_unit=mm&forecast_days=5&timezone=auto
```
- Open-Meteo returns daily forecasts in columnar arrays (e.g. `time: [...]`, `temperature_2m_max: [...]`).
- `WeatherService.convertDailyForecast()` iterates through matching indices and packages them into clean, self-contained `DailyForecast` records.

---

## Building and Running the Application

Navigate to the project root directory:
```powershell
cd C:\Users\Muham\.gemini\antigravity\scratch\weather-app
```

### 1. Compile the Project
```powershell
mvn clean compile
```

### 2. Launch the Application via Maven Exec Plugin
```powershell
mvn exec:java
```

*(On Linux / macOS: `mvn exec:java`)*

### 3. Alternative: Package and Run as a JAR
```powershell
mvn clean package
java -jar target/weather-app-1.0.0.jar
```

---

## Running Unit Tests

Execute the JUnit 5 test suite:
```powershell
mvn test
```

All unit tests are **100% offline and deterministic**; they do not call the live Internet, ensuring fast, reliable CI/CD and local development.

---

## Error Handling & Edge Cases

| Scenario | Handling Strategy |
| :--- | :--- |
| **Empty or Whitespace City Name** | ConsoleUI rejects blank strings and prompts again without querying the API. |
| **City Not Found** | Detects empty `results` array and informs user: *"City not found. Please check spelling..."*. |
| **Multiple Matching Cities** | Displays a numbered list with country and coordinates; prompts user to select 1..N. |
| **No Internet Connection** | Catches `ConnectException` / `UnknownHostException` and translates into friendly error message. |
| **API Server Timeout** | 10-second connect & request timeouts prevent the app from hanging. |
| **HTTP Error (e.g. 500)** | Throws `WeatherApiException` with the HTTP status code. |
| **Malformed JSON** | Catches `JsonParseException` without letting the raw stack trace pollute the UI. |
| **Thread Interruption** | Re-asserts `Thread.currentThread().interrupt()` before throwing `WeatherApiException`. |

---

## Troubleshooting Guide

1. **`java: release version 21 not supported`**
   - Ensure you are running JDK 21 or higher. Verify with `java -version` and `mvn -version`.

2. **Network Connection Refused or Timed Out**
   - Verify active internet connectivity. If you are behind an enterprise proxy, configure Maven or Java proxy settings:
     `-Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=8080`.

3. **Special characters or accents in city names (e.g., Zürich, São Paulo)**
   - All query parameters are safely encoded using UTF-8 `URLEncoder.encode()`. Ensure your console terminal supports UTF-8 display if accents appear garbled.

---

## Official API Documentation

- [Open-Meteo Geocoding API Documentation](https://open-meteo.com/en/docs/geocoding-api)
- [Open-Meteo Weather Forecast Documentation](https://open-meteo.com/en/docs)
