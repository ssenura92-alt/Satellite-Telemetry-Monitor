
package com.satellite.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UDPSender {

    private final DatagramSocket socket;
    private final InetAddress groundStationAddress;
    private final int udpPort;
    private final ObjectMapper objectMapper;

    public UDPSender(SatelliteConfig config) throws IOException {

        this.socket = new DatagramSocket();

        this.groundStationAddress = InetAddress.getByName(
                SatelliteConfig.GROUND_STATION_HOST
        );

        this.udpPort = SatelliteConfig.UDP_PORT;
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage(ObjectNode message) throws IOException {

        byte[] data = objectMapper.writeValueAsBytes(message);

        DatagramPacket packet = new DatagramPacket(
                data,
                data.length,
                groundStationAddress,
                udpPort
        );

        socket.send(packet);
    }

    public void sendTelemetry(ObjectNode telemetry) throws IOException {

        sendMessage(telemetry);
    }

    public void sendHeartbeat(String satelliteId) throws IOException {

        ObjectNode heartbeat = objectMapper.createObjectNode();

        heartbeat.put("type", "HEARTBEAT");
        heartbeat.put("satelliteId", satelliteId);

        sendMessage(heartbeat);
    }

    public void close() {

        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
