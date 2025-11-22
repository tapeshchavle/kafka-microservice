# Kafka Producer Service

## Overview

The **Kafka Producer Service** is a Spring Boot microservice that exposes a REST API to publish events to Apache Kafka. It demonstrates the **producer pattern** in event-driven architecture.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Producer Service                          │
│                                                              │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────┐  │
│  │              │      │              │      │          │  │
│  │ REST API     │─────▶│   Producer   │─────▶│  Kafka   │  │
│  │ Controller   │      │   Service    │      │ Template │  │
│  │              │      │              │      │          │  │
│  └──────────────┘      └──────────────┘      └──────────┘  │
│         │                     │                     │        │
│         │                     │                     │        │
│    HTTP POST            Business Logic         Serialize    │
│    (JSON)               + Validation           to JSON      │
│                                                              │
└──────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │   Apache Kafka        │
                    │   Topic: user-events  │
                    │   Port: 9092          │
                    └───────────────────────┘
```

## How It Works

### 1. Client Sends Event

A client (e.g., web app, mobile app, or curl) sends an HTTP POST request:

```bash
POST http://localhost:8081/api/events/publish
Content-Type: application/json

{
  "userId": "user123",
  "eventType": "CREATED",
  "metadata": "New user registered"
}
```

### 2. Controller Receives Request

**File:** `EventController.java`

```java
@PostMapping("/publish")
public ResponseEntity<String> publishEvent(@RequestBody UserEvent event) {
    // Set timestamp if not provided
    if (event.getTimestamp() == null) {
        event.setTimestamp(LocalDateTime.now());
    }
    
    // Delegate to service layer
    producerService.publishEvent(event);
    
    return ResponseEntity.ok("Event published successfully: " + event.getUserId());
}
```

**What happens:**
- Receives JSON payload
- Converts JSON to `UserEvent` object (Spring auto-deserialization)
- Adds timestamp if missing
- Calls the service layer

### 3. Service Publishes to Kafka

**File:** `EventProducerService.java`

```java
public void publishEvent(UserEvent event) {
    log.info("Publishing event: {}", event);
    
    // Send to Kafka asynchronously
    CompletableFuture<SendResult<String, UserEvent>> future = 
        kafkaTemplate.send(topicName, event.getUserId(), event);

    // Handle success/failure
    future.whenComplete((result, ex) -> {
        if (ex == null) {
            log.info("Event published successfully with offset: {}", 
                result.getRecordMetadata().offset());
        } else {
            log.error("Failed to publish event", ex);
        }
    });
}
```

**What happens:**
- Uses `KafkaTemplate` to send message
- **Key:** `userId` (for partitioning)
- **Value:** Complete `UserEvent` object
- **Topic:** `user-events`
- Asynchronous operation with callback

### 4. Kafka Configuration

**File:** `KafkaProducerConfig.java`

```java
@Bean
public ProducerFactory<String, UserEvent> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");  // Wait for all replicas
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);   // Retry 3 times
    
    return new DefaultKafkaProducerFactory<>(configProps);
}
```

**Configuration explained:**
- **BOOTSTRAP_SERVERS:** Kafka broker address
- **KEY_SERIALIZER:** Converts userId (String) to bytes
- **VALUE_SERIALIZER:** Converts UserEvent object to JSON bytes
- **ACKS:** `all` = wait for all replicas (most reliable)
- **RETRIES:** Retry failed sends up to 3 times

### 5. Message Flow Diagram

```
Client Request
     │
     ▼
┌─────────────────────┐
│  EventController    │  1. Receive HTTP POST
│  @PostMapping       │  2. Validate & add timestamp
└─────────────────────┘
     │
     ▼
┌─────────────────────┐
│ EventProducerService│  3. Log event
│  publishEvent()     │  4. Call kafkaTemplate.send()
└─────────────────────┘
     │
     ▼
┌─────────────────────┐
│   KafkaTemplate     │  5. Serialize to JSON
│                     │  6. Add headers
└─────────────────────┘
     │
     ▼
┌─────────────────────┐
│  Network Layer      │  7. Send over TCP to Kafka
└─────────────────────┘
     │
     ▼
┌─────────────────────┐
│   Kafka Broker      │  8. Store in topic partition
│   Topic: user-events│  9. Assign offset number
│   Partition: 0      │  10. Acknowledge receipt
└─────────────────────┘
     │
     ▼
   Success!
```

## Event Model

**File:** `UserEvent.java`

```java
public class UserEvent {
    private String userId;           // Unique user identifier
    private EventType eventType;     // CREATED, UPDATED, DELETED
    private String metadata;         // Additional information
    private LocalDateTime timestamp; // When event occurred
}
```

**Example JSON:**
```json
{
  "userId": "user123",
  "eventType": "CREATED",
  "metadata": "User registered from mobile app",
  "timestamp": "2025-11-23T01:25:00"
}
```

## API Endpoints

### 1. Publish Event

**Endpoint:** `POST /api/events/publish`

**Request:**
```json
{
  "userId": "user456",
  "eventType": "UPDATED",
  "metadata": "Profile picture changed"
}
```

**Response:**
```
HTTP/1.1 200 OK
Event published successfully: user456
```

**Curl Example:**
```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user789",
    "eventType": "CREATED",
    "metadata": "New user from signup page"
  }'
