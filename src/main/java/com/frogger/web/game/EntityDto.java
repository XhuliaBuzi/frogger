package com.frogger.web.game;

/**
 * Wire format for one obstacle/platform (a car, bus, or log), sent to the
 * browser as JSON inside {@link GameStateDto}. The frontend uses `type` to
 * decide which 3D shape to draw, and x/y/width/height to place it - these
 * are game-space pixel coordinates (0-800 by 0-1000), not 3D world units;
 * the frontend converts them (see game.js's toWorldX/toWorldZ).
 */
public record EntityDto(int id, String type, double x, double y, int width, int height) {
}
