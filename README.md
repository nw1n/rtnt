# Rum, Trade and Treasure (RTnT)

*Rum, Trade and Treasure* is a browser-based real-time economy-focused multiplayer game with a pirate theme.

## Tech Stack

- Backend: Java, Spring Boot
- Frontend: Angular, Angular Material, @elderbyte/ngx-starter UI library
- Database: MongoDB 8
- Deployment: Support for Azure Container Apps + Azure Container Registry (documented in `DEPLOY.md`)

## Architecture

The backend follows a layered structure inspired by Ports and Adapters and Clean Architecture:

- `domain`: core business model and contracts (e.g. `Ship`, `Island`, repositories)
- `usecase`: application logic and orchestration (startup initialization, journey flow,
  trading, price/supply dynamics, game loop scheduling)
- `adapter/in`: inbound adapters (e.g. REST controllers)
- `adapter/out`: outbound adapters (e.g. DB persistence)

## Local Development

### 1) Start MongoDB

```bash
docker compose up -d mongodb
```

Mongo is exposed locally on `localhost:27018` and used by default.

### 2) Run backend

```bash
./gradlew :rtnt-server:bootRun
```

### 3) Run frontend

```bash
cd rtnt-webapp
npm install
npm start
```

Default API base URL in the webapp: `http://localhost:18080/api`.

## Deployment

Azure deployment instructions are in `DEPLOY.md`.

## Contributing

This is a small hobby project, but contributions are very welcome.
If you have ideas, improvements, or bug fixes, feel free to open an issue or submit a pull request.
