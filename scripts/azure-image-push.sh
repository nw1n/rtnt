#!/usr/bin/env bash
set -euo pipefail

: "${ACR:?Set ACR (Azure Container Registry name)}"
IMAGE="${IMAGE:-rtnt-server}"
TAG="${TAG:-v1}"
PLATFORM="${PLATFORM:-linux/amd64}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TRUSTSTORE_PATH="$REPO_ROOT/rtnt-server/client.truststore.jks"

if [[ -n "${RTNT_KAFKA_BOOTSTRAP_SERVERS:-}" ]] && [[ ! -f "$TRUSTSTORE_PATH" ]]; then
  echo "RTNT_KAFKA_BOOTSTRAP_SERVERS is set but missing truststore: $TRUSTSTORE_PATH" >&2
  echo "Place client.truststore.jks at rtnt-server/client.truststore.jks before building the image." >&2
  exit 1
fi

echo "Logging in to ACR: $ACR"
az acr login -n "$ACR"

echo "Building and pushing image: $ACR.azurecr.io/$IMAGE:$TAG"
docker buildx build \
  --platform "$PLATFORM" \
  -t "$ACR.azurecr.io/$IMAGE:$TAG" \
  --push \
  "$REPO_ROOT"

echo "Image pushed successfully."
