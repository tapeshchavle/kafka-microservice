# Kafka Event-Driven Architecture - Complete Guide

## System Overview

This document provides a comprehensive understanding of how the Producer and Consumer microservices work together using Apache Kafka.

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Complete System                              │
│                                                                      │
│  ┌─────────────┐         ┌──────────────┐         ┌──────────────┐ │
│  │             │         │              │         │              │ │
│  │   Client    │────────▶│   Producer   │────────▶│    Kafka     │ │
│  │  (HTTP)     │         │   Service    │         │    Broker    │ │
│  │             │         │  Port: 8081  │         │  Port: 9092  │ │
│  └─────────────┘         └──────────────┘         └──────────────┘ │
│                                                           │          │
│                                                           │          │
│                                                           ▼          │
│                          ┌──────────────┐         ┌──────────────┐ │
│                          │              │         │              │ │
│                          │  Business    │◀────────│   Consumer   │ │
│                          │   Logic      │         │   Service    │ │
│                          │              │         │  Port: 8082  │ │
│                          └──────────────┘         └──────────────┘ │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

## Complete Message Flow (Step-by-Step)

### Step 1: Client Sends Request

```
┌──────────────────────────────────────────────────────┐
│  Client (curl, Postman, Web App, Mobile App)        │
└──────────────────────────────────────────────────────┘
                      │
                      │ HTTP POST
                      │ Content-Type: application/json
                      ▼
        POST http://localhost:8081/api/events/publish
        {
          "userId": "user123",
          "eventType": "CREATED",
          "metadata": "New user registered"
        }
```

### Step 2: Producer Receives Request

```
┌──────────────────────────────────────────────────────┐
│  Producer Service - EventController                  │
│  File: EventController.java                          │
└──────────────────────────────────────────────────────┘
                      │
                      │ 1. Deserialize JSON to UserEvent
                      │ 2. Add timestamp if missing
                      │ 3. Validate data
                      ▼
        UserEvent {
          userId: "user123",
          eventType: CREATED,
          metadata: "New user registered",
          timestamp: 2025-11-23T01:25:00
        }
```

### Step 3: Service Layer Processes

```
┌──────────────────────────────────────────────────────┐
│  Producer Service - EventProducerService             │
│  File: EventProducerService.java                     │
└──────────────────────────────────────────────────────┘
                      │
                      │ 1. Log event
                      │ 2. Call kafkaTemplate.send()
                      │ 3. Set callback for success/failure
                      ▼
        kafkaTemplate.send(
          topic: "user-events",
          key: "user123",
          value: UserEvent{...}
        )
```

### Step 4: Kafka Template Serializes

```
┌──────────────────────────────────────────────────────┐
│  KafkaTemplate + JsonSerializer                      │
└──────────────────────────────────────────────────────┘
                      │
                      │ 1. Serialize UserEvent to JSON
                      │ 2. Convert JSON to bytes
                      │ 3. Add headers (timestamp, etc.)
                      ▼
        Byte Array:
        [123, 34, 117, 115, 101, 114, 73, 100, ...]
        (JSON bytes)
```

### Step 5: Network Transmission

```
┌──────────────────────────────────────────────────────┐
│  Network Layer (TCP)                                 │
└──────────────────────────────────────────────────────┘
                      │
                      │ Send over network
                      │ localhost:9092
                      ▼
        TCP Packet → Kafka Broker
```

### Step 6: Kafka Stores Message

```
┌──────────────────────────────────────────────────────┐
│  Kafka Broker                                        │
│  Topic: user-events                                  │
│  Partition: 0                                        │
└──────────────────────────────────────────────────────┘
                      │
                      │ 1. Determine partition (based on key)
                      │ 2. Append to partition log
                      │ 3. Assign offset number
                      │ 4. Replicate (if configured)
                      │ 5. Send acknowledgment
                      ▼
        Stored at:
        Partition 0, Offset 42
        
        Partition Log:
        [..., offset 40, offset 41, offset 42 ← NEW]
```

### Step 7: Producer Receives Acknowledgment

```
┌──────────────────────────────────────────────────────┐
│  Producer Service - Callback                         │
└──────────────────────────────────────────────────────┘
                      │
                      │ Success callback invoked
                      ▼
        Log: "Event published successfully with offset: 42"
        
        HTTP Response to Client:
        200 OK
        "Event published successfully: user123"
```

