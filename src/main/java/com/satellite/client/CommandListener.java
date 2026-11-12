
package com.satellite.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CommandListener implements Runnable {

    private final Socket socket;
    private final String satelliteId;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final ObjectMapper objectMapper;
    private final SatelliteClient satelliteClient;

    public CommandListener(Socket socket, SatelliteClient satelliteClient)
            throws IOException {

        this.socket = socket;
        this.satelliteClient = satelliteClient;
        this.satelliteId = satelliteClient.getSatelliteId();

        this.writer = new PrintWriter(
                socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
        );

        this.reader = new BufferedReader(
                new java.io.InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            String message;

            while (!socket.isClosed()
                    && (message = reader.readLine()) != null) {

                JsonNode commandMessage = objectMapper.readTree(message);

                String messageType =
                        commandMessage.path("type").asText();

                if ("COMMAND".equals(messageType)) {

                    String targetSatellite =
                            commandMessage.path("satelliteId").asText();

                    if (!satelliteId.equals(targetSatellite)) {
                        continue;
                    }

                    String command =
                            commandMessage.path("command").asText();

                    System.out.println(
                            satelliteId + " received command: " + command
                    );

                    String status = processCommand(command);

                    sendAcknowledgement(status);
                }
            }

        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println(
                        satelliteId + " command listener error: "
                                + e.getMessage()
                );
            }
        }
    }

    private String processCommand(String command) {

        switch (command.toUpperCase()) {

            case "RESTART":
                System.out.println(
                        satelliteId + " restarting simulation."
                );
                return "SUCCESS";

            case "STATUS":
                System.out.println(
                        satelliteId + " status requested."
                );
                return "SUCCESS";

            case "PAUSE":
                satelliteClient.pauseTelemetry();
                System.out.println(
                        satelliteId + " telemetry paused."
                );
                return "SUCCESS";

            case "RESUME":
                satelliteClient.resumeTelemetry();
                System.out.println(
                        satelliteId + " telemetry resumed."
                );
                return "SUCCESS";

            default:
                System.out.println("Unknown command: " + command);
                return "FAILED";
        }
    }

    private void sendAcknowledgement(String status) {

        try {
            ObjectNode acknowledgement =
                    objectMapper.createObjectNode();

            acknowledgement.put("type", "ACK");
            acknowledgement.put("satelliteId", satelliteId);
            acknowledgement.put("status", status);

            writer.println(
                    objectMapper.writeValueAsString(acknowledgement)
            );

            if (writer.checkError()) {
                System.err.println(
                        "Failed to send ACK from " + satelliteId
                );
                return;
            }

            System.out.println(
                    satelliteId + " sent ACK: " + status
            );

        } catch (IOException e) {
            System.err.println(
                    "ACK error: " + e.getMessage()
            );
        }
    }
}
