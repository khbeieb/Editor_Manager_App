#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
REPORT_DIR="./e2e-tests/target/site/allure-maven-plugin"
PORT=8082

echo "🧪 Running Playwright E2E tests for environment: $ENV"

# Validate files
[[ ! -f "$CONFIG_FILE" ]] && echo "❌ $CONFIG_FILE not found!" && exit 1
[[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]] && echo "❌ $DOCKER_COMPOSE_OVERRIDE not found!" && exit 1

# Clean old report
[[ -d "$REPORT_DIR" ]] && echo "🧹 Removing old Allure report" && rm -rf "$REPORT_DIR"

# Run container: execute tests and generate Allure report
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm e2e-tests sh -c "cd /e2e-tests && mvn clean test -Dmaven.test.failure.ignore=true io.qameta.allure:allure-maven:report"

# Serve the report if generated
if [[ -d "$REPORT_DIR" ]]; then
  echo "📄 Allure report generated at $REPORT_DIR"

  # Kill existing server if any
  if lsof -i:$PORT -t &> /dev/null; then
      echo "🛑 Killing existing server on port $PORT"
      kill -9 $(lsof -i:$PORT -t)
  fi

  echo "🚀 Serving Allure report at http://localhost:$PORT"
  if command -v python3 &> /dev/null; then
    (cd "$REPORT_DIR" && python3 -m http.server $PORT &)
  elif command -v python &> /dev/null; then
    (cd "$REPORT_DIR" && python -m SimpleHTTPServer $PORT &)
  else
    echo "⚠️ Could not find Python. Open $REPORT_DIR/index.html manually."
    exit 0
  fi

  # Give server a moment
  sleep 2

  # Open browser with cache busting
  if command -v open &> /dev/null; then
    open "http://localhost:$PORT/index.html?ts=$(date +%s)"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT/index.html?ts=$(date +%s)"
  else
    echo "🔗 Open http://localhost:$PORT/index.html manually."
  fi
else
  echo "❌ Allure report not found in $REPORT_DIR"
fi
