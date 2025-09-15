#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./api-tests/target/allure-results"
PORT=8083

echo "🧪 Running Playwright API tests for environment: $ENV"

# Validate files
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

# Clean old results
[[ -d "$ALLURE_RESULTS_DIR" ]] && echo "🧹 Removing old Allure results" && rm -rf "$ALLURE_RESULTS_DIR"

# Run API tests and generate Allure results
echo "🔄 Running API tests..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm api-tests sh -c "cd /api-tests && mvn clean test -Dmaven.test.failure.ignore=true io.qameta.allure:allure-maven:report"

# Check if results were generated
if [[ -d "$ALLURE_RESULTS_DIR" ]] && [[ -n "$(ls -A "$ALLURE_RESULTS_DIR" 2>/dev/null)" ]]; then
  echo "📊 Allure results generated at $ALLURE_RESULTS_DIR"

  # Stop any existing allure server
  if docker ps | grep -q "allure-server-api"; then
    echo "🛑 Stopping existing Allure server for API tests..."
    docker stop allure-server-api >/dev/null 2>&1
  fi

  echo "🚀 Starting Allure Docker Service for API tests..."

  docker run --rm -d \
    --name allure-server-api \
    -p $PORT:4040 \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    frankescobar/allure-docker-service:latest

  echo "⏳ Waiting for Allure service to start..."
  sleep 5

  echo "📄 Generating Allure report..."
  curl -X GET "http://localhost:$PORT" -H "Content-Type: application/json" >/dev/null 2>&1
  sleep 2

  echo "✅ Allure report ready!"
  echo "📊 View API test report at: http://localhost:$PORT"

  if command -v open &> /dev/null; then
    open "http://localhost:$PORT"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT"
  else
    echo "🔗 Open http://localhost:$PORT in your browser."
  fi

  echo "💡 The Allure server will keep running until you stop it with: docker stop allure-server-api"

else
  echo "❌ No Allure results found in $ALLURE_RESULTS_DIR"
  echo "💡 Make sure your API tests are configured to generate Allure results"
fi
