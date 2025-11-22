# Quick Start Guide - VS Code

## Prerequisites

1. **Install VS Code Extensions:**
   - Extension Pack for Java (by Microsoft)
   - Spring Boot Extension Pack (by VMware)

2. **Ensure you have:**
   - Java 17 installed
   - Maven installed
   - Kafka running locally

## Running with VS Code (Single Click)

### Option 1: Launch Both Services Together (Recommended)

1. Press `F5` or click the **Run and Debug** icon in the sidebar
2. From the dropdown at the top, select **"Launch Both Services"**
3. Click the green play button ▶️

This will start both the consumer and producer in separate terminals!

### Option 2: Launch Services Individually

From the Run and Debug dropdown, you can also select:
- **"Launch Producer"** - Runs only the producer service
- **"Launch Consumer"** - Runs only the consumer service

## Using Tasks (Alternative Method)

Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac) and type "Run Task", then select:

- **"Run Producer"** - Start producer service
- **"Run Consumer"** - Start consumer service
- **"Build All"** - Build both projects
- **"Build Producer"** - Build only producer
- **"Build Consumer"** - Build only consumer

## Testing the Services

Once both services are running:

1. Open a new terminal in VS Code (`Ctrl+` ` or Terminal → New Terminal)
2. Run the test command:

```bash
curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"user123\",\"eventType\":\"CREATED\",\"metadata\":\"Test event\"}"
```

Or use PowerShell:
```powershell
Invoke-WebRequest -Uri http://localhost:8081/api/events/publish -Method POST -ContentType "application/json" -Body '{"userId":"user123","eventType":"CREATED","metadata":"Test event"}'
```

## What Happens When You Click Run?

1. **VS Code** reads `.vscode/launch.json`
2. **Maven** builds the projects (if needed)
3. **Spring Boot** starts the applications
4. **Consumer** starts first (to ensure it's ready to receive events)
5. **Producer** starts second
6. Both services connect to Kafka at `localhost:9092`

## Viewing Logs

When you run the services, VS Code will show:
- **Terminal tabs** for each service
- **Debug Console** with application logs
- **Producer logs** showing published events
- **Consumer logs** showing received events

## Stopping the Services

- Click the **red stop button** (■) in the debug toolbar
- Or press `Shift+F5`
- This will stop both services if you used "Launch Both Services"

## Troubleshooting

### "Cannot find main class"
**Solution:** Run the "Build All" task first (`Ctrl+Shift+P` → "Run Task" → "Build All")

### "Port 8081 or 8082 already in use"
**Solution:** Stop any running instances or change the port in `application.yml`

### "Connection refused to Kafka"
**Solution:** Make sure Kafka and Zookeeper are running before starting the services

## File Structure

```
.vscode/
├── launch.json    # Debug/Run configurations
├── tasks.json     # Build and run tasks
└── settings.json  # Java/Maven settings
```

## Tips

- **Hot Reload**: Code changes will auto-reload in debug mode
- **Breakpoints**: Click left of line numbers to add breakpoints
- **Debug Variables**: Hover over variables to see values
- **Multiple Instances**: You can run multiple instances by clicking "Launch Producer" or "Launch Consumer" multiple times

Enjoy your one-click Kafka microservices! 🚀
