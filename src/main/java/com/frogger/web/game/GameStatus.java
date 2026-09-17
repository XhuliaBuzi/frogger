package com.frogger.web.game;

/**
 * Overall game state, sent to the client as a plain string in
 * {@link GameStateDto#status()}.
 *
 * RUNNING: the simulation ticks normally and player input is accepted.
 * GAME_OVER: the frog has 0 lives left; {@link GameEngine#tick()} stops
 * moving anything and {@link GameEngine#moveFrog} ignores input, until the
 * client sends a "restart" message (see GameWebSocketHandler), which calls
 * {@link GameEngine#reset()} and flips the status back to RUNNING.
 */
public enum GameStatus {
    RUNNING,
    GAME_OVER
}
