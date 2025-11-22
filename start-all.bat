@echo off
echo ========================================
echo Starting Kafka Microservices Stack
echo ========================================
echo.

echo [1/3] Starting Kafka and Zookeeper with Docker...
docker-compose up -d

if %errorlevel% neq 0 (
    echo ERROR: Docker Compose failed. Make sure Docker Desktop is running.
    pause
    exit /b 1
)

echo.
echo [2/3] Waiting for Kafka to be ready (10 seconds)...
timeout /t 10 /nobreak > nul

echo.
echo [3/3] Starting microservices...
echo.

echo Starting Consumer Service (port 8082)...
start "Kafka Consumer" cmd /k "cd kafka-consumer && mvn spring-boot:run"

echo Waiting 5 seconds before starting producer...
timeout /t 5 /nobreak > nul

echo Starting Producer Service (port 8081)...
start "Kafka Producer" cmd /k "cd kafka-producer && mvn spring-boot:run"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Kafka:    http://localhost:9092
echo Producer: http://localhost:8081
echo Consumer: http://localhost:8082
echo.
echo Wait 30-60 seconds for services to fully start.
echo.
echo To test, run:
echo curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d "{\"userId\":\"test123\",\"eventType\":\"CREATED\",\"metadata\":\"Test\"}"
echo.
echo Press any key to exit this window (services will keep running)...
pause > nul
