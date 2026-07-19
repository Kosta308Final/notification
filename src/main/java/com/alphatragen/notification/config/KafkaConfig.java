package com.alphatragen.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${app.kafka.retry-backoff-ms:1000}")
    private long retryBackoffMs;

    @Bean
    public DefaultErrorHandler errorHandler() {
        log.info("Configuring Kafka DefaultErrorHandler with retryBackoffMs: {}", retryBackoffMs);
        // 2 retries (total 3 attempts)
        FixedBackOff backOff = new FixedBackOff(retryBackoffMs, 2);
        
        // Use constructor to pass the recoverer
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((record, exception) -> {
            log.error("kafka_processing_failed_final topic={} partition={} offset={} retryCount={} errorType={}",
                    record.topic(), record.partition(), record.offset(), 2, exception.getClass().getSimpleName(), exception);
        }, backOff);

        return errorHandler;
    }
}
