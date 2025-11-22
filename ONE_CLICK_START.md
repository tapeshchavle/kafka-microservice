# 🚀 One-Click Startup Guide

## Method 1: Double-Click the Batch File (Easiest!)

### Windows:
1. **Double-click** `start-all.bat` in the `c:\fastapi` folder
2. Wait 30-60 seconds for everything to start
3. Done! ✅

This will:
- ✅ Start Kafka and Zookeeper (Docker)
- ✅ Start Consumer service (port 8082)
- ✅ Start Producer service (port 8081)

### Linux/Mac:
```bash
chmod +x start-all.sh
./start-all.sh
```

---

## Method 2: VS Code Task (One Click in Editor)

1. Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
2. Type: **"Tasks: Run Task"**
3. Select: **"🚀 Start All (Kafka + Services)"**
4. Wait 30-60 seconds
5. Done! ✅

---

## Method 3: VS Code Launch (F5)

1. Press `F5`
2. Select **"Launch Both Services"** from dropdown
3. This starts the microservices (Kafka must be running first)

---

## Testing After Startup

Once all services are running (wait 30-60 seconds), test with:

### PowerShell:
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user123","eventType":"CREATED","metadata":"Test event"}'
```

### Curl (Git Bash/WSL/Linux/Mac):
```bash
curl -X POST http://localhost:8081/api/events/publish \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","eventType":"CREATED","metadata":"Test event"}'
```

### Expected Result:
- **Producer response:** `Event published successfully: user123`
- **Consumer logs:** Shows the received event with details

---

## Stopping Everything

### Windows:
Double-click `stop-all.bat`

### VS Code:
`Ctrl+Shift+P` → "Tasks: Run Task" → "🛑 Stop All Services"

### Manual:
```bash
docker-compose down
```
Then close the Consumer and Producer terminal windows.

---

## What Each Method Does

| Method | Starts Kafka? | Starts Services? | Easiest? |
|--------|---------------|------------------|----------|
| `start-all.bat` | ✅ Yes | ✅ Yes | ⭐ **Best!** |
| VS Code Task | ✅ Yes | ✅ Yes | ⭐ **Best!** |
| VS Code F5 | ❌ No | ✅ Yes | Manual Kafka |

---

## Prerequisites

- **Docker Desktop** must be installed and running
- **Java 17** installed
- **Maven** installed

---

## Troubleshooting

### "Docker command not found"
- Install Docker Desktop: https://www.docker.com/products/docker-desktop

### "mvn command not found"
- Install Maven: https://maven.apache.org/download.cgi
- Or use VS Code's built-in Maven support

### Services won't start
1. Make sure Docker Desktop is running
2. Check if ports 8081, 8082, 9092 are free
3. Run `docker-compose ps` to verify Kafka is running

---

## Summary

**Absolute Easiest Way:**
1. Make sure Docker Desktop is running
2. Double-click `start-all.bat`
3. Wait 60 seconds
4. Test the API!

That's it! Everything runs with one click! 🎉
