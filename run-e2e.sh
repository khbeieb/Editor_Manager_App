#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"
REPORT_PATH="./e2e-tests/target/site/allure-maven-plugin/index.html"

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
  run --rm e2e-tests sh -c "export PATH=/opt/maven/bin:\$PATH && cd /e2e-tests && mvn clean test io.qameta.allure:allure-maven:report"

# Open report if exists
if [[ -f "$REPORT_PATH" ]]; then
  echo "📄 Allure report generated at $REPORT_PATH"
  if command -v xdg-open &> /dev/null; then
    xdg-open "$REPORT_PATH"
  elif command -v open &> /dev/null; then
    open "$REPORT_PATH"
  else
    echo "🔗 Please open the report manually in your browser."
  fi
else
  echo "❌ Allure report not found at $REPORT_PATH"
fi
