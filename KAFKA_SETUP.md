# Quick Kafka Setup Guide

## Problem
Your microservices are running but can't connect to Kafka because Kafka isn't started yet.

## Solution: Choose One Method

### Method 1: Docker (Easiest - 1 Command!)

**Requirements:** Docker Desktop installed

**Steps:**
```bash
# Start Kafka and Zookeeper
docker-compose up -d

# Verify they're running
docker-compose ps

# When done, stop them
docker-compose down
```

That's it! Now your microservices will work.

---

### Method 2: Manual Kafka Installation (No Docker)

**For Windows:**

1. **Download Kafka:**
   - Visit: https://kafka.apache.org/downloads
   - Download: `kafka_2.13-3.6.0.tgz` (or latest version)
   - Extract to: `C:\kafka`

2. **Start Zookeeper** (Open PowerShell Terminal 1):
   ```powershell
   cd C:\kafka\kafka_2.13-3.6.0
   .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
   ```
   
   Wait for: `binding to port 0.0.0.0/0.0.0.0:2181`

3. **Start Kafka** (Open PowerShell Terminal 2):
   ```powershell
   cd C:\kafka\kafka_2.13-3.6.0
   .\bin\windows\kafka-server-start.bat .\config\server.properties
   ```
   
   Wait for: `[KafkaServer id=0] started`

**For Linux/Mac:**

```bash
# Download and extract
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0

# Start Zookeeper (background)
bin/zookeeper-server-start.sh config/zookeeper.properties &

# Start Kafka (background)
bin/kafka-server-start.sh config/server.properties &
```

---

## After Kafka is Running

1. **Your microservices will automatically connect**
2. **The error logs will stop**
3. **Test the API:**

```bash
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"user123\",\"eventType\":\"CREATED\",\"metadata\":\"Test event\"}"
```

**Expected Response (fast, < 1 second):**
```
Event published successfully: user123
```

**Check Consumer Logs:**
You'll see:
```
========================================
Received event from partition: 0 with offset: 0
Event Details:
  User ID: user123
  Event Type: CREATED
  ...
========================================
```

---

## Why This Happens

- Your Spring Boot services are configured to connect to `localhost:9092`
- If Kafka isn't running on that port, they keep retrying
- Once Kafka starts, the connection succeeds immediately

## Recommended: Use Docker

Docker is the easiest way because:
- ✅ One command: `docker-compose up -d`
- ✅ No manual download/extraction
- ✅ Easy to stop: `docker-compose down`
- ✅ Consistent across Windows/Mac/Linux
- ✅ No PATH or environment variable setup needed

If you have Docker installed, just run:
```bash
docker-compose up -d
```

Then your microservices will work perfectly!
