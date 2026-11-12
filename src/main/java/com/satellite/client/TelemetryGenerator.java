
package com.satellite.client;

import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TelemetryGenerator {

    private final String satelliteId;
    private final ObjectMapper objectMapper;

    public TelemetryGenerator(String satelliteId) {
        this.satelliteId = satelliteId;
        this.objectMapper = new ObjectMapper();
    }

    public ObjectNode generateTelemetry() {

        ObjectNode telemetry = objectMapper.createObjectNode();

        telemetry.put("type", "TELEMETRY");
        telemetry.put("satelliteId", satelliteId);
        telemetry.put("temperature", randomDouble(20.0, 80.0));
        telemetry.put("battery", randomDouble(10.0, 100.0));
        telemetry.put("altitude", randomDouble(350.0, 450.0));
        telemetry.put("velocity", randomDouble(5.0, 8.0));
        telemetry.put("signalStrength", randomDouble(50.0, 100.0));

        return telemetry;
    }

    private double randomDouble(double min, double max) {

        double value = ThreadLocalRandom.current()
                .nextDouble(min, max);

        return Math.round(value * 10.0) / 10.0;
    }
}
