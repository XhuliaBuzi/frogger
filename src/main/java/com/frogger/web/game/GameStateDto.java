package com.frogger.web.game;

import java.util.List;

/**
 * Full snapshot of the game, broadcast as JSON to every connected client
 * every tick (every 50ms - see GameLoopScheduler). The frontend is a pure
 * renderer: it never computes game logic itself, it just draws whatever
 * this DTO says and sends "move"/"restart" messages back (see
 * GameWebSocketHandler).
 *
 * Fields:
 *  - frogX/frogY/frogWidth/frogHeight: the frog's position and size, in
 *    game-space pixels (the same 800x1000 coordinate system MovingEntity
 *    and GameConstants use).
 *  - lives/score/level: current player stats. `level` is uncapped and rises
 *    by 1 every goal reach (see GameEngine.reachGoal()).
 *  - status: "RUNNING" or "GAME_OVER" (GameStatus.name()).
 *  - reward: "POINTS" or "LIFE" for a few ticks right after reaching the
 *    goal (so the frontend can flash a "+1" / "+1 Life" toast), then null.
 *  - entities: every car, bus, and log currently on the board.
 */
public record GameStateDto(double frogX, double frogY, int frogWidth, int frogHeight,
                            int lives, int score, int level, String status,
                            String reward, List<EntityDto> entities) {
}