### Step 8: Consumer Polls Kafka

```
┌──────────────────────────────────────────────────────┐
│  Consumer Service - Polling Loop                     │
│  Runs continuously in background                     │
└──────────────────────────────────────────────────────┘
                      │
                      │ Poll every 100ms
                      │ "Any new messages?"
                      ▼
        Request to Kafka:
        - Topic: user-events
        - Group: user-event-consumer-group
        - Last offset: 41
```

### Step 9: Kafka Sends Message to Consumer

```
┌──────────────────────────────────────────────────────┐
│  Kafka Broker Response                               │
└──────────────────────────────────────────────────────┘
                      │
                      │ "Yes, new message at offset 42"
                      ▼
        Message:
        - Partition: 0
        - Offset: 42
        - Key: "user123"
        - Value: [JSON bytes]
        - Timestamp: 2025-11-23T01:25:00
```

### Step 10: Consumer Deserializes

```
┌──────────────────────────────────────────────────────┐
│  JsonDeserializer                                    │
└──────────────────────────────────────────────────────┘
                      │
                      │ 1. Convert bytes to JSON string
                      │ 2. Parse JSON
                      │ 3. Create UserEvent object
                      ▼
        UserEvent {
          userId: "user123",
          eventType: CREATED,
          metadata: "New user registered",
          timestamp: 2025-11-23T01:25:00
        }
```

### Step 11: Listener Method Invoked

```
┌──────────────────────────────────────────────────────┐
│  Consumer Service - EventConsumerService             │
│  File: EventConsumerService.java                     │
│  Method: consumeEvent()                              │
└──────────────────────────────────────────────────────┘
                      │
                      │ @KafkaListener triggered
                      │ Method parameters populated:
                      │ - event: UserEvent object
                      │ - partition: 0
                      │ - offset: 42
                      ▼
        Log:
        ========================================
        Received event from partition: 0 with offset: 42
        Event Details:
          User ID: user123
          Event Type: CREATED
          Metadata: New user registered
          Timestamp: 2025-11-23T01:25:00
        ========================================
```

### Step 12: Business Logic Executes

```
┌──────────────────────────────────────────────────────┐
│  Consumer Service - processEvent()                   │
└──────────────────────────────────────────────────────┘
                      │
                      │ Switch on eventType
                      ▼
        case CREATED:
          - Send welcome email
          - Create user profile
          - Update analytics
          - Trigger onboarding
          
        Log: "Processing user creation for: user123"
```

### Step 13: Offset Committed

```
┌──────────────────────────────────────────────────────┐
│  Consumer - Offset Commit                            │
└──────────────────────────────────────────────────────┘
                      │
                      │ After successful processing
                      │ Commit offset to Kafka
                      ▼
        Kafka stores:
        Consumer Group: user-event-consumer-group
        Topic: user-events
        Partition: 0
        Committed Offset: 42
        
        (If consumer crashes, it will resume from offset 43)
```

### Step 14: Ready for Next Message

```
┌──────────────────────────────────────────────────────┐
│  Consumer - Back to Polling                          │
└──────────────────────────────────────────────────────┘
                      │
                      │ Continue polling
                      │ Wait for offset 43
                      ▼
        Poll loop continues...
```

## Detailed Component Interaction

### Producer Components

