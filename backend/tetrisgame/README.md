# Tetris Game Backend

A Spring Boot backend for a multiplayer Tetris game supporting authentication, room management, and real-time gameplay via WebSocket.

## Setup & Run

1. Clone the repository
2. Configure application.properties with your database settings
3. Run with Maven:
```bash
mvn spring-boot:run
```

## API Documentation

### Authentication

- POST `/api/auth/register`
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- POST `/api/auth/login`
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
  Returns:
  ```json
  {
    "token": "JWT token",
    "user": {
      "id": "number",
      "username": "string",
      "lastScore": "number"
    }
  }
  ```

### Players

- POST `/api/players` - Create player for current user
- PATCH `/api/players/{id}/online?online=true|false` - Update player online status

### Rooms

- POST `/api/rooms` - Create new room (creator becomes host)

### WebSocket Protocol

Connect to `/ws` endpoint with SockJS/STOMP, using the JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

#### Client -> Server Messages:

- `/app/room/join/{roomId}/{playerId}` - Join room
- `/app/room/leave/{roomId}/{playerId}` - Leave room
- `/app/room/start/{roomId}` - Start game (host only)
- `/app/game/update/{playerId}` - Update game state
- `/app/game/over/{playerId}` - Signal game over

#### Server -> Client Messages:

Subscribe to `/topic/room/{roomId}` to receive:

- `JOIN` - Player joined room
- `LEAVE` - Player left room
- `START` - Game started
- `UPDATE` - Game state updated
- `GAME_OVER` - Player lost

Message format:
```json
{
  "type": "string",
  "roomId": "number",
  "playerId": "number",
  "username": "string",
  "payload": "object"
}
```

## Game Logic

The game follows standard Tetris rules:
- Board size: 10x20
- 7 different tetromino shapes
- Score based on lines cleared and drop speed
- Game over when new piece can't spawn