#!/bin/bash

# Usage: ./test.sh [dev|staging|prod]
ENV=${1:-dev}
CONFIG_FILE="./config/.env.$ENV"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "Error: Configuration file $CONFIG_FILE not found!"
  exit 1
fi

# Load environment variables
set -o allexport
source "$CONFIG_FILE"
set +o allexport

# Export SPRING_PROFILES_ACTIVE for Spring Boot
export SPRING_PROFILES_ACTIVE=$ENV

# Run Maven tests with profile
mvn clean verify -Dspring.profiles.active=$ENV