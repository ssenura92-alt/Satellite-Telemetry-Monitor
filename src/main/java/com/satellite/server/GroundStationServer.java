
package com.satellite.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GroundStationServer {

    private static final int TCP_PORT = 6000;

    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private SatelliteManager satelliteManager;
    private CommandHandler commandHandler;

    public void startServer() {

        clientThreadPool = Executors.newCachedThreadPool();

        satelliteManager = new SatelliteManager();
        commandHandler = new CommandHandler(satelliteManager);
        Thread udpReceiverThread = new Thread(
                () -> UDPReceiver.main(new String[0])
        );

        udpReceiverThread.setName("UDP-Receiver");
        udpReceiverThread.setDaemon(true);
        udpReceiverThread.start();
        try {

            serverSocket = new ServerSocket(TCP_PORT);

            System.out.println("================================");
            System.out.println(" SATELLITE GROUND STATION");
            System.out.println("================================");
            System.out.println("Server started on port: " + TCP_PORT);
            System.out.println("Waiting for satellite connections...");

            startCommandConsole();

            while (!serverSocket.isClosed()) {

                Socket satelliteSocket = serverSocket.accept();

                System.out.println("New connection received from: "
                        + satelliteSocket.getInetAddress());

                clientThreadPool.execute(
                        new SatelliteHandler(
                                satelliteSocket,
                                satelliteManager
                        )
                );
            }

        } catch (IOException e) {

            if (serverSocket != null && !serverSocket.isClosed()) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }

        } finally {
            stopServer();
        }
    }

    public boolean sendCommandToSatellite(String satelliteId, String command) {

        return commandHandler.sendCommand(satelliteId, command);
    }

    public SatelliteManager getSatelliteManager() {

        return satelliteManager;
    }

    private void startCommandConsole() {

        Thread commandThread = new Thread(() -> {

            try (Scanner scanner = new Scanner(System.in)) {

                System.out.println("\nCommand Console Ready.");
                System.out.println("Format: SAT-01 RESTART");
                System.out.println("Type EXIT to stop the server.");

                while (!serverSocket.isClosed()) {

                    System.out.print("\nEnter command: ");

                    if (!scanner.hasNextLine()) {
                        break;
                    }

                    String input = scanner.nextLine().trim();

                    if (input.equalsIgnoreCase("EXIT")) {
                        stopServer();
                        break;
                    }

                    String[] parts = input.split("\\s+", 2);

                    if (parts.length != 2) {
                        System.out.println(
                                "Invalid format. Example: SAT-01 RESTART"
                        );
                        continue;
                    }

                    String satelliteId = parts[0];
                    String command = parts[1];

                    sendCommandToSatellite(satelliteId, command);
                }

            } catch (Exception e) {

                System.err.println(
                        "Command console error: " + e.getMessage()
                );
            }

        });

        commandThread.setDaemon(true);
        commandThread.start();
    }

    public void stopServer() {

        try {

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

        } catch (IOException e) {

            System.err.println(
                    "Error closing server: " + e.getMessage()
            );
        }

        if (clientThreadPool != null) {
            clientThreadPool.shutdown();
        }

        System.out.println("Ground Station Server stopped.");
    }

    public static void main(String[] args) {

        GroundStationServer server = new GroundStationServer();
        server.startServer();
    }
}