```

**PowerShell Example:**
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"userId":"user789","eventType":"CREATED","metadata":"Test"}'
```

### 2. Health Check

**Endpoint:** `GET /api/events/health`

**Response:**
```
HTTP/1.1 200 OK
Producer service is running
```

## Configuration

**File:** `application.yml`

```yaml
server:
  port: 8081                          # Service runs on port 8081

spring:
  application:
    name: kafka-producer
  kafka:
    bootstrap-servers: localhost:9092 # Kafka broker address
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                       # Wait for all replicas
      retries: 3                      # Retry failed sends

kafka:
  topic:
    name: user-events                 # Topic name
```

## Kafka Concepts Explained

### Topics
A **topic** is like a category or feed name. All events of the same type go to the same topic.
- Our topic: `user-events`
- Think of it as a message queue specifically for user-related events

### Partitions
Topics are divided into **partitions** for scalability.
- Messages with the same key go to the same partition
- We use `userId` as the key
- This ensures all events for one user are in order

### Offsets
Each message in a partition gets a unique **offset** (like an ID number).
- Offset 0: First message
- Offset 1: Second message
- Consumers use offsets to track what they've read

### Producers
A **producer** sends messages to Kafka.
- Our service is a producer
- It publishes `UserEvent` messages to the `user-events` topic

### Serialization
Converting objects to bytes for transmission.
- **Key:** String → bytes (StringSerializer)
- **Value:** UserEvent object → JSON → bytes (JsonSerializer)

## Example Scenarios

### Scenario 1: User Registration

```
1. User fills registration form
2. Backend calls: POST /api/events/publish
   {
     "userId": "newuser123",
     "eventType": "CREATED",
     "metadata": "Registered via email"
   }
3. Producer publishes to Kafka
4. Consumer receives and processes:
   - Send welcome email
   - Create user profile
   - Update analytics
```

### Scenario 2: Profile Update

```
1. User updates profile picture
2. Backend calls: POST /api/events/publish
   {
     "userId": "user456",
     "eventType": "UPDATED",
     "metadata": "Profile picture changed"
   }
3. Producer publishes to Kafka
4. Consumer receives and processes:
   - Resize image
   - Update CDN
   - Notify followers
```

### Scenario 3: Account Deletion

```
1. User deletes account
2. Backend calls: POST /api/events/publish
   {
     "userId": "user789",
     "eventType": "DELETED",
     "metadata": "Account deletion requested"
   }
3. Producer publishes to Kafka
4. Consumer receives and processes:
   - Remove personal data
   - Cancel subscriptions
   - Archive user data
```

## Reliability Features

### 1. Acknowledgments (acks=all)
```
Producer sends message
     ↓
Kafka leader receives
     ↓
Kafka replicates to followers
     ↓
All replicas acknowledge
     ↓
Producer receives confirmation
```

### 2. Retries
If sending fails, the producer automatically retries up to 3 times:
```
Attempt 1: Failed (network issue)
Attempt 2: Failed (broker busy)
Attempt 3: Success! ✓
```

### 3. Asynchronous Publishing
```java
CompletableFuture<SendResult> future = kafkaTemplate.send(...);

// Non-blocking - continues immediately
// Callback executes when complete

future.whenComplete((result, ex) -> {
    // Handle success or failure
});
```

## Monitoring & Logging

The service logs important events:

```
INFO: Publishing event: UserEvent(userId=user123, eventType=CREATED, ...)
INFO: Event published successfully with offset: 42
```

Or if there's an error:
```
ERROR: Failed to publish event: UserEvent(userId=user123, ...)
ERROR: Connection timeout to Kafka broker
```

## Running the Service

### Standalone
```bash
cd kafka-producer
mvn spring-boot:run
```

### With Docker
```bash
# Start Kafka first
docker-compose up -d

# Then start producer
cd kafka-producer
mvn spring-boot:run
```

### One-Click (Recommended)
```bash
# Double-click start-all.bat (starts everything)
```

## Testing

### Manual Test
```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"test1","eventType":"CREATED","metadata":"Test event"}'
```

### Expected Output
```
Event published successfully: test1
```

### Check Logs
```
INFO: Publishing event: UserEvent(userId=test1, eventType=CREATED, ...)
INFO: Event published successfully with offset: 0
```

## Troubleshooting

### Issue: "Connection refused to Kafka"
**Solution:** Start Kafka first
```bash
docker-compose up -d
```

### Issue: "Port 8081 already in use"
**Solution:** Change port in `application.yml`
```yaml
server:
  port: 8082  # Use different port
```

### Issue: Slow response
**Solution:** Check Kafka is running and accessible at localhost:9092

## Next Steps

- Add authentication to API endpoints
- Implement request validation
- Add metrics and monitoring
- Configure multiple Kafka brokers for high availability
- Add dead letter queue for failed messages

## Related Files

- `EventController.java` - REST API endpoints
- `EventProducerService.java` - Kafka publishing logic
- `KafkaProducerConfig.java` - Kafka configuration
- `UserEvent.java` - Event data model
- `application.yml` - Service configuration
