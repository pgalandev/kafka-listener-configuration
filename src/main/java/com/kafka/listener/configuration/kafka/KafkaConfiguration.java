package com.kafka.listener.configuration.kafka;

import com.kafka.listener.domain.RecordMsg;
import com.kafka.listener.domain.Telemetry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServer;

    @Bean("kafka-single-listener")
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerMultiTypeContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(new JsonMessageConverter());
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean("kafka-multiple-listener")
    public ConcurrentKafkaListenerContainerFactory<String, Object> multiTypeKafkaListenerContainerFactory(
            RecordMessageConverter converter,
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordMessageConverter(converter);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    private Map<String, Object> kafkaCommonConfig() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");

        return props;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        ConsumerRecordRecoverer recover  = (record, exception) -> {
            log.error("Recovery for record: {}", record.value(), exception);
        };
        FixedBackOff backOff = new FixedBackOff(1000L, 3);
        return new DefaultErrorHandler(recover, backOff);
    }


    public ConsumerFactory<String, Object> multiTypeConsumerFactory() {
        HashMap<String, Object> props = new HashMap<>(kafkaCommonConfig() );
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean("multi-converter")
    public RecordMessageConverter multiTypeConverter() {
        StringJsonMessageConverter converter = new StringJsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("*");
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("record", RecordMsg.class);
        mappings.put("telemetry", Telemetry.class);
        typeMapper.setIdClassMapping(mappings);
        converter.setTypeMapper(typeMapper);

        return converter;
    }
}
