package com.satellite.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TelemetryProcessor {

    public static void process(String message) {

        try {
            // Convert received JSON text into an object
            JsonObject data = JsonParser
                    .parseString(message)
                    .getAsJsonObject();

            // Check message type

String type = data.get("type").getAsString();
String satelliteId = data.get("satelliteId").getAsString();

// Validate satellite ID
if (satelliteId.isBlank()) {
    System.out.println("Invalid satellite ID!");
    return;
}

// Handle heartbeat messages
if ("HEARTBEAT".equals(type)) {

    SatelliteManager.heartbeatReceived(satelliteId);

    return;
}

// Handle telemetry messages
if (!"TELEMETRY".equals(type)) {

    System.out.println("Unknown message type: " + type);

    return;
}

            double temperature =
                    data.get("temperature").getAsDouble();

            double battery =
                    data.get("battery").getAsDouble();

            double altitude =
                    data.get("altitude").getAsDouble();

            double velocity =
                    data.get("velocity").getAsDouble();

            double signal =
                    data.get("signalStrength").getAsDouble();

            // Validate values
            if (satelliteId.isBlank()
                    || !Double.isFinite(temperature)
                    || !Double.isFinite(battery)
                    || !Double.isFinite(altitude)
                    || !Double.isFinite(velocity)
                    || !Double.isFinite(signal)
                    || battery < 0 || battery > 100
                    || signal < 0 || signal > 100
                    || altitude < 0 || velocity < 0) {

                System.out.println("Invalid telemetry values!");
                return;
            }

            // Display received telemetry
            System.out.println("===== SATELLITE DATA =====");
            System.out.println("Satellite: " + satelliteId);
            System.out.println("Temperature: " + temperature + " C");
            System.out.println("Battery: " + battery + "%");
            System.out.println("Altitude: " + altitude + " km");
            System.out.println("Velocity: " + velocity + " km/s");
            System.out.println("Signal: " + signal + "%");
            SatelliteManager.telemetryReceived(satelliteId);
            AlertManager.checkAlerts(satelliteId, temperature, battery);

         // Save valid telemetry to CSV
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
                    "Invalid telemetry message: " + e.getMessage()
            );
        }
    }
}
