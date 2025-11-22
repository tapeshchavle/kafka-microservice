@echo off
echo ========================================
echo Stopping Kafka Microservices Stack
echo ========================================
echo.

echo [1/2] Stopping Docker containers...
docker-compose down

echo.
echo [2/2] Stopping Spring Boot services...
echo Please close the "Kafka Consumer" and "Kafka Producer" windows manually.
echo Or use Task Manager to kill Java processes.
echo.

echo ========================================
echo Cleanup complete!
echo ========================================
pause
