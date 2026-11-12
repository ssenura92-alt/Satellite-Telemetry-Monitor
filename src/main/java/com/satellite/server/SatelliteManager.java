
package com.satellite.server;

import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SatelliteManager {

    private final Map<String, Socket> connectedSatellites =
            new ConcurrentHashMap<>();

    public boolean registerSatellite(String satelliteId, Socket socket) {

        if (satelliteId == null || satelliteId.isBlank()
                || socket == null || socket.isClosed()) {
            return false;
        }

        return connectedSatellites.putIfAbsent(satelliteId, socket) == null;
    }

    public void removeSatellite(String satelliteId) {
        connectedSatellites.remove(satelliteId);
    }

    public Socket getSatellite(String satelliteId) {
        return connectedSatellites.get(satelliteId);
    }

    public Set<String> getConnectedSatelliteIds() {
        return Set.copyOf(connectedSatellites.keySet());
    }

    public int getConnectedSatelliteCount() {
        return connectedSatellites.size();
    }

    public boolean isConnected(String satelliteId) {
        Socket socket = connectedSatellites.get(satelliteId);
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
