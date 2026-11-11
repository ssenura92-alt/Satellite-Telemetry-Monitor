
package com.satellite.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SatelliteManager {

    // Satellite offline timeout = 5 seconds
    private static final long TIMEOUT = 5000;

    // Store satellite's last activity and online status
    private static class SatelliteState {

        long lastSeen;
        boolean online;

        SatelliteState(long lastSeen) {
            this.lastSeen = lastSeen;
            this.online = true;
        }
    }

    // Store multiple satellites
    private static final ConcurrentHashMap<String, SatelliteState>
            satellites = new ConcurrentHashMap<>();

    // Background scheduler for offline detection
    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private static boolean started = false;

    // Start checking satellite status every second
    public static synchronized void start() {

        if (started) {
            return;
        }

        started = true;

        scheduler.scheduleAtFixedRate(
                SatelliteManager::checkOffline,
                1,
                1,
                TimeUnit.SECONDS
        );

        System.out.println(
                "Satellite Offline Monitoring Started!"
        );
    }

    // Called whenever valid telemetry arrives
    public static void telemetryReceived(String satelliteId) {

        updateSatelliteActivity(satelliteId);
    }

    // Called whenever a heartbeat arrives
    public static void heartbeatReceived(String satelliteId) {

        updateSatelliteActivity(satelliteId);

        System.out.println(
                "[HEARTBEAT] " + satelliteId
                + " - Received"
        );
    }

    // Update satellite's last-seen time
    private static void updateSatelliteActivity(
            String satelliteId) {

        long now = System.currentTimeMillis();

        satellites.compute(satelliteId, (id, state) -> {

            // First time satellite is detected
            if (state == null) {

                System.out.println(
                        "[ONLINE] " + id
                        + " - Satellite detected"
                );

                return new SatelliteState(now);
            }

            // Satellite reconnects after being offline
            if (!state.online) {

                System.out.println(
                        "[ONLINE] " + id
                        + " - Connection restored"
                );

                TelemetryLogger.logEvent(
                        id,
                        "ONLINE_AGAIN"
                );
            }

            state.lastSeen = now;
            state.online = true;

            return state;
        });
    }

    // Check for satellites with no recent activity
    private static void checkOffline() {

        long now = System.currentTimeMillis();

        satellites.forEach((id, value) -> {

            satellites.computeIfPresent(id, (key, state) -> {

                if (state.online
                        && now - state.lastSeen >= TIMEOUT) {

                    state.online = false;

                    System.out.println(
                            "[OFFLINE] " + id
                            + " - No telemetry or heartbeat "
                            + "for 5 seconds"
                    );

                    TelemetryLogger.logEvent(
                            id,
                            "SATELLITE_OFFLINE"
                    );
                }

                return state;
            });
        });
    }
}

