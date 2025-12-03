#!/bin/bash
set -e

# Usage: ./run-e2e.sh [ENV] [BROWSER] [RUN_MODE]
#   ENV: dev (default) or prod
#   BROWSER: chromium, firefox, webkit, or all (default: all)
#   RUN_MODE: local or docker (default: docker)
# Examples:
#   ./run-e2e.sh dev chromium local    # Run locally with chromium
#   ./run-e2e.sh dev all docker        # Run in Docker with all browsers
#   ./run-e2e.sh dev all               # Run in Docker (default)

ENV=${1:-dev}
BROWSER=${2:-all}
RUN_MODE=${3:-docker}  # 'local' or 'docker' (default: docker)

CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./e2e-tests/target/allure-results"
ALLURE_REPORT_DIR="./e2e-tests/target/allure-report"
PORT=8083

# Base URLs based on run mode
if [[ "$RUN_MODE" == "local" ]]; then
  E2E_BASE_URL_UI="${E2E_BASE_URL_UI:-http://localhost:4200}"
  E2E_BASE_URL_API="${E2E_BASE_URL_API:-http://localhost:8080}"
  echo "🏠 Running in LOCAL mode"
else
  E2E_BASE_URL_UI="${E2E_BASE_URL_UI:-http://frontend:4200}"
  E2E_BASE_URL_API="${E2E_BASE_URL_API:-http://backend:8080}"
  echo "🐳 Running in DOCKER mode"
fi

echo "🧪 Running Playwright E2E Cucumber tests for environment: $ENV | Browser: $BROWSER | Mode: $RUN_MODE"
echo "📍 UI Base URL: $E2E_BASE_URL_UI"
echo "📍 API Base URL: $E2E_BASE_URL_API"

