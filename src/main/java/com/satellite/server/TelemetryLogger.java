
package com.satellite.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public class TelemetryLogger {

    // Save satellite readings
    public static synchronized void logTelemetry(
            String satelliteId,
            double temperature,
            double battery,
            double altitude,
            double velocity,
            double signal) {

        String header =
                "timestamp,satelliteId,temperature,battery,"
                + "altitude,velocity,signalStrength";

        String row = Instant.now() + ","
                + satelliteId + ","
                + temperature + ","
                + battery + ","
                + altitude + ","
                + velocity + ","
                + signal;

        saveToFile("telemetry.csv", header, row);
    }

    // Save important system events
    public static synchronized void logEvent(
            String satelliteId,
            String event) {

        String header = "timestamp,satelliteId,event";

        String safeEvent = event.replace(",", ";")
                                .replace("\n", " ");

        String row = Instant.now() + ","
                + satelliteId + ","
                + safeEvent;

        saveToFile("events.csv", header, row);
    }

    // Common file-writing method
    private static void saveToFile(
            String fileName,
            String header,
            String row) {

        try {
            Path directory = Path.of("logs");
            Files.createDirectories(directory);

            Path file = directory.resolve(fileName);

            boolean isNewFile =
                    Files.notExists(file) ||
                    Files.size(file) == 0;

            String content = "";

            if (isNewFile) {
                content += header + System.lineSeparator();
            }

            content += row + System.lineSeparator();

            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.println("Saved to: " + file);

        } catch (IOException e) {
            System.err.println(
                    "Logging error: " + e.getMessage()
            );
        }
    }
}
