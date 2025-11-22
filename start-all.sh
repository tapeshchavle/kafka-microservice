#!/bin/bash

echo "========================================"
echo "Starting Kafka Microservices Stack"
echo "========================================"
echo ""

echo "[1/3] Starting Kafka and Zookeeper with Docker..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "ERROR: Docker Compose failed. Make sure Docker is running."
    exit 1
fi

echo ""
echo "[2/3] Waiting for Kafka to be ready (10 seconds)..."
sleep 10

echo ""
echo "[3/3] Starting microservices..."
echo ""

echo "Starting Consumer Service (port 8082)..."
cd kafka-consumer
gnome-terminal --title="Kafka Consumer" -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -T "Kafka Consumer" -e "mvn spring-boot:run; bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && mvn spring-boot:run"' 2>/dev/null &

cd ..

echo "Waiting 5 seconds before starting producer..."
sleep 5

echo "Starting Producer Service (port 8081)..."
cd kafka-producer
gnome-terminal --title="Kafka Producer" -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -T "Kafka Producer" -e "mvn spring-boot:run; bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)' && mvn spring-boot:run"' 2>/dev/null &

cd ..

echo ""
echo "========================================"
echo "All services are starting!"
echo "========================================"
echo ""
echo "Kafka:    http://localhost:9092"
echo "Producer: http://localhost:8081"
echo "Consumer: http://localhost:8082"
echo ""
echo "Wait 30-60 seconds for services to fully start."
echo ""
echo "To test, run:"
echo 'curl -X POST http://localhost:8081/api/events/publish -H "Content-Type: application/json" -d '"'"'{"userId":"test123","eventType":"CREATED","metadata":"Test"}'"'"''
echo ""
