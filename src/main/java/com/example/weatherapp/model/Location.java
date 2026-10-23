package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.Objects;

/**
 * Represents a geographical location returned by the Open-Meteo Geocoding API.
 * 
 * In modern Java (Java 16+), a {@code record} is an immutable data carrier.
 * The compiler automatically generates:
 * - Private final fields for all components
 * - A canonical constructor
 * - Accessor methods (e.g., {@code name()}, {@code latitude()})
 * - {@code equals()}, {@code hashCode()}, and {@code toString()} implementations
 */
public record Location(
        @SerializedName("id") long id,
        @SerializedName("name") String name,
        @SerializedName("latitude") double latitude,
        @SerializedName("longitude") double longitude,
        @SerializedName("country") String country,
        @SerializedName("admin1") String admin1,
        @SerializedName("timezone") String timezone
) {
    /**
     * Compact constructor to normalize data and ensure safe defaults.
     */
    public Location {
        name = (name != null) ? name.trim() : "Unknown";
        country = (country != null) ? country.trim() : "";
        admin1 = (admin1 != null) ? admin1.trim() : "";
        timezone = (timezone != null && !timezone.isBlank()) ? timezone.trim() : "UTC";
    }

    /**
     * Returns a human-friendly display label combining city, state/region, and country.
     * Example: "Karachi, Sindh, Pakistan" or "London, United Kingdom".
     *
     * @return Formatted location name for console output.
     */
    public String displayName() {
        StringBuilder sb = new StringBuilder(name);
        if (admin1 != null && !admin1.isBlank() && !admin1.equalsIgnoreCase(name)) {
            sb.append(", ").append(admin1);
        }
        if (country != null && !country.isBlank()) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }
}
