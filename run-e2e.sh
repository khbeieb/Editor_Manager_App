#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./e2e-tests/target/allure-results"
PORT=8083

echo "🧪 Running Playwright E2E Cucumber tests for environment: $ENV"

# Validate required files
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

# Clean old Allure results
if [[ -d "$ALLURE_RESULTS_DIR" ]]; then
  echo "🧹 Removing old Allure results"
  rm -rf "$ALLURE_RESULTS_DIR"
fi
mkdir -p "$ALLURE_RESULTS_DIR"

# ---------------------------
# Step 1: Run E2E Playwright Cucumber tests
# ---------------------------
echo "🔄 Running Playwright E2E tests..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm e2e-tests sh -c "
    cd /e2e-tests && \
    mvn clean test \
      -Dmaven.test.failure.ignore=true \
      -Dallure.results.directory=target/allure-results \
      -Dtest='com.project.ui.*Test,CucumberE2ERunnerTest' \
      -Dcucumber.features=src/test/resources/features
  "

# ---------------------------
# Step 2: Generate Allure report
# ---------------------------
echo "📄 Generating Allure report..."
docker run --rm -v "$(pwd)/$ALLURE_RESULTS_DIR:/app/allure-results" \
  -v "$(pwd)/e2e-tests/target/allure-report:/app/allure-report" \
  frankescobar/allure-docker-service:latest generate

# ---------------------------
# Step 3: Start Allure Docker service
# ---------------------------
echo "🚀 Starting Allure Docker Service..."
docker run --rm -d \
  --name allure-server-e2e \
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

echo "💡 Allure server will keep running until you stop it: docker stop allure-server-e2e"
