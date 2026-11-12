# Network Protocol Specification
## Satellite Ground Station & Telemetry Management System

### 1. Overview

This document defines the communication protocol between the Ground Station and satellite clients.

Communication uses:
- TCP for satellite registration and remote commands.
- UDP for telemetry and heartbeat transmission.
- JSON for message formatting.

### 2. Network Configuration

| Setting | Value |
|---|---|
| TCP Port | 6000 |
| UDP Port | 5001 |
| Host (local testing) | localhost |
| Satellite IDs | SAT-01, SAT-02, SAT-03 |
| Telemetry interval | 1 second |
| Heartbeat interval | 2 seconds |
| Offline timeout | 5 seconds |
| Message format | JSON |

Note: The original proposal used TCP port 5000. Port 6000 is currently used because port 5000 is occupied on the Member 1 development computer. All members must use the same agreed TCP port during integration.

### 3. TCP Communication

TCP is used for satellite registration, remote commands and acknowledgements.

#### 3.1 Satellite Registration

When a satellite connects, it sends a registration message.

Example:

```json
{
  "type": "REGISTER",
  "satelliteId": "SAT-01"
}
```

The Ground Station validates the message and registers the satellite ID.

#### 3.2 Remote Commands

The Ground Station sends commands to a selected satellite.

Example:

```json
{
  "type": "COMMAND",
  "satelliteId": "SAT-01",
  "command": "RESTART"
}
```

#### 3.3 Command Acknowledgement

After processing a command, the satellite sends an acknowledgement.

Example:

```json
{
  "type": "ACK",
  "satelliteId": "SAT-01",
  "status": "SUCCESS"
}
```

Possible statuses:
- SUCCESS
- FAILED

The current implementation has successfully tested command delivery and ACK reception.

TCP messages use newline-delimited JSON.

### 4. UDP Communication

UDP is used for periodic telemetry and heartbeat transmission.

#### 4.1 Telemetry Message

Example:

```json
{
  "type": "TELEMETRY",
  "satelliteId": "SAT-01",
  "temperature": 35.5,
  "battery": 85,
  "altitude": 420,
  "velocity": 7.6,
  "signalStrength": 90
}
```

Telemetry fields:
- temperature: Simulated satellite temperature.
- battery: Battery percentage.
- altitude: Simulated altitude.
- velocity: Simulated velocity.
- signalStrength: Signal strength percentage.

Transmission interval: 1 second.

#### 4.2 Heartbeat Message

Proposed format:

```json
{
  "type": "HEARTBEAT",
  "satelliteId": "SAT-01"
}
```

Transmission interval: 2 seconds.

The Ground Station monitoring component should identify inactive satellites when no telemetry or heartbeat is received for 5 seconds.

### 5. Remote Command Types

Initial proposed commands:

| Command | Description |
|---|---|
| RESTART | Reset simulated satellite values |
| STATUS | Return current satellite status |
| PAUSE | Pause telemetry transmission |
| RESUME | Resume telemetry transmission |

Additional commands and their payloads can be agreed upon during implementation.

### 6. Shared Satellite State

Proposed shared satellite information structure:

| Field | Description |
|---|---|
| satelliteId | Unique satellite identifier |
| socket | Active TCP connection |
| connected | Connection status |
| latestTelemetry | Most recent telemetry values |
| lastHeartbeatTime | Time of last heartbeat |
| lastTelemetryTime | Time of last telemetry packet |

Member 1 manages satellite registration and TCP connections.

Member 3 should use the same shared satellite state to update telemetry and monitoring information rather than maintaining a conflicting duplicate satellite list.

The shared interface and thread-safety approach must be confirmed by Members 1 and 3 before integration.

### 7. Team Responsibilities

**Member 1 – Ground Station Server**
- TCP server and connection management.
- Satellite registration.
- Remote command delivery.
- ACK reception.

**Member 2 – Satellite Client**
- Satellite simulation.
- TCP registration and command listener.
- UDP telemetry transmission.
- Heartbeat transmission.
- Command acknowledgement.

**Member 3 – UDP Receiver and Monitoring**
- UDP telemetry reception.
- Telemetry validation and processing.
- Health monitoring and alerts.
- Offline detection.
- Telemetry and event logging.

### 8. Integration Notes

- All members use Java 21, Eclipse IDE and Maven.
- All messages use JSON.
- TCP messages are newline-delimited.
- All members must use the same communication ports and agreed message formats.
- Changes to this specification should be communicated to all team members.

**Document status:** Initial protocol proposal. Shared-state design, command formats and final network settings require team confirmation.