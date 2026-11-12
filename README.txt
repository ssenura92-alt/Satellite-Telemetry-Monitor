# MEMBER 2 – SATELLITE CLIENT IMPLEMENTATION

## 1. Overview

This package contains the Java source files developed for Member 2 of the Satellite Ground Station and Telemetry Management System.

The Satellite Client simulates multiple independent satellites communicating with a central Ground Station using TCP and UDP protocols.

## 2. Included Source Files

The package contains the following five Java files:

1. SatelliteClient.java – Main satellite client application. Handles TCP connection, registration, telemetry transmission, heartbeat transmission and client lifecycle.

2. SatelliteConfig.java – Stores satellite configuration, including Ground Station address, network ports and transmission intervals.

3. TelemetryGenerator.java – Generates simulated satellite sensor readings.

4. UDPSender.java – Sends telemetry and heartbeat messages to the Ground Station using UDP.

5. CommandListener.java – Receives remote commands from the Ground Station and sends acknowledgement (ACK) messages.

## 3. Development Requirements

- Java JDK 21
- Maven
- Jackson Databind dependency

### Maven Dependency

Add the following dependency to the existing project's pom.xml if it is not already included:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.3</version>
</dependency>
```

## 4. Network Configuration

| Configuration | Value |
|---|---|
| Ground Station Host | localhost |
| TCP Port | 6000 |
| UDP Port | 5001 |
| Telemetry Interval | 1000 ms |
| Heartbeat Interval | 2000 ms |

Note: The TCP port is configured as 6000 because port 5000 was occupied in the development environment.

## 5. Running the Satellite Clients

Copy the five Java files into the following package in the existing Maven project:

`src/main/java/com/satellite/client/`

Run SatelliteClient.java as a Java Application.

Use the following program arguments to start individual satellite simulations:

- SAT-01
- SAT-02
- SAT-03

Each satellite should run as a separate Java application process.

The Ground Station server must be running before starting the satellite clients.

## 6. Implemented Features

- TCP connection to the Ground Station.
- Satellite registration using JSON messages.
- Simulated telemetry generation.
- UDP telemetry transmission.
- Periodic UDP heartbeat transmission.
- Remote command reception.
- Command acknowledgement (ACK) responses.
- PAUSE command to stop telemetry transmission.
- RESUME command to restart telemetry transmission.
- Independent simulation of multiple satellites.

## 7. Supported Remote Commands

| Command | Function |
|---|---|
| STATUS | Displays a status request message. |
| RESTART | Displays a restart request message. |
| PAUSE | Pauses telemetry transmission. |
| RESUME | Resumes telemetry transmission. |

Note: STATUS and RESTART currently simulate the requested actions through console messages. Actual restart functionality and detailed status reporting have not yet been implemented.

## 8. Testing Performed

The following tests were performed during development:

- Successful TCP connection and satellite registration.
- Simultaneous registration of SAT-01, SAT-02 and SAT-03.
- Telemetry generation and UDP transmission.
- UDP telemetry and heartbeat reception.
- Remote command delivery.
- Successful ACK responses.
- PAUSE functionality.
- RESUME functionality.
- Maven clean compile with BUILD SUCCESS.

## 9. Important Notes

- The five source files are intended to be integrated into the existing Satellite Telemetry Management Maven project.
- This ZIP contains Member 2's source files only.
- The Ground Station server and UDP receiver are separate project components.
- Ensure that the network configuration matches the Ground Station implementation before running the clients.

**Contribution: Member 2 – Satellite Client Implementation**