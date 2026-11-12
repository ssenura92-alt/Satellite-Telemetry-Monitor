package com.satellite.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class UDPReceiver {

    public static void main(String[] args) {

        int port = 5001;

        try (DatagramSocket socket = new DatagramSocket(port)) {

            System.out.println("Ground Station UDP Receiver Started!");
            System.out.println("Listening on port: " + port);
            SatelliteActivityMonitor.start();

            while (true) {

                byte[] buffer = new byte[2048];

                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length
                );

                socket.receive(packet);

                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("------------------------");
                System.out.println("Packet received from: "
                        + packet.getAddress().getHostAddress());

                TelemetryProcessor.process(message);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
