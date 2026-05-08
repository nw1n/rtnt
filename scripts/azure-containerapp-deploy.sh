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

SECRETS=(
  "mongodb-uri=$MONGODB_URI"
)
ENV_VARS=(
  "SERVER_PORT=8080"
  "MONGODB_URI=secretref:mongodb-uri"
)

if [[ -n "${RTNT_KAFKA_BOOTSTRAP_SERVERS:-}" ]]; then
  : "${RTNT_KAFKA_SASL_PASSWORD:?Set RTNT_KAFKA_SASL_PASSWORD when RTNT_KAFKA_BOOTSTRAP_SERVERS is set}"
  : "${RTNT_KAFKA_SSL_TRUSTSTORE_PASSWORD:?Set RTNT_KAFKA_SSL_TRUSTSTORE_PASSWORD when RTNT_KAFKA_BOOTSTRAP_SERVERS is set}"
  SECRETS+=(
    "kafka-sasl-password=$RTNT_KAFKA_SASL_PASSWORD"
    "kafka-truststore-password=$RTNT_KAFKA_SSL_TRUSTSTORE_PASSWORD"
  )
  ENV_VARS+=(
    "RTNT_KAFKA_BOOTSTRAP_SERVERS=${RTNT_KAFKA_BOOTSTRAP_SERVERS}"
    "RTNT_KAFKA_SECURITY_PROTOCOL=${RTNT_KAFKA_SECURITY_PROTOCOL:-SASL_SSL}"
    "RTNT_KAFKA_SASL_MECHANISM=${RTNT_KAFKA_SASL_MECHANISM:-SCRAM-SHA-256}"
    "RTNT_KAFKA_SASL_USERNAME=${RTNT_KAFKA_SASL_USERNAME:-}"
    "RTNT_KAFKA_SASL_PASSWORD=secretref:kafka-sasl-password"
    "RTNT_KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=${RTNT_KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM:-}"
    "RTNT_KAFKA_SSL_TRUSTSTORE_TYPE=${RTNT_KAFKA_SSL_TRUSTSTORE_TYPE:-PKCS12}"
    "RTNT_KAFKA_SSL_TRUSTSTORE_LOCATION=${RTNT_KAFKA_SSL_TRUSTSTORE_LOCATION:-/app/client.truststore.jks}"
    "RTNT_KAFKA_SSL_TRUSTSTORE_PASSWORD=secretref:kafka-truststore-password"
  )
fi

if az containerapp show -g "$RG" -n "$APP" >/dev/null 2>&1; then
  echo "Container app exists. Updating image to: $IMAGE_REF"
  az containerapp registry set \
    -g "$RG" \
    -n "$APP" \
    --server "$ACR.azurecr.io" \
    --username "$ACR_USER" \
    --password "$ACR_PASS"
  az containerapp ingress update \
    -g "$RG" \
    -n "$APP" \
    --target-port 8080 \
    --type external
  az containerapp update \
    -g "$RG" \
    -n "$APP" \
    --image "$IMAGE_REF"
  echo "Note: existing apps get registry auth, ingress target port (8080), and image updates here."
  echo "To change env vars or app secrets, use the Azure Portal or:"
  echo "  az containerapp update -g \"$RG\" -n \"$APP\" --replace-secrets ... --set-env-vars ..."
  echo "See: https://learn.microsoft.com/en-us/cli/azure/containerapp"
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
    --secrets "${SECRETS[@]}" \
    --env-vars "${ENV_VARS[@]}"
fi

FQDN="$(az containerapp show -g "$RG" -n "$APP" --query properties.configuration.ingress.fqdn -o tsv)"
echo "App URL: https://$FQDN"
