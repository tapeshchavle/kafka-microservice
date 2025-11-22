package com.example.consumer.controller;

import com.example.consumer.model.UserEvent;
import com.example.consumer.service.EventConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    private final EventConsumerService consumerService;

    public EventController(EventConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    /**
     * Get all consumed events
     */
    @GetMapping("/consumed")
    public ResponseEntity<List<UserEvent>> getAllConsumedEvents() {
        log.info("Fetching all consumed events");
        List<UserEvent> events = consumerService.getAllConsumedEvents();
        return ResponseEntity.ok(events);
    }

    /**
     * Get consumed events count
     */
    @GetMapping("/consumed/count")
    public ResponseEntity<Map<String, Object>> getConsumedEventsCount() {
        log.info("Fetching consumed events count");
        int count = consumerService.getConsumedEventsCount();
        return ResponseEntity.ok(Map.of(
                "totalEvents", count,
                "message", "Total events consumed since service started"));
    }

    /**
     * Get consumed events by user ID
     */
    @GetMapping("/consumed/user/{userId}")
    public ResponseEntity<List<UserEvent>> getEventsByUserId(@PathVariable String userId) {
        log.info("Fetching events for user: {}", userId);
        List<UserEvent> events = consumerService.getEventsByUserId(userId);
        return ResponseEntity.ok(events);
    }

    /**
     * Get consumed events by event type
     */
    @GetMapping("/consumed/type/{eventType}")
    public ResponseEntity<List<UserEvent>> getEventsByType(@PathVariable String eventType) {
        log.info("Fetching events of type: {}", eventType);
        List<UserEvent> events = consumerService.getEventsByType(eventType);
        return ResponseEntity.ok(events);
    }

    /**
     * Clear all consumed events from memory
     */
    @DeleteMapping("/consumed")
    public ResponseEntity<Map<String, String>> clearConsumedEvents() {
        log.info("Clearing all consumed events");
        consumerService.clearConsumedEvents();
        return ResponseEntity.ok(Map.of(
                "message", "All consumed events cleared successfully"));
    }

    /**
     * Get the latest consumed event
     */
    @GetMapping("/consumed/latest")
    public ResponseEntity<UserEvent> getLatestEvent() {
        log.info("Fetching latest consumed event");
        UserEvent event = consumerService.getLatestEvent();
        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "kafka-consumer",
                "eventsConsumed", consumerService.getConsumedEventsCount()));
    }
}
