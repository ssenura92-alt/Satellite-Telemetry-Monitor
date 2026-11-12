
package com.satellite.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TelemetryProcessor {

    public static void process(String message) {

        try {
            JsonObject data = JsonParser
                    .parseString(message)
                    .getAsJsonObject();

            String type = getString(data, "type");
            String satelliteId = getString(data, "satelliteId");

            // Validate satellite ID format
            if (!satelliteId.matches("SAT-\\d{2}")) {
                System.out.println("Invalid satellite ID!");
                return;
            }

            // Handle HEARTBEAT
            if ("HEARTBEAT".equals(type)) {
                SatelliteActivityMonitor.heartbeatReceived(satelliteId);
                return;
            }

            // Handle TELEMETRY only
            if (!"TELEMETRY".equals(type)) {
                System.out.println("Unknown message type: " + type);
                return;
            }

            // Extract required numeric values
            double temperature = getNumber(data, "temperature");
            double battery = getNumber(data, "battery");
            double altitude = getNumber(data, "altitude");
            double velocity = getNumber(data, "velocity");
            double signal = getNumber(data, "signalStrength");

            // Validate sensor ranges
            if (battery < 0 || battery > 100
                    || signal < 0 || signal > 100
                    || altitude < 0
                    || velocity < 0) {

                System.out.println("Invalid telemetry values!");
                return;
            }

            // Display telemetry
            System.out.println("===== SATELLITE DATA =====");
            System.out.println("Satellite: " + satelliteId);
            System.out.println("Temperature: " + temperature + " C");
            System.out.println("Battery: " + battery + "%");
            System.out.println("Altitude: " + altitude + " km");
            System.out.println("Velocity: " + velocity + " km/s");
            System.out.println("Signal: " + signal + "%");

            // Update satellite online status
            SatelliteActivityMonitor.telemetryReceived(satelliteId);

            // Check temperature and battery alerts
            AlertManager.checkAlerts(
                    satelliteId,
                    temperature,
                    battery
            );

            // Save telemetry to CSV
            TelemetryLogger.logTelemetry(
                    satelliteId,
                    temperature,
                    battery,
                    altitude,
                    velocity,
                    signal
            );

        } catch (RuntimeException e) {
            System.out.println(
                    "Invalid telemetry message: "
                    + e.getMessage()
            );
        }
    }

    // Read a required string field
    private static String getString(
            JsonObject data,
            String field) {

        JsonElement element = data.get(field);

        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isString()) {

            throw new IllegalArgumentException(
                    "Missing or invalid field: " + field
            );
        }

        return element.getAsString().trim();
    }

    // Read a required numeric field
    private static double getNumber(
            JsonObject data,
            String field) {

        JsonElement element = data.get(field);

        if (element == null
                || element.isJsonNull()
                || !element.isJsonPrimitive()
                || !element.getAsJsonPrimitive().isNumber()) {

            throw new IllegalArgumentException(
                    "Missing or invalid number: " + field
            );
        }

        double value = element.getAsDouble();

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "Invalid numeric value: " + field
            );
        }

        return value;
    }
}
