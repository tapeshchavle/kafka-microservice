package com.example.producer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    
    private String userId;
    private EventType eventType;
    private String metadata;
    private LocalDateTime timestamp;

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    public UserEvent(String userId, EventType eventType, String metadata) {
        this.userId = userId;
        this.eventType = eventType;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }
}
