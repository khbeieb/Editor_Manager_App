#!/bin/bash
set -e

ENV=${1:-dev}
BROWSER=${2:-all}

CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
ALLURE_RESULTS_DIR="./e2e-tests/target/allure-results"
ALLURE_REPORT_DIR="./e2e-tests/target/allure-report"
PORT=8083

echo "🧪 Running Playwright E2E Cucumber tests for environment: $ENV | Browser: $BROWSER"

# ---------------------------
# Step 0: Validate files
# ---------------------------
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

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

  docker compose \
    --env-file "$CONFIG_FILE" \
    --file docker-compose.yml \
    --file "$DOCKER_COMPOSE_OVERRIDE" \
    run --rm -T e2e-tests sh -c "
      cd /e2e-tests && \
      mvn clean test \
        -Dmaven.test.failure.ignore=true \
        -Dallure.results.directory=target/allure-results/${browser} \
        -Dtest='com.project.ui.*Test,CucumberE2ERunnerTest' \
        -Dbrowser=${browser} \
        -Dcucumber.features=src/test/resources/features
    " && echo "✅ ${browser} tests passed" || echo "❌ ${browser} tests failed"
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
