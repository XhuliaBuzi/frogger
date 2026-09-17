package com.frogger.web.game;

/**
 * Direction requested by the player - arrow keys, the on-screen D-pad
 * buttons, or a "move" WebSocket message from any client. Handled in
 * {@link GameEngine#moveFrog(MoveDirection)}.
 */
public enum MoveDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