```
┌────────────────────────────────────────────────────────────┐
│                    Producer Service                        │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  1. EventController                                  │ │
│  │     - @RestController                                │ │
│  │     - @PostMapping("/api/events/publish")            │ │
│  │     - Receives HTTP requests                         │ │
│  │     - Returns HTTP responses                         │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↓                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  2. EventProducerService                             │ │
│  │     - @Service                                       │ │
│  │     - publishEvent(UserEvent)                        │ │
│  │     - Business logic                                 │ │
│  │     - Logging                                        │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↓                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  3. KafkaTemplate                                    │ │
│  │     - send(topic, key, value)                        │ │
│  │     - Async operation                                │ │
│  │     - Returns CompletableFuture                      │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↓                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  4. KafkaProducerConfig                              │ │
│  │     - ProducerFactory bean                           │ │
│  │     - Serialization config                           │ │
│  │     - Reliability settings (acks, retries)           │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↓                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  5. JsonSerializer                                   │ │
│  │     - Converts UserEvent → JSON → bytes             │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

### Consumer Components

```
┌────────────────────────────────────────────────────────────┐
│                    Consumer Service                        │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  1. @KafkaListener                                   │ │
│  │     - Annotation on consumeEvent() method            │ │
│  │     - Specifies topic and group                      │ │
│  │     - Triggered automatically                        │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↓                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  2. EventConsumerService                             │ │
│  │     - @Service                                       │ │
│  │     - consumeEvent(UserEvent, partition, offset)     │ │
│  │     - processEvent(UserEvent)                        │ │
│  │     - Business logic                                 │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↑                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  3. KafkaListenerContainerFactory                    │ │
│  │     - Manages listener containers                    │ │
│  │     - Polling configuration                          │ │
│  │     - Concurrency settings                           │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↑                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  4. KafkaConsumerConfig                              │ │
│  │     - ConsumerFactory bean                           │ │
│  │     - Deserialization config                         │ │
│  │     - Group ID, offset reset                         │ │
│  └──────────────────────────────────────────────────────┘ │
│                          ↑                                 │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  5. JsonDeserializer                                 │ │
│  │     - Converts bytes → JSON → UserEvent             │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────────────────────────────────────────┘
```

## Kafka Internal Structure

### Topic Partitions

```
Topic: user-events
├── Partition 0
│   ├── Offset 0: {userId: "user1", eventType: "CREATED", ...}
│   ├── Offset 1: {userId: "user2", eventType: "UPDATED", ...}
│   ├── Offset 2: {userId: "user1", eventType: "UPDATED", ...}
│   └── Offset 3: {userId: "user3", eventType: "DELETED", ...}
│
├── Partition 1
│   ├── Offset 0: {userId: "user4", eventType: "CREATED", ...}
│   └── Offset 1: {userId: "user5", eventType: "UPDATED", ...}
│
└── Partition 2
    ├── Offset 0: {userId: "user6", eventType: "CREATED", ...}
    └── Offset 1: {userId: "user7", eventType: "DELETED", ...}
```

### Message Structure

```
┌─────────────────────────────────────────────────────┐
│                 Kafka Message                       │
├─────────────────────────────────────────────────────┤
│  Headers:                                           │
│    - timestamp: 2025-11-23T01:25:00                 │
│    - content-type: application/json                 │
│                                                     │
│  Key: "user123" (String)                            │
│    - Used for partitioning                          │
│    - Ensures ordering for same user                 │
│                                                     │
│  Value: (JSON bytes)                                │
│    {                                                │
│      "userId": "user123",                           │
│      "eventType": "CREATED",                        │
│      "metadata": "New user registered",             │
│      "timestamp": "2025-11-23T01:25:00"             │
│    }                                                │
│                                                     │
│  Metadata:                                          │
│    - partition: 0                                   │
│    - offset: 42                                     │
│    - topic: "user-events"                           │
└─────────────────────────────────────────────────────┘
```

## Real-World Example: User Registration Flow

### Timeline

```
T=0ms    Client submits registration form
         │
         ▼
T=10ms   Producer receives HTTP POST
         │
         ▼
T=15ms   Producer validates and enriches data
         │
         ▼
T=20ms   Producer sends to Kafka
         │
         ▼
T=25ms   Kafka stores message (offset 42)
         │
         ▼
T=30ms   Kafka acknowledges to producer
         │
         ▼
T=35ms   Producer returns 200 OK to client
         │
         ├─────────────────────────────────┐
         │                                 │
         ▼                                 ▼
T=100ms  Consumer polls Kafka         Client shows
         │                            "Registration
         ▼                             successful!"
T=105ms  Consumer receives message
         │
         ▼
T=110ms  Consumer processes:
         - Send welcome email
         - Create profile
         - Update analytics
         │
         ▼
T=500ms  Email sent
         Profile created
         Analytics updated
         │
         ▼
T=505ms  Consumer commits offset 42
         Ready for next message
```

### Data Flow

```
1. Registration Form
   ┌─────────────────────────┐
   │ Name: John Doe          │
   │ Email: john@example.com │
   │ Password: ********      │
   └─────────────────────────┘
              ↓
2. HTTP Request
   POST /api/events/publish
   {
     "userId": "john123",
     "eventType": "CREATED",
     "metadata": "Registered via web"
   }
              ↓
