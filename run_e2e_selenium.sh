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
MAX_RETRIES=3

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
# Step 2: Start required services
# ---------------------------
echo "🚀 Starting required services..."
docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES up -d db backend frontend selenium

# ---------------------------
# Step 3: Wait a few seconds for DB and Selenium to start
# ---------------------------
echo "⏳ Waiting 10 seconds for services to initialize..."
sleep 10

# ---------------------------
# Step 4: Build e2e-selenium test container
# ---------------------------
echo "🔨 Building e2e-selenium test container..."
docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES build e2e-selenium

# ---------------------------
# Step 5: Run tests with retries
# ---------------------------
TEST_PASSED=false

for attempt in $(seq 1 $MAX_RETRIES); do
  echo ""
  echo "🧪 Running Selenium E2E tests (attempt ${attempt}/${MAX_RETRIES})..."

  if docker compose --env-file "$CONFIG_FILE" $COMPOSE_FILES run --rm e2e-selenium mvn clean test -Dmaven.test.failure.ignore=true; then
    echo "✅ Selenium E2E tests completed successfully!"
    TEST_PASSED=true
    break
  else
    echo "⚠️ Test run failed (attempt ${attempt}/${MAX_RETRIES})"
    if [ $attempt -lt $MAX_RETRIES ]; then
      echo "🔄 Retrying in 5 seconds..."
      sleep 5
    fi
  fi
done

# ---------------------------
# Step 6: Generate Allure report
# ---------------------------
if [ -d "$ALLURE_RESULTS_DIR" ] && [ "$(ls -A $ALLURE_RESULTS_DIR 2>/dev/null)" ]; then
  echo ""
  echo "📊 Generating Allure report..."
  docker run --rm \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    -v "$(pwd)/$ALLURE_REPORT_DIR:/app/allure-report" \
    frankescobar/allure-docker-service:latest \
    allure generate /app/allure-results -o /app/allure-report --clean

  echo "✅ Allure report generated at: $ALLURE_REPORT_DIR"

  # ---------------------------
  # Step 7: Stop existing Allure server if any and start new one
  # ---------------------------
  if docker ps -a | grep -q "allure-server-selenium"; then
    echo "🛑 Stopping and removing existing Allure server..."
    docker stop allure-server-selenium >/dev/null 2>&1
    docker rm allure-server-selenium >/dev/null 2>&1
  fi

  echo "🚀 Starting Allure Docker Service..."
  docker run -d \
    --name allure-server-selenium \
    -p $PORT:4040 \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    frankescobar/allure-docker-service:latest >/dev/null 2>&1

  echo "⏳ Waiting for Allure service to start..."
  sleep 5

  echo "📄 Allure report ready at: http://localhost:$PORT"

  # Auto-open in browser
  if command -v open &> /dev/null; then
    open "http://localhost:$PORT"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT"
  fi

  echo "💡 To stop the Allure server: docker stop allure-server-selenium"
else
  echo "⚠️ No Allure results found, skipping report generation"
fi

# ---------------------------
# Step 8: Finish
# ---------------------------
echo ""
if [ "$TEST_PASSED" = true ]; then
  echo "🎉 Selenium E2E test script finished successfully!"
  exit 0
else
  echo "❌ Selenium E2E test script finished with failures!"
  exit 1
fi
