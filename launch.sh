#!/bin/bash

# Usage: ./launch.sh [dev|staging|prod]
ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"
DOCKER_COMPOSE_OVERRIDE="docker-compose.$ENV.yml"

echo "📦 Selected environment: $ENV"

# Check if environment config file exists
if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "❌ Error: Configuration file $CONFIG_FILE not found!"
  exit 1
fi

# Check if override Compose file exists
if [[ ! -f "$DOCKER_COMPOSE_OVERRIDE" ]]; then
  echo "❌ Error: $DOCKER_COMPOSE_OVERRIDE not found!"
  exit 1
fi

# Load env vars from the file
set -o allexport
source "$CONFIG_FILE"
set +o allexport

export ENV=$ENV

echo "🛑 Stopping any running containers..."
docker compose down

echo "🚀 Starting Docker containers for $ENV environment..."
docker compose \
  --env-file "$CONFIG_FILE" \
  --file docker-compose.yml \
  --file "$DOCKER_COMPOSE_OVERRIDE" \
  up --build -d

echo "📜 Streaming logs..."
docker compose logs -f
