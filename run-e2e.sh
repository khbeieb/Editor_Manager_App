#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
REPORT_DIR="./e2e-tests/target/site/allure-maven-plugin"

echo "🧪 Running Playwright E2E tests for environment: $ENV"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "❌ Config file $CONFIG_FILE not found!"
  exit 1
fi

if [[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]]; then
  echo "❌ Override file $DOCKER_COMPOSE_OVERRIDE not found!"
  exit 1
fi

docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  run --rm e2e-tests sh -c "export PATH=/opt/maven/bin:\$PATH && cd /e2e-tests && mvn clean test -Dmaven.test.failure.ignore=true io.qameta.allure:allure-maven:report"

# Serve report if generated
if [[ -d "$REPORT_DIR" ]]; then
  echo "📄 Allure report generated in $REPORT_DIR"

  PORT=8082
  echo "🚀 Serving Allure report at http://localhost:$PORT"

  # Use Python's simple server if available
  if command -v python3 &> /dev/null; then
    (cd "$REPORT_DIR" && python3 -m http.server $PORT &)   # run in background
  elif command -v python &> /dev/null; then
    (cd "$REPORT_DIR" && python -m SimpleHTTPServer $PORT &)  # run in background
  else
    echo "⚠️ Could not find Python. Please open $REPORT_DIR/index.html manually in IDE."
    exit 0
  fi

  # Give the server a second to start
  sleep 2

  # Open browser automatically
  if command -v open &> /dev/null; then
    open "http://localhost:$PORT"
  elif command -v xdg-open &> /dev/null; then
    xdg-open "http://localhost:$PORT"
  else
    echo "🔗 Please open http://localhost:$PORT manually in your browser."
  fi
else
  echo "❌ Allure report not found in $REPORT_DIR"
fi
