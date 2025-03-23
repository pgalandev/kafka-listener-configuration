package com.kafka.listener.controller.kafka;

import com.kafka.listener.domain.Telemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SingleListener {

    @KafkaListener(
            topics = "single-topic",
            groupId = "single-group",
            containerFactory = "kafka-single-listener",
            autoStartup = "${kafka.listener.auto.startup:true}")
    public void listen(@Payload Telemetry telemetry) {
        log.info("Received telemetry: {}", telemetry);
    }
}
