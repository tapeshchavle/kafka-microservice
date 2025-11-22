package com.example.producer.service;

import com.example.producer.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EventProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topicName;

    public EventProducerService(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(UserEvent event) {
        log.info("Publishing event: {}", event);
        
        CompletableFuture<SendResult<String, UserEvent>> future = 
            kafkaTemplate.send(topicName, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published successfully: {} with offset: {}", 
                    event, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event: {}", event, ex);
            }
        });
    }
}
