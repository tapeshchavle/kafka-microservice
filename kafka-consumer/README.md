# Kafka Consumer Service

## Overview

The **Kafka Consumer Service** is a Spring Boot microservice that listens to Apache Kafka topics and processes events. It demonstrates the **consumer pattern** in event-driven architecture.

## Architecture

```
┌───────────────────────┐
│   Apache Kafka        │
│   Topic: user-events  │
│   Port: 9092          │
└───────────────────────┘
          │
          │ Polls for new messages
          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Consumer Service                          │
│                                                              │
│  ┌──────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │          │      │              │      │              │  │
│  │  Kafka   │─────▶│   Consumer   │─────▶│   Business   │  │
│  │ Listener │      │   Service    │      │    Logic     │  │
│  │          │      │              │      │              │  │
│  └──────────┘      └──────────────┘      └──────────────┘  │
│       │                   │                      │          │
│       │                   │                      │          │
│  Deserialize         Log Event            Process Event     │
│  from JSON           Extract Data         Based on Type     │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

## How It Works

### 1. Consumer Listens to Kafka

The consumer continuously polls Kafka for new messages:

```
┌─────────────────────────────────────────────────┐
│  Consumer Group: user-event-consumer-group      │
│  Topic: user-events                             │
│  Partition: 0                                   │
│                                                 │
│  Current Offset: 42                             │
│  Last Committed: 41                             │
└─────────────────────────────────────────────────┘
          │
          │ Poll every 100ms
          ▼
    New message at offset 42?
          │
          ├─ Yes ──▶ Fetch and process
          │
          └─ No ───▶ Wait and poll again
```

### 2. Kafka Delivers Message

When a new event is published:

```
Producer publishes event
     │
     ▼
┌─────────────────────┐
│  Kafka Broker       │
│  Stores at offset 42│
└─────────────────────┘
     │
     │ Consumer polls
     ▼
┌─────────────────────┐
│  Consumer receives  │
│  Message + Metadata │
│  - Partition: 0     │
│  - Offset: 42       │
│  - Key: user123     │
│  - Value: {...}     │
└─────────────────────┘
```

### 3. Listener Method Invoked

**File:** `EventConsumerService.java`

```java
@KafkaListener(topics = "${kafka.topic.name}", 
               groupId = "${spring.kafka.consumer.group-id}")
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
    
    // Process the event
    processEvent(event);
}
```

**What happens:**
- `@KafkaListener` annotation makes this method listen to Kafka
- Spring automatically deserializes JSON to `UserEvent` object
- Extracts partition and offset from message headers
- Logs all event details
- Calls business logic to process the event

### 4. Event Processing

```java
private void processEvent(UserEvent event) {
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
    }
}
```

### 5. Complete Message Flow

```
┌──────────────┐
│   Producer   │ Publishes event
└──────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│            Kafka Broker                  │
│                                          │
│  Topic: user-events                      │
│  Partition 0: [msg0, msg1, msg2, ...]    │
│               Offset: 0, 1, 2, ...       │
└──────────────────────────────────────────┘
       │
       │ Consumer polls
       ▼
┌──────────────────────────────────────────┐
│         Consumer Service                 │
│                                          │
│  1. Fetch message from Kafka             │
│  2. Deserialize JSON → UserEvent         │
│  3. Extract partition & offset           │
│  4. Invoke @KafkaListener method         │
│  5. Log event details                    │
│  6. Process based on eventType           │
│  7. Commit offset (mark as processed)    │
└──────────────────────────────────────────┘
       │
       ▼
   Processing Complete!
   (Offset committed, ready for next message)
```

## Kafka Configuration

**File:** `KafkaConsumerConfig.java`

```java
@Bean
public ConsumerFactory<String, UserEvent> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "user-event-consumer-group");
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    
    return new DefaultKafkaConsumerFactory<>(configProps);
}
```

**Configuration explained:**
- **BOOTSTRAP_SERVERS:** Kafka broker address
- **GROUP_ID:** Consumer group name (enables load balancing)
- **KEY_DESERIALIZER:** Converts bytes to String (userId)
- **VALUE_DESERIALIZER:** Converts JSON bytes to UserEvent object
- **AUTO_OFFSET_RESET:** `earliest` = read from beginning if no offset stored

## Configuration File

**File:** `application.yml`

```yaml
server:
  port: 8082                          # Service runs on port 8082

