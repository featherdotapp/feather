#!/bin/bash

# Exit on error
set -e

# Build the Docker image (forces rebuild, tags as latest)
docker-compose build

# Stop and remove any running containers for a clean start
docker-compose down

# Start the container in detached mode
docker-compose up -d

echo "Docker image built and container started."