3. Kafka Message
   Topic: user-events
   Key: "john123"
   Value: {
     "userId": "john123",
     "eventType": "CREATED",
     "metadata": "Registered via web",
     "timestamp": "2025-11-23T01:25:00"
   }
              ↓
4. Consumer Processing
   - Send email to john@example.com
   - Create profile in database
   - Increment registration counter
   - Trigger onboarding workflow
              ↓
5. Side Effects
   ✓ Welcome email sent
   ✓ Profile created
   ✓ Analytics updated
   ✓ Onboarding started
```

## Performance Characteristics

### Latency

```
Producer API Response Time:
├── Best case: 5-10ms
├── Typical: 20-50ms
└── Worst case: 100-200ms (network issues)

Consumer Processing Time:
├── Message receipt: < 100ms (polling interval)
├── Deserialization: < 1ms
├── Business logic: Varies (10ms - 1s+)
└── Offset commit: < 10ms

End-to-End Latency:
Client → Producer → Kafka → Consumer → Processing
  10ms     20ms      5ms     100ms      500ms
= ~635ms total
```

### Throughput

```
Single Producer:
├── Messages/sec: 1,000 - 10,000
├── MB/sec: 1 - 10 MB
└── Limited by: Network, serialization

Single Consumer:
├── Messages/sec: 1,000 - 10,000
├── MB/sec: 1 - 10 MB
└── Limited by: Processing logic

Kafka Broker:
├── Messages/sec: 100,000+
├── MB/sec: 100+ MB
└── Limited by: Disk I/O
```

## Scaling Strategies

### Horizontal Scaling

```
Single Consumer (Current):
┌─────────────────────────┐
│ Partition 0 → Consumer 1│
└─────────────────────────┘
Throughput: 1,000 msg/sec

Multiple Consumers (Scaled):
┌─────────────────────────┐
│ Partition 0 → Consumer 1│
│ Partition 1 → Consumer 2│
│ Partition 2 → Consumer 3│
└─────────────────────────┘
Throughput: 3,000 msg/sec
```

### Vertical Scaling

```
Increase Resources:
├── More CPU cores → Parallel processing
├── More RAM → Larger buffers
└── Faster disk → Better Kafka performance
```

## Error Handling & Reliability

### Producer Reliability

```
Message Send Flow:
┌─────────────────────────────────────────┐
│ 1. Send to Kafka                        │
│    ↓                                    │
│ 2. Network error?                       │
│    ├─ Yes → Retry (up to 3 times)      │
│    └─ No → Continue                     │
│    ↓                                    │
│ 3. Kafka stores message                 │
│    ↓                                    │
│ 4. Wait for acks from all replicas     │
│    ↓                                    │
│ 5. Return success to producer           │
└─────────────────────────────────────────┘
```

### Consumer Reliability

```
Message Processing Flow:
┌─────────────────────────────────────────┐
│ 1. Receive message                      │
│    ↓                                    │
│ 2. Deserialize                          │
│    ├─ Error → Log and skip              │
│    └─ Success → Continue                │
│    ↓                                    │
│ 3. Process business logic               │
│    ├─ Error → Retry or DLQ              │
│    └─ Success → Continue                │
│    ↓                                    │
│ 4. Commit offset                        │
│    ↓                                    │
│ 5. Ready for next message               │
└─────────────────────────────────────────┘
```

## Monitoring & Observability

### Key Metrics

```
Producer Metrics:
├── Messages sent/sec
├── Send latency (p50, p95, p99)
├── Error rate
└── Retry rate

Consumer Metrics:
├── Messages consumed/sec
├── Processing latency
├── Consumer lag (how far behind)
└── Error rate

Kafka Metrics:
├── Disk usage
├── Network throughput
├── Partition count
└── Replication lag
```

## Summary

This architecture provides:

✅ **Decoupling:** Producer and consumer are independent  
✅ **Scalability:** Add more consumers for parallel processing  
✅ **Reliability:** Messages persisted, retries, acknowledgments  
✅ **Ordering:** Same-key messages processed in order  
✅ **Performance:** High throughput, low latency  
✅ **Flexibility:** Easy to add new consumers for new use cases  

## Next Steps

- Explore the [Producer README](../kafka-producer/README.md)
- Explore the [Consumer README](../kafka-consumer/README.md)
- Try the examples in [QUICK_START.md](../QUICK_START.md)
