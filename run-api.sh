#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./api-tests/target/allure-results"
ALLURE_CUCUMBER_DIR="$ALLURE_RESULTS_DIR/cucumber"
PORT=8083

echo "🧪 Running Playwright API + Cucumber tests for environment: $ENV"

# Validate files
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

# Clean old results
if [[ -d "$ALLURE_RESULTS_DIR" ]]; then
  echo "🧹 Removing old Allure results"
  rm -rf "$ALLURE_RESULTS_DIR"
fi

# Ensure directories exist
mkdir -p "$ALLURE_CUCUMBER_DIR"

# Run tests inside Docker
echo "🔄 Running all tests (API + Cucumber)..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm api-tests sh -c "
    cd /api-tests && mvn clean test -Dmaven.test.failure.ignore=true io.qameta.allure:allure-maven:report
  "

# Generate Allure report
echo "📄 Generating Allure report..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm api-tests sh -c "
    cd /api-tests &&
    mvn io.qameta.allure:allure-maven:report
  "

# Check if results were generated
if [[ -d "$ALLURE_RESULTS_DIR" ]] && [[ -n "$(ls -A "$ALLURE_RESULTS_DIR" 2>/dev/null)" ]]; then
  echo "📊 Allure results generated at $ALLURE_RESULTS_DIR"

  # Stop any existing Allure server
  if docker ps | grep -q "allure-server-api"; then
    echo "🛑 Stopping existing Allure server..."
    docker stop allure-server-api >/dev/null 2>&1
  fi

  # Start Allure Docker service
  echo "🚀 Starting Allure Docker Service..."
  docker run --rm -d \
    --name allure-server-api \
    -p $PORT:4040 \
    -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
    frankescobar/allure-docker-service:latest

  echo "⏳ Waiting for Allure service to start..."
  sleep 5

  echo "📄 Allure report ready at http://localhost:$PORT"
  if command -v open &> /dev/null; then
    open "http://localhost:$PORT"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT"
  fi

  echo "💡 The Allure server will keep running until you stop it with: docker stop allure-server-api"

else
  echo "❌ No Allure results found in $ALLURE_RESULTS_DIR"
  echo "💡 Make sure your API tests and Cucumber tests are configured to generate Allure results"
fi
