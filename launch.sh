#!/bin/bash

# Usage: ./launch.sh [dev|staging|prod]

ENV=${1:-dev}  # Default to dev
CONFIG_FILE="./config/.env.$ENV"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "❌ Error: Configuration file $CONFIG_FILE not found!"
  exit 1
fi

# Load environment variables
set -o allexport
source "$CONFIG_FILE"
set +o allexport

# Export ENV so Docker Compose picks it up
export ENV=$ENV

echo "🛑 Stopping any running containers..."
docker compose down

echo "🚀 Starting Docker containers for $ENV environment..."
docker compose --env-file "$CONFIG_FILE" up --build -d

echo "📜 Streaming logs..."
docker compose logs -f