spring:
  application:
    name: kafka-consumer
  kafka:
    bootstrap-servers: localhost:9092 # Kafka broker address
    consumer:
      group-id: user-event-consumer-group  # Consumer group
      auto-offset-reset: earliest     # Start from beginning
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"  # Trust all packages for deserialization

kafka:
  topic:
    name: user-events                 # Topic to consume from
```

## Kafka Concepts Explained

### Consumer Groups

A **consumer group** is a set of consumers working together to consume a topic.

```
Topic: user-events (3 partitions)
┌─────────────┬─────────────┬─────────────┐
│ Partition 0 │ Partition 1 │ Partition 2 │
└─────────────┴─────────────┴─────────────┘
      │              │              │
      ▼              ▼              ▼
┌──────────┐   ┌──────────┐   ┌──────────┐
│Consumer 1│   │Consumer 2│   │Consumer 3│
└──────────┘   └──────────┘   └──────────┘
      └──────────────┬──────────────┘
              Consumer Group
         "user-event-consumer-group"
```

**Benefits:**
- **Load balancing:** Each partition assigned to one consumer
- **Fault tolerance:** If one consumer fails, others take over
- **Scalability:** Add more consumers to process faster

### Offset Management

**Offsets** track which messages have been processed:

```
Partition 0:
┌────┬────┬────┬────┬────┬────┐
│ 0  │ 1  │ 2  │ 3  │ 4  │ 5  │  ← Messages
└────┴────┴────┴────┴────┴────┘
           ▲
           │
    Current Offset: 2
    (Next message to read: 3)
```

**Auto-commit:**
- Consumer automatically commits offset after processing
- If consumer crashes, it resumes from last committed offset
- No duplicate processing (at-least-once delivery)

### Deserialization

Converting bytes back to objects:

```
Kafka Message (bytes)
     │
     ▼
┌─────────────────────────────────┐
│  JsonDeserializer               │
│  Converts JSON bytes to object  │
└─────────────────────────────────┘
     │
     ▼
UserEvent Object
{
  userId: "user123",
  eventType: CREATED,
  metadata: "...",
  timestamp: "2025-11-23T01:25:00"
}
```

## Example Scenarios

### Scenario 1: New User Registration

**Producer publishes:**
```json
{
  "userId": "newuser123",
  "eventType": "CREATED",
  "metadata": "Registered via email"
}
```

**Consumer receives and logs:**
```
========================================
Received event from partition: 0 with offset: 42
Event Details:
  User ID: newuser123
  Event Type: CREATED
  Metadata: Registered via email
  Timestamp: 2025-11-23T01:25:00
========================================
Processing user creation for: newuser123
```

**Consumer processes:**
1. Send welcome email to user
2. Create user profile in database
3. Update analytics dashboard
4. Trigger onboarding workflow

### Scenario 2: Profile Update

**Producer publishes:**
```json
{
  "userId": "user456",
  "eventType": "UPDATED",
  "metadata": "Profile picture changed"
}
```

**Consumer receives and processes:**
1. Update user cache
2. Invalidate CDN cache
3. Sync to search index
4. Notify followers of update

### Scenario 3: Account Deletion

**Producer publishes:**
```json
{
  "userId": "user789",
  "eventType": "DELETED",
  "metadata": "Account deletion requested"
}
```

**Consumer receives and processes:**
1. Remove personal data (GDPR compliance)
2. Cancel active subscriptions
3. Archive user data for retention period
4. Update billing system

## Message Processing Guarantees

### At-Least-Once Delivery

```
Message arrives → Process → Commit offset
                     │
                     ▼
              If crash happens here
                     │
                     ▼
         Message reprocessed on restart
```

**Implication:** Your processing logic should be **idempotent** (safe to run multiple times)

### Ordering Guarantee

Messages with the **same key** are always in order:

```
userId: "user123"
  ↓
Partition 0:
  Offset 10: CREATED
  Offset 11: UPDATED
  Offset 12: UPDATED
  Offset 13: DELETED

