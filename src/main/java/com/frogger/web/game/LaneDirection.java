package com.frogger.web.game;

/**
 * Direction a lane's traffic (or logs) travels in.
 * Replaces the original code's "L"/"R" string comparisons, which were
 * compared with == instead of .equals() - a latent bug even though it
 * happened to work due to String interning of literals.
 */
public enum LaneDirection {
    LEFT,
    RIGHT
}
