#!/bin/bash
set -e

# ---------------------------
# Step 0: Handle environment argument
# ---------------------------
ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="./docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./e2e-selenium/target/allure-results"
ALLURE_REPORT_DIR="./e2e-selenium/target/allure-report"
PORT=8084

echo "🧪 Running Selenium E2E tests for environment: $ENV"

# Validate environment file
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1

# Load environment variables
export $(grep -v '^#' "$CONFIG_FILE" | xargs)

# Determine which Compose files to use
if [[ -f "$DOCKER_COMPOSE_OVERRIDE" ]]; then
  COMPOSE_FILES="-f docker-compose.yml -f $DOCKER_COMPOSE_OVERRIDE"
else
  COMPOSE_FILES="-f docker-compose.yml"
fi

# ---------------------------
# Step 1: Clean previous Allure data
# ---------------------------
echo "🧹 Cleaning old Allure results..."
rm -rf "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR"
mkdir -p "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR"

# ---------------------------
# Step 2: Start backend, frontend, db if not running
# ---------------------------
echo "🚀 Starting environment services if not already running..."

services=("backend" "frontend" "db")
for service in "${services[@]}"; do
  if ! docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES ps -q "$service" >/dev/null 2>&1; then
    echo "   🔹 Starting $service..."
    docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES up -d "$service"
  else
    echo "   ✅ $service already running"
  fi
done

echo "✅ Backend, frontend, and DB are running!"

# ---------------------------
# Step 3: Run Selenium E2E tests
# ---------------------------
echo "🧪 Running Selenium E2E tests..."
docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES run --rm e2e-selenium \
  mvn clean test -Dmaven.test.failure.ignore=true

# ---------------------------
# Step 4: Generate Allure report
# ---------------------------
if [ -d "$ALLURE_RESULTS_DIR" ]; then
  echo "📊 Generating Allure report..."

  # Remove existing Allure container if it exists
  if docker ps -a --format '{{.Names}}' | grep -q "^allure-server-selenium$"; then
      echo "🛑 Removing existing Allure container..."
      docker rm -f allure-server-selenium
  fi

  docker run --rm \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    -v "$(pwd)/$ALLURE_REPORT_DIR:/app/allure-report" \
    frankescobar/allure-docker-service:latest \
    allure generate /app/allure-results -o /app/allure-report --clean

  echo "✅ Allure report generated at: $ALLURE_REPORT_DIR"
fi

# ---------------------------
# Step 5: Start Allure report viewer
# ---------------------------
docker run -d \
  --name allure-server-selenium \
  -p $PORT:4040 \
  -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
  frankescobar/allure-docker-service:latest

echo "📄 Allure report available at: http://localhost:$PORT"

# Open automatically if possible
if command -v open &> /dev/null; then
  open "http://localhost:$PORT"
elif command -v xdg-open &> /dev/null; then
  xdg-open "http://localhost:$PORT"
fi

echo "🎉 Selenium E2E tests completed successfully!"
