#!/bin/bash

ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"

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
  run --rm e2e-tests
