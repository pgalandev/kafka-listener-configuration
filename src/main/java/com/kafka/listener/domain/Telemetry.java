package com.kafka.listener.domain;

import lombok.Data;

@Data
public class Telemetry {
    private String id;
    private String type;
    private Double value;
    private Long timestamp;
}