# ---------------------------
# Step 0: Validate files and prerequisites
# ---------------------------
if [[ "$RUN_MODE" == "docker" ]]; then
  [[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
  [[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1
elif [[ "$RUN_MODE" == "local" ]]; then
  # Check if Maven is available
  if ! command -v mvn &> /dev/null; then
    echo "❌ Maven (mvn) is not installed or not in PATH. Required for local mode."
    exit 1
  fi
  # Check if e2e-tests directory exists
  [[ ! -d "e2e-tests" ]] && echo "❌ e2e-tests directory not found!" && exit 1
  # Optional: Check if services are accessible (warn if not)
  if ! curl -s --connect-timeout 2 "$E2E_BASE_URL_UI" > /dev/null 2>&1; then
    echo "⚠️  Warning: Cannot reach UI at $E2E_BASE_URL_UI. Make sure services are running."
  fi
  if ! curl -s --connect-timeout 2 "$E2E_BASE_URL_API" > /dev/null 2>&1; then
    echo "⚠️  Warning: Cannot reach API at $E2E_BASE_URL_API. Make sure services are running."
  fi
else
  echo "❌ Invalid RUN_MODE: $RUN_MODE. Must be 'local' or 'docker'"
  exit 1
fi

# ---------------------------
# Step 1: Clean old results
# ---------------------------
echo "🧹 Cleaning previous results..."
rm -rf "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR"
mkdir -p "$ALLURE_RESULTS_DIR" "$ALLURE_REPORT_DIR"

# ---------------------------
# Step 2: Define function for test execution
# ---------------------------
run_playwright_tests() {
  local browser=$1
  echo "🔄 Running Playwright E2E tests on browser: $browser..."

  if [[ "$RUN_MODE" == "local" ]]; then
    # Run tests locally
    (cd e2e-tests && \
    export E2E_BASE_URL_UI="$E2E_BASE_URL_UI" \
    export E2E_BASE_URL_API="$E2E_BASE_URL_API" && \
    mvn clean test \
      -Dmaven.test.failure.ignore=true \
      -Dallure.results.directory=target/allure-results/${browser} \
      -Dtest='com.project.ui.*Test,CucumberE2ERunnerTest' \
      -Dbrowser=${browser} \
      -Dcucumber.features=src/test/resources/features \
      -De2e.base.url.ui="$E2E_BASE_URL_UI" \
      -De2e.base.url.api="$E2E_BASE_URL_API") \
    && echo "✅ ${browser} tests passed" || echo "❌ ${browser} tests failed"
  else
    # Run tests in Docker
    docker compose \
      --env-file "$CONFIG_FILE" \
      --file docker-compose.yml \
      --file "$DOCKER_COMPOSE_OVERRIDE" \
      run --rm -T \
      -e E2E_BASE_URL_UI="$E2E_BASE_URL_UI" \
      -e E2E_BASE_URL_API="$E2E_BASE_URL_API" \
      e2e-tests sh -c "
        cd /e2e-tests && \
        export E2E_BASE_URL_UI='$E2E_BASE_URL_UI' && \
        export E2E_BASE_URL_API='$E2E_BASE_URL_API' && \
        mvn clean test \
          -Dmaven.test.failure.ignore=true \
          -Dallure.results.directory=target/allure-results/${browser} \
          -Dtest='com.project.ui.*Test,CucumberE2ERunnerTest' \
          -Dbrowser=${browser} \
          -Dcucumber.features=src/test/resources/features \
          -De2e.base.url.ui='$E2E_BASE_URL_UI' \
          -De2e.base.url.api='$E2E_BASE_URL_API'
      " && echo "✅ ${browser} tests passed" || echo "❌ ${browser} tests failed"
  fi
}

# ---------------------------
# Step 3: Run tests (single or parallel)
# ---------------------------
if [[ "$BROWSER" == "all" ]]; then
  echo "🌐 Running tests in parallel on Chromium, Firefox, and WebKit..."
  run_playwright_tests "chromium" &
  pid_chromium=$!
  run_playwright_tests "firefox" &
  pid_firefox=$!
  run_playwright_tests "webkit" &
  pid_webkit=$!

  wait $pid_chromium || chromium_failed=true
  wait $pid_firefox || firefox_failed=true
  wait $pid_webkit || webkit_failed=true
else
  run_playwright_tests "$BROWSER"
fi

# ---------------------------
# Step 4: Stop existing Allure server if running
# ---------------------------
if docker ps | grep -q "allure-server-e2e"; then
  echo "🛑 Stopping existing Allure server..."
  docker stop allure-server-e2e >/dev/null 2>&1
fi

# ---------------------------
# Step 5: Merge Allure results from all browsers
# ---------------------------
echo "📂 Merging Allure results from all browsers..."
mkdir -p "$ALLURE_RESULTS_DIR/_merged"
find "$ALLURE_RESULTS_DIR" -mindepth 2 -type f -exec cp {} "$ALLURE_RESULTS_DIR/_merged" \; || true

# ---------------------------
# Step 6: Generate Allure report
# ---------------------------
echo "📄 Generating Allure report..."
docker run --rm \
  -v "$(pwd)/$ALLURE_RESULTS_DIR/_merged:/app/allure-results" \
  -v "$(pwd)/$ALLURE_REPORT_DIR:/app/allure-report" \
  frankescobar/allure-docker-service:latest \
  allure generate /app/allure-results -o /app/allure-report --clean

# ---------------------------
# Step 7: Start Allure Docker service
# ---------------------------
echo "🚀 Starting Allure Docker Service..."
docker run --rm -d \
  --name allure-server-e2e \
  -p $PORT:4040 \
  -v "$(pwd)/$ALLURE_RESULTS_DIR/_merged:/app/allure-results" \
  frankescobar/allure-docker-service:latest

echo "⏳ Waiting for Allure service to start..."
sleep 5
echo "📄 Allure report ready at: http://localhost:$PORT"
echo "💡 Stop the Allure server with: docker stop allure-server-e2e"

# ---------------------------
# Step 8: Summary of results
# ---------------------------
echo ""
echo "🧭 TEST SUMMARY:"
[[ "$BROWSER" == "all" ]] && {
  [[ "$chromium_failed" == true ]] && echo "❌ Chromium tests failed" || echo "✅ Chromium passed"
  [[ "$firefox_failed" == true ]] && echo "❌ Firefox tests failed" || echo "✅ Firefox passed"
  [[ "$webkit_failed" == true ]] && echo "❌ WebKit tests failed" || echo "✅ WebKit passed"
}

# ---------------------------
# Step 9: Open report in browser
# ---------------------------
if command -v open &> /dev/null; then
  open "http://localhost:$PORT"
elif command -v xdg-open &> /dev/null; then
  xdg-open "http://localhost:$PORT"
fi
