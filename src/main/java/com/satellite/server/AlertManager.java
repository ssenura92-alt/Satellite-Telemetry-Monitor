
package com.satellite.server;

public class AlertManager {

    public static void checkAlerts(
            String satelliteId,
            double temperature,
            double battery) {

        boolean hasAlert = false;

        System.out.println("===== HEALTH MONITORING =====");

        // Check for high temperature
        if (temperature > 70) {

            System.out.println(
                "[WARNING] " + satelliteId
                + " - High Temperature: "
                + temperature + " C"
            );

            // Save high temperature alert to events.csv
            TelemetryLogger.logEvent(
                satelliteId,
                "HIGH_TEMPERATURE"
            );

            hasAlert = true;
        }

        // Check for low battery
        if (battery < 20) {

            System.out.println(
                "[WARNING] " + satelliteId
                + " - Low Battery: "
                + battery + "%"
            );

            // Save low battery alert to events.csv
            TelemetryLogger.logEvent(
                satelliteId,
                "LOW_BATTERY"
            );

            hasAlert = true;
        }

        // If no alerts, satellite is healthy
        if (!hasAlert) {

            System.out.println(
                "[HEALTHY] " + satelliteId
                + " - All systems normal"
            );
        }
    }
}
