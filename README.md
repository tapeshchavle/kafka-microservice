# Kafka Microservices Demo

This project demonstrates a simple event-driven architecture using two Spring Boot microservices with Apache Kafka:
- **kafka-producer**: Publishes user events via REST API
- **kafka-consumer**: Subscribes to and processes user events

## 🚀 Quick Start with VS Code (One-Click Run)

**Easiest Way - Just Double-Click!**

1. Make sure **Docker Desktop** is running
2. Double-click **`start-all.bat`** in the project folder
3. Wait 60 seconds for everything to start
4. Done! Test with the curl command below 🎉

**Alternative - VS Code Task:**
1. Press `Ctrl+Shift+P` → "Tasks: Run Task"
2. Select **"🚀 Start All (Kafka + Services)"**
3. Wait 60 seconds

See [ONE_CLICK_START.md](ONE_CLICK_START.md) for detailed instructions.

## Prerequisites

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.6+** ([Download](https://maven.apache.org/download.cgi))
- **Apache Kafka** ([Download](https://kafka.apache.org/downloads))

## Quick Start

### Step 1: Start Kafka

#### Windows

1. Download and extract Kafka from https://kafka.apache.org/downloads
2. Open **two separate PowerShell terminals**

**Terminal 1 - Start Zookeeper:**
```powershell
cd kafka_2.13-3.6.0
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

**Terminal 2 - Start Kafka:**
```powershell
cd kafka_2.13-3.6.0
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

#### Linux/Mac

```bash
cd kafka_2.13-3.6.0

# Start Zookeeper (in background)
bin/zookeeper-server-start.sh config/zookeeper.properties &

# Start Kafka (in background)
bin/kafka-server-start.sh config/server.properties &
```

### Step 2: Start Consumer Service

Open a **new terminal** and run:

```bash
cd kafka-consumer
mvn spring-boot:run
```

Wait for the message: `Started KafkaConsumerApplication`

### Step 3: Start Producer Service

Open **another new terminal** and run:

```bash
cd kafka-producer
mvn spring-boot:run
```

Wait for the message: `Started KafkaProducerApplication`

### Step 4: Publish Events

Use curl or any REST client to publish events:

```bash
# Example 1: Create user event
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"user123\",\"eventType\":\"CREATED\",\"metadata\":\"New user registered\"}"

# Example 2: Update user event
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"user456\",\"eventType\":\"UPDATED\",\"metadata\":\"User profile updated\"}"

# Example 3: Delete user event
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"user789\",\"eventType\":\"DELETED\",\"metadata\":\"User account deleted\"}"
```

**PowerShell alternative:**
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user123","eventType":"CREATED","metadata":"New user registered"}'
```

### Step 5: Verify

**Check Producer Logs** (Terminal where producer is running):
```
Event published successfully: UserEvent(userId=user123, ...)
```

**Check Consumer Logs** (Terminal where consumer is running):
```
========================================
Received event from partition: 0 with offset: 0
Event Details:
  User ID: user123
  Event Type: CREATED
  Metadata: New user registered
  Timestamp: 2025-11-23T00:42:00
========================================
Processing user creation for: user123
```

## Project Structure

```
fastapi/
├── kafka-producer/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/producer/
│       │   ├── KafkaProducerApplication.java
│       │   ├── config/KafkaProducerConfig.java
│       │   ├── controller/EventController.java
│       │   ├── model/UserEvent.java
│       │   └── service/EventProducerService.java
│       └── resources/application.yml
│
├── kafka-consumer/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/consumer/
│       │   ├── KafkaConsumerApplication.java
│       │   ├── config/KafkaConsumerConfig.java
│       │   ├── model/UserEvent.java
│       │   └── service/EventConsumerService.java
│       └── resources/application.yml
│
└── README.md
```

## API Endpoints

### Producer Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/events/publish` | Publish a user event |
| GET | `/api/events/health` | Health check |

**Request Body Example:**
```json
{
  "userId": "user123",
  "eventType": "CREATED",
  "metadata": "Additional information"
}
```

**Event Types:** `CREATED`, `UPDATED`, `DELETED`

## Configuration

### Producer (kafka-producer/src/main/resources/application.yml)
- **Port:** 8081
- **Kafka Topic:** user-events
- **Bootstrap Servers:** localhost:9092

### Consumer (kafka-consumer/src/main/resources/application.yml)
- **Port:** 8082
- **Kafka Topic:** user-events
- **Consumer Group:** user-event-consumer-group
- **Bootstrap Servers:** localhost:9092

## Troubleshooting

### Issue: "Connection to node -1 could not be established"
**Solution:** Make sure Kafka and Zookeeper are running before starting the microservices.

### Issue: "Topic 'user-events' does not exist"
**Solution:** The topic will be auto-created on first message. If auto-creation is disabled, create it manually:

**Windows:**
```powershell
.\bin\windows\kafka-topics.bat --create --topic user-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

**Linux/Mac:**
```bash
bin/kafka-topics.sh --create --topic user-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### Issue: Consumer not receiving messages
**Solution:** 
1. Check that both services are connected to the same Kafka instance (localhost:9092)
2. Verify the topic name matches in both services (user-events)
3. Check consumer logs for any deserialization errors

### Issue: Port already in use
**Solution:** Change the port in `application.yml` if 8081 or 8082 are already in use.

## Stopping the Services

1. Stop Producer and Consumer: Press `Ctrl+C` in their respective terminals
2. Stop Kafka: Press `Ctrl+C` in the Kafka terminal
3. Stop Zookeeper: Press `Ctrl+C` in the Zookeeper terminal

## Testing Multiple Events

You can test the system by sending multiple events in sequence:

```bash
# Send 5 events
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/events/publish \
    -H "Content-Type: application/json" \
    -d "{\"userId\":\"user$i\",\"eventType\":\"CREATED\",\"metadata\":\"Test event $i\"}"
  sleep 1
done
```

**PowerShell:**
```powershell
1..5 | ForEach-Object {
  Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body "{`"userId`":`"user$_`",`"eventType`":`"CREATED`",`"metadata`":`"Test event $_`"}"
  Start-Sleep -Seconds 1
}
```

## Next Steps

- Add more event types and handlers
- Implement error handling and dead letter queues
- Add monitoring and metrics
- Implement event replay functionality
- Add database persistence for events
- Configure multiple partitions for scalability

## License

