
package com.satellite.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SatelliteHandler implements Runnable {

    private final Socket satelliteSocket;
    private final SatelliteManager satelliteManager;
    private final ObjectMapper objectMapper;

    private String satelliteId;

    public SatelliteHandler(Socket satelliteSocket,
                            SatelliteManager satelliteManager) {

        this.satelliteSocket = satelliteSocket;
        this.satelliteManager = satelliteManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {

        boolean registered = false;

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    satelliteSocket.getInputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {

            String registrationMessage = reader.readLine();

            if (registrationMessage == null) {
                return;
            }

            JsonNode registration =
                    objectMapper.readTree(registrationMessage);

            String messageType = registration.path("type").asText();

            satelliteId = registration.path("satelliteId").asText();

            if (!"REGISTER".equals(messageType) || satelliteId.isBlank()) {

                System.out.println("Invalid satellite registration.");
                return;
            }

            if (!satelliteManager.registerSatellite(
                    satelliteId, satelliteSocket)) {

                System.out.println(
                        "Registration rejected: " + satelliteId
                );
                return;
            }

            registered = true;

            System.out.println("Satellite registered: " + satelliteId);

            String message;

            while ((message = reader.readLine()) != null) {

                JsonNode receivedMessage =
                        objectMapper.readTree(message);

                String receivedType =
                        receivedMessage.path("type").asText();

                if ("ACK".equals(receivedType)) {

                    String acknowledgedSatellite =
                            receivedMessage.path("satelliteId").asText();

                    String status =
                            receivedMessage.path("status").asText();

                    if (!satelliteId.equals(acknowledgedSatellite)) {

                        System.out.println(
                                "Invalid ACK: Satellite ID mismatch."
                        );

                    } else if ("SUCCESS".equalsIgnoreCase(status)) {

                        System.out.println(
                                "Command acknowledged by " + satelliteId
                        );

                        System.out.println(
                                "Command execution status: SUCCESS"
                        );

                    } else {

                        System.out.println(
                                "Command failed on " + satelliteId
                        );

                        System.out.println(
                                "Command execution status: " + status
                        );
                    }

                } else {

                    System.out.println(
                            satelliteId + " sent: " + message
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Satellite connection error: " + e.getMessage()
            );

        } finally {

            if (registered) {

                satelliteManager.removeSatellite(satelliteId);

                System.out.println(
                        "Satellite disconnected: " + satelliteId
                );
            }

            try {

                satelliteSocket.close();

            } catch (IOException e) {

                System.out.println(
                        "Error closing satellite connection."
                );
            }
        }
    }
}
