package com.example.producer.controller;

import com.example.producer.model.UserEvent;
import com.example.producer.service.EventProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    private final EventProducerService producerService;

    public EventController(EventProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(@RequestBody UserEvent event) {
        log.info("Received request to publish event: {}", event);
        
        // Set timestamp if not provided
        if (event.getTimestamp() == null) {
            event.setTimestamp(java.time.LocalDateTime.now());
        }
        
        producerService.publishEvent(event);
        
        return ResponseEntity.ok("Event published successfully: " + event.getUserId());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Producer service is running");
    }
}
