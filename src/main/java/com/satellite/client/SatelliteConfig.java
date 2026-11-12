
package com.satellite.client;

public class SatelliteConfig {

    public static final String GROUND_STATION_HOST = "localhost";

    public static final int TCP_PORT = 6000;

    public static final int UDP_PORT = 5001;

    public static final long TELEMETRY_INTERVAL = 1000;

    public static final long HEARTBEAT_INTERVAL = 2000;

    private final String satelliteId;

    public SatelliteConfig(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public String getSatelliteId() {
        return satelliteId;
    }
}
