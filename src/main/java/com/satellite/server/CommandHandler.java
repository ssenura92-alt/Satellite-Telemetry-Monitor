
package com.satellite.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CommandHandler {

    private final SatelliteManager satelliteManager;
    private final ObjectMapper objectMapper;

    public CommandHandler(SatelliteManager satelliteManager) {
        this.satelliteManager = satelliteManager;
        this.objectMapper = new ObjectMapper();
    }

    public boolean sendCommand(String satelliteId, String command) {

        Socket satelliteSocket =
                satelliteManager.getSatellite(satelliteId);

        if (satelliteSocket == null || satelliteSocket.isClosed()) {
            System.out.println("Satellite is not connected: " + satelliteId);
            return false;
        }

        try {
            ObjectNode commandMessage = objectMapper.createObjectNode();
            commandMessage.put("type", "COMMAND");
            commandMessage.put("satelliteId", satelliteId);
            commandMessage.put("command", command);

            PrintWriter writer = new PrintWriter(
                    satelliteSocket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
            );

            writer.println(objectMapper.writeValueAsString(commandMessage));

            System.out.println("Command sent to " + satelliteId
                    + ": " + command);

            return !writer.checkError();

        } catch (IOException e) {
            System.err.println("Command delivery failed: " + e.getMessage());
            return false;
        }
    }
}
