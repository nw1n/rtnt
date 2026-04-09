# Deploy (Azure)

## Build Container Image and Push to ACR

```
source .env.local
source .env.secrets.local
./scripts/azure-image-push.sh
```

## Deploy to Azure

```
source .env.local
source .env.secrets.local
./scripts/azure-containerapp-deploy.sh
```
