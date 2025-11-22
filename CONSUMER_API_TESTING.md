# Consumer API Testing Guide

## Overview

The consumer service now exposes REST API endpoints to retrieve all consumed events. This allows you to verify that events published by the producer are being received and processed.

## New Endpoints

### 1. Get All Consumed Events

**Endpoint:** `GET /api/events/consumed`

**Description:** Returns all events consumed since the service started

**Example:**
```bash
curl http://localhost:8082/api/events/consumed
```

**Response:**
```json
[
  {
    "userId": "user123",
    "eventType": "CREATED",
    "metadata": "New user registered",
    "timestamp": "2025-11-23T01:25:00"
  },
  {
    "userId": "user456",
    "eventType": "UPDATED",
    "metadata": "Profile updated",
    "timestamp": "2025-11-23T01:26:00"
  }
]
```

---

### 2. Get Event Count

**Endpoint:** `GET /api/events/consumed/count`

**Description:** Returns the total number of events consumed

**Example:**
```bash
curl http://localhost:8082/api/events/consumed/count
```

**Response:**
```json
{
  "totalEvents": 5,
  "message": "Total events consumed since service started"
}
```

---

### 3. Get Events by User ID

**Endpoint:** `GET /api/events/consumed/user/{userId}`

**Description:** Returns all events for a specific user

**Example:**
```bash
curl http://localhost:8082/api/events/consumed/user/user123
```

**Response:**
```json
[
  {
    "userId": "user123",
    "eventType": "CREATED",
    "metadata": "New user registered",
    "timestamp": "2025-11-23T01:25:00"
  },
  {
    "userId": "user123",
    "eventType": "UPDATED",
    "metadata": "Profile updated",
    "timestamp": "2025-11-23T01:30:00"
  }
]
```

---

### 4. Get Events by Type

**Endpoint:** `GET /api/events/consumed/type/{eventType}`

**Description:** Returns all events of a specific type (CREATED, UPDATED, DELETED)

**Example:**
```bash
curl http://localhost:8082/api/events/consumed/type/CREATED
```

**Response:**
```json
[
  {
    "userId": "user123",
    "eventType": "CREATED",
    "metadata": "New user registered",
    "timestamp": "2025-11-23T01:25:00"
  },
  {
    "userId": "user789",
    "eventType": "CREATED",
    "metadata": "Another user",
    "timestamp": "2025-11-23T01:27:00"
  }
]
```

---

### 5. Get Latest Event

**Endpoint:** `GET /api/events/consumed/latest`

**Description:** Returns the most recently consumed event

**Example:**
```bash
curl http://localhost:8082/api/events/consumed/latest
```

**Response:**
```json
{
  "userId": "user999",
  "eventType": "DELETED",
  "metadata": "User deleted account",
  "timestamp": "2025-11-23T01:35:00"
}
```

---

### 6. Clear All Events

**Endpoint:** `DELETE /api/events/consumed`

**Description:** Clears all consumed events from memory

**Example:**
```bash
curl -X DELETE http://localhost:8082/api/events/consumed
```

**Response:**
```json
{
  "message": "All consumed events cleared successfully"
}
```

---

### 7. Health Check

**Endpoint:** `GET /api/events/health`

**Description:** Check consumer service health and event count

**Example:**
```bash
curl http://localhost:8082/api/events/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "kafka-consumer",
  "eventsConsumed": 10
}
```

---

## Complete Testing Workflow

### Step 1: Start All Services

```bash
# Double-click start-all.bat or run:
docker-compose up -d
cd kafka-consumer && mvn spring-boot:run
cd kafka-producer && mvn spring-boot:run
```

### Step 2: Publish Some Events

```bash
# Event 1: Create user
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","eventType":"CREATED","metadata":"New user"}'

# Event 2: Update user
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","eventType":"UPDATED","metadata":"Profile updated"}'

# Event 3: Create another user
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user456","eventType":"CREATED","metadata":"Another user"}'

# Event 4: Delete user
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user789","eventType":"DELETED","metadata":"User deleted"}'
```

### Step 3: Verify Events Were Consumed

```bash
# Get all consumed events
curl http://localhost:8082/api/events/consumed

# Get count
curl http://localhost:8082/api/events/consumed/count
# Expected: {"totalEvents": 4, ...}

# Get events for user123
curl http://localhost:8082/api/events/consumed/user/user123
# Expected: 2 events (CREATED and UPDATED)

# Get all CREATED events
curl http://localhost:8082/api/events/consumed/type/CREATED
# Expected: 2 events (user123 and user456)

# Get latest event
curl http://localhost:8082/api/events/consumed/latest
# Expected: user789 DELETED event
```

### Step 4: Clear Events (Optional)

```bash
# Clear all events from memory
curl -X DELETE http://localhost:8082/api/events/consumed

# Verify cleared
curl http://localhost:8082/api/events/consumed/count
# Expected: {"totalEvents": 0, ...}
```

---

## PowerShell Examples

