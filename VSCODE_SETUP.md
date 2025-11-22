# VS Code Setup Summary

## ✅ What I Created

### 1. Launch Configurations (`.vscode/launch.json`)
- **Launch Producer** - Run producer service with one click
- **Launch Consumer** - Run consumer service with one click  
- **Launch Both Services** - Run both services together (compound configuration)

### 2. Tasks (`.vscode/tasks.json`)
- **Build All** - Build both projects
- **Build Producer** - Build producer only
- **Build Consumer** - Build consumer only
- **Run Producer** - Run producer via Maven
- **Run Consumer** - Run consumer via Maven

### 3. Settings (`.vscode/settings.json`)
- Java configuration for automatic build updates
- Maven terminal settings
- File exclusions for cleaner workspace

### 4. Extensions (`.vscode/extensions.json`)
- Recommended extensions list
- VS Code will prompt you to install these when you open the project

## 🎯 How to Use

### First Time Setup:

1. **Open the project in VS Code**
   ```bash
   cd c:\fastapi
   code .
   ```

2. **Install recommended extensions**
   - VS Code will show a notification: "This workspace has extension recommendations"
   - Click **"Install All"**
   - Wait for extensions to install

3. **Start Kafka** (required before running services)
   - See README.md Step 1 for Kafka startup instructions

### Running the Services:

**Method 1: Debug Panel (Easiest)**
1. Click the **Run and Debug** icon (▶️ with bug) in the left sidebar
2. Select **"Launch Both Services"** from the dropdown
3. Press `F5` or click the green play button ▶️

**Method 2: Command Palette**
1. Press `Ctrl+Shift+P`
2. Type "Debug: Select and Start Debugging"
3. Choose **"Launch Both Services"**

**Method 3: Tasks**
1. Press `Ctrl+Shift+P`
2. Type "Tasks: Run Task"
3. Select "Run Producer" or "Run Consumer"

## 📊 What You'll See

```
┌─────────────────────────────────────────┐
│  VS Code Debug Panel                    │
├─────────────────────────────────────────┤
│  ▶️ Launch Both Services               │
│  ▶️ Launch Producer                    │
│  ▶️ Launch Consumer                    │
└─────────────────────────────────────────┘
           ↓ (Press F5)
┌─────────────────────────────────────────┐
│  Terminal: Consumer                     │
│  Started KafkaConsumerApplication...    │
├─────────────────────────────────────────┤
│  Terminal: Producer                     │
│  Started KafkaProducerApplication...    │
└─────────────────────────────────────────┘
```

## 🔧 Troubleshooting

### Extensions Not Working
- Make sure you installed **Extension Pack for Java**
- Reload VS Code: `Ctrl+Shift+P` → "Reload Window"

### Launch.json Shows Errors
- This is normal until Java extensions are installed
- Errors will disappear after installing recommended extensions

### Services Won't Start
- Build first: `Ctrl+Shift+P` → "Tasks: Run Task" → "Build All"
- Check that Kafka is running
- Verify Java 17 is installed: `java -version`

## 📁 Files Created

```
.vscode/
├── launch.json       # Run/Debug configurations
├── tasks.json        # Build and run tasks
├── settings.json     # Java/Maven settings
└── extensions.json   # Recommended extensions
```

## 🎉 Benefits

✅ **One-Click Launch** - No need to open multiple terminals  
✅ **Integrated Debugging** - Set breakpoints and debug easily  
✅ **Hot Reload** - Code changes reload automatically  
✅ **Organized Terminals** - Each service in its own terminal tab  
✅ **Easy Stopping** - Stop all services with one click  

Enjoy your streamlined development experience! 🚀
