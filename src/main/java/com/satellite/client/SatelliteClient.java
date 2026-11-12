
package com.satellite.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SatelliteClient {

    private final SatelliteConfig config;
    private final TelemetryGenerator telemetryGenerator;

    private Socket socket;
    private UDPSender udpSender;
    private CommandListener commandListener;
    private ScheduledExecutorService scheduler;

    private volatile boolean running;
    private volatile boolean telemetryPaused;

    public SatelliteClient(String satelliteId) {
        this.config = new SatelliteConfig(satelliteId);
        this.telemetryGenerator = new TelemetryGenerator(satelliteId);
    }

    public void start() {
        try {
            socket = new Socket(
                    SatelliteConfig.GROUND_STATION_HOST,
                    SatelliteConfig.TCP_PORT
            );

            System.out.println(
                    config.getSatelliteId()
                    + " connected to Ground Station."
            );

            registerSatellite();

            udpSender = new UDPSender(config);

            scheduler = Executors.newScheduledThreadPool(2);

            commandListener =
                    new CommandListener(socket, this);

            running = true;

            startCommandListener();
            startTelemetryTransmission();
            startHeartbeatTransmission();

            System.out.println(
                    config.getSatelliteId() + " simulation started."
            );

        } catch (IOException e) {
            System.err.println(
                    "Satellite connection error: " + e.getMessage()
            );
            stop();
        }
    }

    private void registerSatellite() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        ObjectNode registration = objectMapper.createObjectNode();
        registration.put("type", "REGISTER");
        registration.put("satelliteId", config.getSatelliteId());

        PrintWriter writer = new PrintWriter(
                socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
        );

        writer.println(objectMapper.writeValueAsString(registration));

        if (writer.checkError()) {
            throw new IOException("Satellite registration failed.");
        }

        System.out.println(
                config.getSatelliteId() + " registration message sent."
        );
    }

    private void startCommandListener() {
        Thread commandThread = new Thread(commandListener);

        commandThread.setName(
                config.getSatelliteId() + "-CommandListener"
        );

        commandThread.start();
    }

    private void startTelemetryTransmission() {
        scheduler.scheduleAtFixedRate(() -> {

            if (!running || telemetryPaused) {
                return;
            }

            try {
                ObjectNode telemetry =
                        telemetryGenerator.generateTelemetry();

                udpSender.sendTelemetry(telemetry);

                System.out.println(
                        config.getSatelliteId()
                        + " telemetry sent: " + telemetry
                );

            } catch (IOException e) {
                System.err.println(
                        "Telemetry error: " + e.getMessage()
                );
            }

        }, 0, SatelliteConfig.TELEMETRY_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private void startHeartbeatTransmission() {
        scheduler.scheduleAtFixedRate(() -> {

            if (!running) {
                return;
            }

            try {
                udpSender.sendHeartbeat(config.getSatelliteId());

                System.out.println(
                        config.getSatelliteId()
                        + " heartbeat sent."
                );

            } catch (IOException e) {
                System.err.println(
                        "Heartbeat error: " + e.getMessage()
                );
            }

        }, 0, SatelliteConfig.HEARTBEAT_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    public String getSatelliteId() {
        return config.getSatelliteId();
    }

    public void pauseTelemetry() {
        telemetryPaused = true;
    }

    public void resumeTelemetry() {
        telemetryPaused = false;
    }

    public void stop() {
        running = false;

        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        if (udpSender != null) {
            udpSender.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Error closing satellite connection: "
                    + e.getMessage()
            );
        }

        System.out.println(
                config.getSatelliteId() + " simulation stopped."
        );
    }

    public static void main(String[] args) {
        String satelliteId = args.length > 0
                ? args[0]
                : "SAT-01";

        SatelliteClient satellite =
                new SatelliteClient(satelliteId);

        Runtime.getRuntime().addShutdownHook(
                new Thread(satellite::stop)
        );

        satellite.start();
    }
}
