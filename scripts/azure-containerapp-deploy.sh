#!/usr/bin/env bash
set -euo pipefail

: "${RG:?Set RG (resource group)}"
: "${ENV:?Set ENV (container app environment name)}"
: "${APP:?Set APP (container app name)}"
: "${ACR:?Set ACR (Azure Container Registry name)}"
: "${MONGODB_URI:?Set MONGODB_URI (Atlas connection string)}"

IMAGE="${IMAGE:-rtnt-server}"
TAG="${TAG:-v1}"

ACR_USER="${ACR_USER:-$(az acr credential show -n "$ACR" --query username -o tsv)}"
ACR_PASS="${ACR_PASS:-$(az acr credential show -n "$ACR" --query "passwords[0].value" -o tsv)}"

IMAGE_REF="$ACR.azurecr.io/$IMAGE:$TAG"

if az containerapp show -g "$RG" -n "$APP" >/dev/null 2>&1; then
  echo "Container app exists. Updating image to: $IMAGE_REF"
  az containerapp update \
    -g "$RG" \
    -n "$APP" \
    --image "$IMAGE_REF"
else
  echo "Container app does not exist. Creating: $APP"
  az containerapp create \
    -g "$RG" \
    -n "$APP" \
    --environment "$ENV" \
    --image "$IMAGE_REF" \
    --registry-server "$ACR.azurecr.io" \
    --registry-username "$ACR_USER" \
    --registry-password "$ACR_PASS" \
    --target-port 8080 \
    --ingress external \
    --cpu 0.5 \
    --memory 1Gi \
    --min-replicas 0 \
    --max-replicas 1 \
    --secrets mongodb-uri="$MONGODB_URI" \
    --env-vars SERVER_PORT=8080 MONGODB_URI=secretref:mongodb-uri
fi

FQDN="$(az containerapp show -g "$RG" -n "$APP" --query properties.configuration.ingress.fqdn -o tsv)"
echo "App URL: https://$FQDN"
