# Quick Test: Producer → Kafka → Consumer Verification

## ✅ What Was Added

The consumer service now has **REST API endpoints** to retrieve all consumed events!

### New Features:
- ✅ **In-memory storage** of all consumed events
- ✅ **REST API** to retrieve events
- ✅ **Filter by user ID** or event type
- ✅ **Event count** tracking
- ✅ **Latest event** retrieval

---

## 🚀 Quick Test (3 Steps)

### Step 1: Publish an Event (Producer)

```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","eventType":"CREATED","metadata":"Test event"}'
```

**Response:**
```
Event published successfully: test123
```

---

### Step 2: Retrieve Events (Consumer)

```bash
curl http://localhost:8082/api/events/consumed
```

**Response:**
```json
[
  {
    "userId": "test123",
    "eventType": "CREATED",
    "metadata": "Test event",
    "timestamp": "2025-11-23T01:35:00"
  }
]
```

---

### Step 3: Get Event Count

```bash
curl http://localhost:8082/api/events/consumed/count
```

**Response:**
```json
{
  "totalEvents": 1,
  "message": "Total events consumed since service started"
}
```

---

## 📊 Complete Flow Diagram

```
┌──────────────┐
│   Client     │
│   (curl)     │
└──────────────┘
       │
       │ 1. POST /api/events/publish
       │    {"userId":"test123","eventType":"CREATED",...}
       ▼
┌──────────────────────┐
│  Producer Service    │
│  Port: 8081          │
└──────────────────────┘
       │
       │ 2. Publish to Kafka
       ▼
┌──────────────────────┐
│  Kafka Broker        │
│  Topic: user-events  │
│  Offset: 42          │
└──────────────────────┘
       │
       │ 3. Consumer polls
       ▼
┌──────────────────────┐
│  Consumer Service    │
│  Port: 8082          │
│                      │
│  📦 In-Memory Store  │
│  [event1, event2...] │
└──────────────────────┘
       │
       │ 4. GET /api/events/consumed
       ▼
┌──────────────┐
│   Client     │
│  (Browser/   │
│   curl)      │
└──────────────┘
```

---

## 🎯 All Consumer Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/events/consumed` | GET | Get all events |
| `/api/events/consumed/count` | GET | Get event count |
| `/api/events/consumed/user/{userId}` | GET | Get events by user |
| `/api/events/consumed/type/{type}` | GET | Get events by type |
| `/api/events/consumed/latest` | GET | Get latest event |
| `/api/events/consumed` | DELETE | Clear all events |
| `/api/events/health` | GET | Health check |

---

## 💡 Example Workflow

```bash
# 1. Publish 3 events
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d '{"userId":"user1","eventType":"CREATED","metadata":"First"}'
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d '{"userId":"user2","eventType":"CREATED","metadata":"Second"}'
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d '{"userId":"user1","eventType":"UPDATED","metadata":"Updated"}'

# 2. Wait 2 seconds
sleep 2

# 3. Get all events
curl http://localhost:8082/api/events/consumed

# 4. Get events for user1 only
curl http://localhost:8082/api/events/consumed/user/user1

# 5. Get only CREATED events
curl http://localhost:8082/api/events/consumed/type/CREATED

# 6. Get total count
curl http://localhost:8082/api/events/consumed/count

# 7. Clear all events
curl -X DELETE http://localhost:8082/api/events/consumed
```

---

## 🌐 Browser Testing

Just open these URLs in your browser:

- **All events:** http://localhost:8082/api/events/consumed
- **Event count:** http://localhost:8082/api/events/consumed/count
- **Latest event:** http://localhost:8082/api/events/consumed/latest
- **Health check:** http://localhost:8082/api/events/health

---

## 📝 PowerShell Examples

```powershell
# Publish event
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish `
  -Method POST -ContentType "application/json" `
  -Body '{"userId":"test","eventType":"CREATED","metadata":"Test"}'

# Get all events
(Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed).Content | ConvertFrom-Json

# Get count
(Invoke-WebRequest -Uri http://localhost:8082/api/events/consumed/count).Content | ConvertFrom-Json
```

---

## 🔧 How It Works

### Consumer Service Enhancement

**Before:**
- Consumer received events
- Logged to console
- Events were lost

**After:**
- Consumer receives events ✓
- Logs to console ✓
- **Stores in memory** ✓
- **Exposes REST API** ✓
- **Allows retrieval** ✓

### Storage Mechanism

```java
// Thread-safe in-memory storage
private final List<UserEvent> consumedEvents = new CopyOnWriteArrayList<>();
private final ConcurrentHashMap<String, List<UserEvent>> eventsByUser = new ConcurrentHashMap<>();

// When event arrives
@KafkaListener(...)
public void consumeEvent(UserEvent event) {
    // Store in memory
    consumedEvents.add(event);
    eventsByUser.get(event.getUserId()).add(event);
    
    // Process as usual
    processEvent(event);
}
```

---

## ⚠️ Important Notes

1. **In-Memory Only:** Events are stored in RAM, not a database
2. **Lost on Restart:** Events clear when consumer service restarts
3. **Thread-Safe:** Uses concurrent collections for safety
4. **Testing Purpose:** Perfect for development and testing
5. **Production:** Consider adding database persistence

---

## 📚 Full Documentation

See [CONSUMER_API_TESTING.md](CONSUMER_API_TESTING.md) for complete details!

---

## ✨ Summary

Now you can:
1. ✅ Publish events via Producer API
2. ✅ Verify consumption via Consumer API
3. ✅ Filter events by user or type
4. ✅ Track event counts
5. ✅ Clear events for fresh testing

**Perfect for testing and verifying your Kafka setup!** 🎉
