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
