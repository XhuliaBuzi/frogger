# Frogger — Java Backend Edition (3D)

A rewrite of the classic Frogger arcade game as a web app: a **Java backend
(Spring Boot + WebSocket)** owns every rule of the game, and a **Three.js 3D
frontend** renders whatever the backend says and sends the player's input.
Originally ported from a Java Swing desktop project
([XhuliaBuzi/frogger](https://github.com/XhuliaBuzi/frogger)), fixing
several bugs along the way (see "Bugs fixed" below) and growing well beyond
the original with 3D graphics, multiple difficulty levels, and three
camera views.

## Play it

Requires Java 17+ and Maven (or use Docker, see "Deploy" below).

```bash
mvn spring-boot:run
```

Open `http://localhost:8080`. Controls: arrow keys, or the on-screen D-pad
(works with mouse click or touch). The button in the top-right of the HUD
cycles between three camera views: top-down, first-person ("frog's-eye"),
and a third-person chase camera from behind the frog.

## Architecture

```
Browser (Three.js + WebGL)  <== WebSocket (/game) ==>  Spring Boot backend
                                                          |
                                                          +-- GameEngine (all rules & state)
                                                          +-- GameLoopScheduler (ticks every 50ms)
                                                          +-- GameWebSocketHandler (I/O)
```

- **The backend computes everything**: obstacle positions, collisions,
  lives, score, level, and the level-up traffic/log density change. The
  frontend (`game.js`) is a pure renderer - it never decides whether a move
  is legal or whether the frog died; it just draws the state it's given and
  sends `{"action":"move","direction":"UP"}` / `{"action":"restart"}`
  messages back over the WebSocket.
- Communication uses a **plain WebSocket** (no STOMP/SockJS), so the
  browser can connect with a bare `new WebSocket(...)` and no extra
  libraries - see `GameWebSocketHandler.java` and the `connect()` function
  in `game.js`.
- Rendering is a **Three.js 3D scene**: low-poly cars/buses, bamboo-raft
  logs, a jointed frog model, a gradient sky, and a big grass "floor" so the
  camera never scrolls into empty space. See the comments throughout
  `game.js` for how each piece works.
- The game currently has **one single shared state** (`GameEngine` is a
  Spring singleton) - fine for a solo demo, but every connected browser
  controls and sees the *same* frog. Real multiplayer would need a
  `GameEngine` instance per session instead of one shared instance (see the
  note on `GameWebSocketHandler`).

## Gameplay mechanics

- **Levels are uncapped.** Reaching the goal raises the level by 1 and
  every entity speeds up by 25% per level, forever - there's no ceiling,
  the game just keeps getting faster until you run out of lives.
- **Traffic/log density changes once.** Level 1 starts sparse on
  cars/buses and generous on logs (an easier first crossing); the moment
  the level reaches 2, the lanes are reshuffled to more traffic and fewer
  logs, and *stay* at that density for the rest of the run - only speed
  keeps climbing after that.
- **Bonus lives**: reaching the goal has a 1-in-5 chance of granting an
  extra life (capped at 5) instead of just points, shown as a brief "+1
  Life!" toast.
- **Death animation**: dying (hit by traffic, or drowning in the river)
  squashes the frog flat with a blood splatter, holds for about half a
  second (input is briefly ignored), then pops back to normal at the start
  tile. This is detected entirely client-side, as a drop in the `lives`
  count between two server states - see the comment on `applyState()` in
  `game.js`.

## Bugs fixed from the original (Swing) version

1. **The `Object` class** shadowed `java.lang.Object` → renamed to `MovingEntity`.
2. **`TimeUnit.MICROSECONDS.sleep(100)`** → the loop ran roughly 1000x faster than
   intended. The simulation is now driven by `@Scheduled(fixedRate = 50)` (50ms/tick).
3. **`temp == "R"`** (string comparison with `==`) → replaced with the `LaneDirection` enum.
4. **The biggest bug:** reaching the goal (y=0) called `restart()`, i.e. it **cost a
   life** instead of **scoring a point**. Reaching the goal now correctly increases
   the score and respawns the frog without losing a life.
5. **`getSpeed()`** computed a new x-offset but never applied it (dead code) →
   movement and wrap-around now happen directly in `MovingEntity.tick()`.
6. **Thread safety**: Swing components were mutated off the Event Dispatch Thread →
   no longer applicable (no Swing at all); the shared game state is now guarded by
   a `ReentrantLock` (see `GameEngine`, since `tick()` runs on the scheduler
   thread while `moveFrog()`/`reset()` run on the WebSocket thread).
7. **`life == 0`** just printed to the console and kept running → there is now a real
   `GAME_OVER` status that halts the simulation until a restart is requested
   (`{"action":"restart"}`).
8. **An off-by-one in AABB overlap checks**: a frog jumping from the median
   straight onto the first log row could land exactly on the log's edge and
   be rejected as "not overlapping" by a strict `<` comparison. Fixed by
   widening the log's own hit-box slightly (rather than loosening the
   overlap test everywhere, which caused a different bug: the frog
   occasionally surviving in open water). See the comments in
   `GameEngine.buildLaneSpecs()` and `MovingEntity.intersects()`.
9. Images used to depend on the JVM's working directory → the frontend no longer
   loads image files at all; obstacles and the frog are drawn as 3D primitives.

## WebSocket protocol

Endpoint: `ws(s)://<host>/game`

**Client → server:**
```json
{"action":"move","direction":"UP"}      // UP | DOWN | LEFT | RIGHT
{"action":"restart"}
```

**Server → client** (every ~50ms, see `GameStateDto`):
```json
{
  "frogX": 375, "frogY": 900, "frogWidth": 50, "frogHeight": 50,
  "lives": 3, "score": 0, "level": 1, "status": "RUNNING",
  "reward": null,
  "entities": [
    {"id": 1, "type": "CAR", "x": 120, "y": 500, "width": 100, "height": 70}
  ]
}
```

## Deploy for free on Render

1. Push this project to a GitHub repo.
2. On [render.com](https://render.com) → **New +** → **Web Service**.
3. Connect the repo. Render auto-detects the `Dockerfile` and uses it to build.
4. Pick the **Free** plan.
5. Deploy. Render gives you a public link (`https://<your-app>.onrender.com`).

**Note:** Render's free plan spins the service down after ~15 minutes without
traffic; the first request after that takes 30-60 seconds to wake up (cold start).
That's normal for a portfolio demo — just mention it in the project description.

## Project structure

```
src/main/java/com/frogger/web/
  FroggerWebApplication.java   # entry point (@SpringBootApplication + @EnableScheduling)
  config/WebSocketConfig.java  # registers the /game WebSocket endpoint
  ws/GameWebSocketHandler.java # per-connection I/O: receives input, broadcasts state
  game/
    GameEngine.java        # all game rules and state live here
    GameLoopScheduler.java # ticks GameEngine every 50ms and broadcasts
    MovingEntity.java       # a car/bus/log: position, movement, AABB overlap test
    Frog.java               # player state: position, lives, score
    GameConstants.java       # every tunable number, with reasoning for each
    EntityType.java / LaneDirection.java / MoveDirection.java / GameStatus.java
    GameStateDto.java / EntityDto.java   # the JSON wire format
src/main/resources/
  application.properties   # binds to $PORT (Render sets this) or 8080 locally
  static/index.html   # page structure; every element game.js touches is commented
  static/style.css    # dark theme, D-pad, HUD, toast/overlay styling
  static/game.js      # the entire frontend: Three.js scene, WebSocket client, input
Dockerfile
```
