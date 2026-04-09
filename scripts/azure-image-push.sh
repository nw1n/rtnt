#!/usr/bin/env bash
set -euo pipefail

: "${ACR:?Set ACR (Azure Container Registry name)}"
IMAGE="${IMAGE:-rtnt-server}"
TAG="${TAG:-v1}"
PLATFORM="${PLATFORM:-linux/amd64}"

echo "Logging in to ACR: $ACR"
az acr login -n "$ACR"

echo "Building and pushing image: $ACR.azurecr.io/$IMAGE:$TAG"
docker buildx build \
  --platform "$PLATFORM" \
  -t "$ACR.azurecr.io/$IMAGE:$TAG" \
  --push \
  .

echo "Image pushed successfully."
