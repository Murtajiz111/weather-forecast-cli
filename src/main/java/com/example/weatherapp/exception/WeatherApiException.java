package com.example.weatherapp.exception;

/**
 * Custom checked exception representing failures during weather or geocoding API operations.
 *
 * Examples include:
 * - Network timeouts or unreachable servers (DNS/connectivity errors)
 * - HTTP status errors (e.g. 400 Bad Request, 500 Server Error)
 * - JSON parsing errors or unexpected response payloads
 * - Interrupted threads during HTTP operations
 */
public class WeatherApiException extends Exception {

    private final int statusCode;

    /**
     * Constructs an exception with a user-friendly message.
     *
     * @param message Explanatory message.
     */
    public WeatherApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    /**
     * Constructs an exception with an explanatory message and root cause.
     *
     * @param message Explanatory message.
     * @param cause   The underlying exception (e.g., IOException, InterruptedException).
     */
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    /**
     * Constructs an exception with an HTTP status code, message, and cause.
     *
     * @param message    Explanatory message.
     * @param statusCode HTTP response status code (e.g., 500, 404).
     * @param cause      The underlying exception.
     */
    public WeatherApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
