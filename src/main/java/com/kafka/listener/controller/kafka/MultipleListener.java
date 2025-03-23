package com.kafka.listener.controller.kafka;

import com.kafka.listener.domain.RecordMsg;
import com.kafka.listener.domain.Telemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(
        topics = "multiple-topic",
        groupId = "multiple-group",
        containerFactory = "kafka-multiple-listener",
        autoStartup = "${kafka.listener.auto.startup:true}")
public class MultipleListener {

    @KafkaHandler
    public void listen(@Payload Telemetry telemetry) {
        log.info("Received telemetry: {}", telemetry);
    }

    @KafkaHandler
    public void lister(@Payload RecordMsg recordMsg) {
        log.info("Received record: {}", recordMsg);
    }

    @KafkaHandler(isDefault = true)
    public void listen(Object message) {
        log.info("Received default: {}", message);
    }
}