### Publish Event
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"userId":"user123","eventType":"CREATED","metadata":"Test"}'
```

### Get All Events
```powershell
Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed | 
  Select-Object -ExpandProperty Content | 
  ConvertFrom-Json | 
  ConvertTo-Json -Depth 10
```

### Get Event Count
```powershell
(Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed/count).Content | ConvertFrom-Json
```

### Get Events by User
```powershell
Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed/user/user123 | 
  Select-Object -ExpandProperty Content
```

---

## Browser Testing

You can also test in your browser:

1. **Get all events:**  
   Open: `http://localhost:8082/api/events/consumed`

2. **Get count:**  
   Open: `http://localhost:8082/api/events/consumed/count`

3. **Get events for user123:**  
   Open: `http://localhost:8082/api/events/consumed/user/user123`

4. **Get CREATED events:**  
   Open: `http://localhost:8082/api/events/consumed/type/CREATED`

5. **Get latest event:**  
   Open: `http://localhost:8082/api/events/consumed/latest`

6. **Health check:**  
   Open: `http://localhost:8082/api/events/health`

---

## Testing with Postman

### Collection Setup

1. Create a new collection: "Kafka Microservices"
2. Add requests:

**Producer - Publish Event:**
- Method: POST
- URL: `http://localhost:8081/api/events/publish`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
  ```json
  {
    "userId": "user123",
    "eventType": "CREATED",
    "metadata": "Test event"
  }
  ```

**Consumer - Get All Events:**
- Method: GET
- URL: `http://localhost:8082/api/events/consumed`

**Consumer - Get Count:**
- Method: GET
- URL: `http://localhost:8082/api/events/consumed/count`

**Consumer - Get by User:**
- Method: GET
- URL: `http://localhost:8082/api/events/consumed/user/{{userId}}`
- Variable: `userId = user123`

**Consumer - Clear Events:**
- Method: DELETE
- URL: `http://localhost:8082/api/events/consumed`

---

## Automated Test Script

### Bash Script (Linux/Mac/Git Bash)

```bash
#!/bin/bash

echo "=== Kafka Microservices Test ==="
echo ""

# Publish 3 events
echo "1. Publishing events..."
curl -s -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","eventType":"CREATED","metadata":"First user"}'
echo ""

curl -s -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user2","eventType":"CREATED","metadata":"Second user"}'
echo ""

curl -s -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user1","eventType":"UPDATED","metadata":"Updated profile"}'
echo ""

# Wait for processing
echo "2. Waiting 2 seconds for processing..."
sleep 2

# Get count
echo "3. Getting event count..."
curl -s http://localhost:8082/api/events/consumed/count | jq
echo ""

# Get all events
echo "4. Getting all events..."
curl -s http://localhost:8082/api/events/consumed | jq
echo ""

# Get events for user1
echo "5. Getting events for user1..."
curl -s http://localhost:8082/api/events/consumed/user/user1 | jq
echo ""

echo "=== Test Complete ==="
```

### PowerShell Script (Windows)

```powershell
Write-Host "=== Kafka Microservices Test ===" -ForegroundColor Green
Write-Host ""

# Publish 3 events
Write-Host "1. Publishing events..." -ForegroundColor Yellow
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user1","eventType":"CREATED","metadata":"First user"}' | Out-Null
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user2","eventType":"CREATED","metadata":"Second user"}' | Out-Null
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user1","eventType":"UPDATED","metadata":"Updated profile"}' | Out-Null

# Wait for processing
Write-Host "2. Waiting 2 seconds for processing..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

# Get count
Write-Host "3. Getting event count..." -ForegroundColor Yellow
$count = (Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed/count).Content | ConvertFrom-Json
Write-Host "Total Events: $($count.totalEvents)" -ForegroundColor Cyan

# Get all events
Write-Host "4. Getting all events..." -ForegroundColor Yellow
$events = (Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed).Content | ConvertFrom-Json
$events | Format-Table -AutoSize

Write-Host "=== Test Complete ===" -ForegroundColor Green
```

---

## Important Notes

### Memory Storage
- Events are stored **in memory** (not persisted to database)
- Events are **lost when the consumer service restarts**
- Use the `DELETE /api/events/consumed` endpoint to clear events during testing

### Thread Safety
- Uses `CopyOnWriteArrayList` for thread-safe storage
- Safe for concurrent reads and writes
- No locking required

### Performance
- Fast retrieval (in-memory)
- Suitable for testing and development
- For production, consider persisting to a database

---

## Troubleshooting

### No events returned
**Check:**
1. Is the consumer service running? `curl http://localhost:8082/api/events/health`
2. Did you publish events? Check producer logs
3. Is Kafka running? `docker-compose ps`

### Events not appearing immediately
- Wait 1-2 seconds after publishing
- Consumer polls every 100ms
- Processing takes time

### Old events still showing
- Events persist in memory until service restart
- Use `DELETE /api/events/consumed` to clear

---

## Next Steps

- Add database persistence for events
- Add pagination for large event lists
- Add filtering by date range
- Add event search functionality
- Add WebSocket for real-time event updates
