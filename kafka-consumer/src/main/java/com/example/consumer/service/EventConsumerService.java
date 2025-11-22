package com.example.consumer.service;

import com.example.consumer.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventConsumerService {

    // Thread-safe list to store all consumed events
    private final List<UserEvent> consumedEvents = new CopyOnWriteArrayList<>();

    // Map to store events by userId for quick lookup
    private final ConcurrentHashMap<String, List<UserEvent>> eventsByUser = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvent(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("========================================");
        log.info("Received event from partition: {} with offset: {}", partition, offset);
        log.info("Event Details:");
        log.info("  User ID: {}", event.getUserId());
        log.info("  Event Type: {}", event.getEventType());
        log.info("  Metadata: {}", event.getMetadata());
        log.info("  Timestamp: {}", event.getTimestamp());
        log.info("========================================");

        // Store event in memory
        storeEvent(event);

        // Process the event
        processEvent(event);
    }

    /**
     * Store event in memory for later retrieval
     */
    private void storeEvent(UserEvent event) {
        // Add to main list
        consumedEvents.add(event);

        // Add to user-specific map
        eventsByUser.computeIfAbsent(event.getUserId(), k -> new CopyOnWriteArrayList<>())
                .add(event);

        log.info("Event stored in memory. Total events: {}", consumedEvents.size());
    }

    /**
     * Process the event based on event type
     */
    private void processEvent(UserEvent event) {
        // Add your business logic here
        switch (event.getEventType()) {
            case CREATED:
                log.info("Processing user creation for: {}", event.getUserId());
                // Send welcome email
                // Create user profile
                // Update analytics
                break;
            case UPDATED:
                log.info("Processing user update for: {}", event.getUserId());
                // Update cache
                // Sync to other systems
                // Notify relevant services
                break;
            case DELETED:
                log.info("Processing user deletion for: {}", event.getUserId());
                // Remove personal data
                // Cancel subscriptions
                // Archive records
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    /**
     * Get all consumed events
     */
    public List<UserEvent> getAllConsumedEvents() {
        return new ArrayList<>(consumedEvents);
    }

    /**
     * Get total count of consumed events
     */
    public int getConsumedEventsCount() {
        return consumedEvents.size();
    }

    /**
     * Get events for a specific user
     */
    public List<UserEvent> getEventsByUserId(String userId) {
        return eventsByUser.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Get events by event type
     */
    public List<UserEvent> getEventsByType(String eventType) {
        try {
            UserEvent.EventType type = UserEvent.EventType.valueOf(eventType.toUpperCase());
            return consumedEvents.stream()
                    .filter(event -> event.getEventType() == type)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid event type: {}", eventType);
            return new ArrayList<>();
        }
    }

    /**
     * Get the latest consumed event
     */
    public UserEvent getLatestEvent() {
        if (consumedEvents.isEmpty()) {
            return null;
        }
        return consumedEvents.get(consumedEvents.size() - 1);
    }

    /**
     * Clear all consumed events from memory
     */
    public void clearConsumedEvents() {
        int count = consumedEvents.size();
        consumedEvents.clear();
        eventsByUser.clear();
        log.info("Cleared {} events from memory", count);
    }
}