Consumer processes in this exact order ✓
```

## Monitoring & Logging

### Successful Processing

```
INFO: ========================================
INFO: Received event from partition: 0 with offset: 42
INFO: Event Details:
INFO:   User ID: user123
INFO:   Event Type: CREATED
INFO:   Metadata: New user registered
INFO:   Timestamp: 2025-11-23T01:25:00
INFO: ========================================
INFO: Processing user creation for: user123
```

### Error Handling

If deserialization fails:
```
ERROR: Failed to deserialize message at offset 42
ERROR: Invalid JSON format
WARN: Skipping message and committing offset
```

## Running the Service

### Standalone
```bash
cd kafka-consumer
mvn spring-boot:run
```

### With Docker
```bash
# Start Kafka first
docker-compose up -d

# Then start consumer
cd kafka-consumer
mvn spring-boot:run
```

### One-Click (Recommended)
```bash
# Double-click start-all.bat (starts everything)
```

## Testing

### 1. Start the Consumer
```bash
cd kafka-consumer
mvn spring-boot:run
```

Wait for:
```
Started KafkaConsumerApplication in X seconds
```

### 2. Publish a Test Event

From another terminal:
```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","eventType":"CREATED","metadata":"Test event"}'
```

### 3. Check Consumer Logs

You should see:
```
========================================
Received event from partition: 0 with offset: 0
Event Details:
  User ID: test123
  Event Type: CREATED
  Metadata: Test event
  Timestamp: 2025-11-23T01:25:00
========================================
Processing user creation for: test123
```

## Advanced Features

### Multiple Consumers (Scaling)

Run multiple instances for parallel processing:

```
Terminal 1:
cd kafka-consumer
mvn spring-boot:run -Dserver.port=8082

Terminal 2:
cd kafka-consumer
mvn spring-boot:run -Dserver.port=8083

Terminal 3:
cd kafka-consumer
mvn spring-boot:run -Dserver.port=8084
```

Kafka automatically distributes partitions among consumers!

### Consumer Lag Monitoring

Check how far behind the consumer is:

```bash
# View consumer group details
docker exec -it kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group user-event-consumer-group
```

Output:
```
TOPIC         PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
user-events   0          42              42              0
```

**LAG = 0** means consumer is caught up ✓

## Troubleshooting

### Issue: Consumer not receiving messages
**Check:**
1. Is Kafka running? `docker-compose ps`
2. Is producer publishing? Check producer logs
3. Is topic name correct? `user-events`
4. Is consumer group ID correct?

### Issue: Deserialization errors
**Solution:** Ensure `UserEvent` class matches in both services

### Issue: Consumer keeps reprocessing same message
**Cause:** Exception thrown before offset commit
**Solution:** Add try-catch in processing logic

### Issue: Messages out of order
**Cause:** Multiple partitions
**Solution:** Use same key (userId) for related events

## Best Practices

### 1. Idempotent Processing
```java
// Bad: Not idempotent
void processEvent(UserEvent event) {
    database.insert(event);  // Fails if run twice
}

// Good: Idempotent
void processEvent(UserEvent event) {
    database.upsert(event);  // Safe to run multiple times
}
```

### 2. Error Handling
```java
@KafkaListener(topics = "user-events")
public void consumeEvent(UserEvent event) {
    try {
        processEvent(event);
    } catch (Exception e) {
        log.error("Failed to process event: {}", event, e);
        // Send to dead letter queue
        // Or retry with exponential backoff
    }
}
```

### 3. Graceful Shutdown
Spring Boot handles this automatically, but ensure:
- Processing completes before shutdown
- Offsets are committed
- Resources are cleaned up

## Related Files

- `EventConsumerService.java` - Kafka listener and processing logic
- `KafkaConsumerConfig.java` - Kafka consumer configuration
- `UserEvent.java` - Event data model
- `application.yml` - Service configuration

## Next Steps

- Add dead letter queue for failed messages
- Implement retry logic with exponential backoff
- Add metrics and monitoring (Prometheus/Grafana)
- Configure multiple partitions for scalability
- Add database persistence for processed events
- Implement exactly-once semantics
