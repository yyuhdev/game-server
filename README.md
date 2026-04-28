# Revived Game Server

A modular Minecraft server project built with Paper, featuring multiple minigames and a microservices architecture.

## Project Structure

| Module | Description |
|--------|-------------|
| `shared` | Shared utilities (GUI system, translation engine, result types) |
| `lobby` | Lobby server with queue integration |
| `game/game-api` | Game framework API (gamemode abstraction, game handlers) |
| `game/game-server` | Main game server hosting all minigames |
| `game/minigames/*` | minigame implementations |
| `services/*` | microservices |

## Building

Build all modules:

```bash
./gradlew build
```

## Development

Run tests:

```bash
./gradlew test
```

Clean build:

```bash
./gradlew clean build
```

Start all services with Docker:

```bash
./gradlew dev
```

This builds the project and starts all containers defined in `docker-compose.yml`:
- MongoDB (port 27017)
- Redis/Dragonfly (port 6379)
- NATS (port 4222)
- InfluxDB (port 8086)
- Queue Service

## Configuration

Translation files are located in:
- `lobby/src/main/resources/translations/`
- `game/game-server/src/main/resources/translations/`

GUI configurations are stored in:
- `lobby/src/main/resources/gui/`
