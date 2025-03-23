package com.kafka.listener.domain;

import lombok.Data;

@Data
public class RecordMsg {
    private String id;
    private String message;
    private long timestamp;
}
