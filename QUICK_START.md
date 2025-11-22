# ⚡ ONE-CLICK STARTUP - QUICK REFERENCE

## 🎯 Absolute Easiest Method

### Windows Users:
```
1. Make sure Docker Desktop is running
2. Double-click: start-all.bat
3. Wait 60 seconds
4. Done! ✅
```

### What Happens:
```
start-all.bat
    ↓
[1] Starts Kafka + Zookeeper (Docker)
    ↓
[2] Waits 10 seconds for Kafka to be ready
    ↓
[3] Starts Consumer (port 8082)
    ↓
[4] Waits 5 seconds
    ↓
[5] Starts Producer (port 8081)
    ↓
✅ All Running!
```

---

## 🖱️ VS Code Method

```
Ctrl+Shift+P
    ↓
Type: "Tasks: Run Task"
    ↓
Select: "🚀 Start All (Kafka + Services)"
    ↓
Wait 60 seconds
    ↓
✅ All Running!
```

---

## 🧪 Test It

```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"test123","eventType":"CREATED","metadata":"Hello Kafka!"}'
```

**Expected:** Fast response (< 1 second)
```
Event published successfully: test123
```

---

## 🛑 Stop Everything

- **Windows:** Double-click `stop-all.bat`
- **VS Code:** Ctrl+Shift+P → "🛑 Stop All Services"
- **Manual:** `docker-compose down`

---

## 📊 Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Producer API | http://localhost:8081 | Publish events |
| Consumer | http://localhost:8082 | Process events |
| Kafka | localhost:9092 | Message broker |

---

## ⚠️ Prerequisites

✅ Docker Desktop installed and **running**  
✅ Java 17 installed  
✅ Maven installed  

---

## 🎉 That's It!

No complex setup, no multiple terminals, just **one click** and everything runs!
