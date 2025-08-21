#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./e2e-tests/target/allure-results"
PORT=8082

echo "🧪 Running Playwright E2E tests for environment: $ENV"

# Validate files
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

# Clean old results
[[ -d "$ALLURE_RESULTS_DIR" ]] && echo "🧹 Removing old Allure results" && rm -rf "$ALLURE_RESULTS_DIR"

# Run tests and generate Allure results (not reports)
echo "🔄 Running E2E tests..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm e2e-tests sh -c "cd /e2e-tests && mvn clean test -Dmaven.test.failure.ignore=true io.qameta.allure:allure-maven:report"

# Check if results were generated
if [[ -d "$ALLURE_RESULTS_DIR" ]] && [[ -n "$(ls -A "$ALLURE_RESULTS_DIR" 2>/dev/null)" ]]; then
  echo "📊 Allure results generated at $ALLURE_RESULTS_DIR"

  # Stop any existing allure server
  if docker ps | grep -q "allure-server"; then
    echo "🛑 Stopping existing Allure server..."
    docker stop allure-server >/dev/null 2>&1
  fi

  echo "🚀 Starting Allure Docker Service..."

  # Start the Allure Docker service
  docker run --rm -d \
    --name allure-server \
    -p $PORT:4040 \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    frankescobar/allure-docker-service:latest

  # Wait for service to start
  echo "⏳ Waiting for Allure service to start..."
  sleep 5

  # Generate the report
  echo "📄 Generating Allure report..."
  curl -X GET "http://localhost:$PORT" \
    -H "Content-Type: application/json" >/dev/null 2>&1

  sleep 2

  echo "✅ Allure report ready!"
  echo "📊 View report at: http://localhost:$PORT"

  # Open browser
  if command -v open &> /dev/null; then
    open "http://localhost:$PORT"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT"
  else
    echo "🔗 Open http://localhost:$PORT."
  fi

  echo "💡 The server will keep running until you stop it with: docker stop allure-server"

else
  echo "❌ No Allure results found in $ALLURE_RESULTS_DIR"
  echo "💡 Make sure your tests are configured to generate Allure results"
fi
