#!/bin/bash

# Usage: ./launch.sh [dev|staging|prod]
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

# Export for Spring Boot
export SPRING_PROFILES_ACTIVE=$ENV

mvn clean package -DskipTests

# Find the jar file
JAR_FILE=$(ls -t ./target/*.jar 2>/dev/null | head -n1)

if [[ -z "$JAR_FILE" || ! -f "$JAR_FILE" ]]; then
  echo "Error: No JAR file found in ./target/"
  exit 1
fi

echo "Running Spring Boot app from $JAR_FILE with profile '$ENV'..."

# Kill previous process (optional)
PID=$(pgrep -f "$JAR_FILE")
if [[ -n "$PID" ]]; then
  echo "Killing existing process with PID $PID"
  kill "$PID"
fi

# Run the app
java -jar "$JAR_FILE" --spring.profiles.active=$ENV

echo "EditorManager App started."