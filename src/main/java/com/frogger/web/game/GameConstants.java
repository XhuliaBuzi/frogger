package com.frogger.web.game;

/**
 * All of the game's tunable numbers in one place. Everything here is in
 * "game space" pixels (an 800x1000 board), the same coordinate system the
 * original desktop Frogger used - the 3D frontend converts these to its own
 * small world-unit scale (see game.js's WORLD_SCALE/toWorldX/toWorldZ).
 */
public final class GameConstants {

    private GameConstants() {
        // no instances - this is a pure constant holder
    }

    public static final int BOARD_WIDTH = 800;
    public static final int BOARD_HEIGHT = 1000;

    /** One grid step for the frog's jump (matches the frog's own size, so jumps tile cleanly). */
    public static final int CELL = 50;

    public static final double FROG_START_X = 375;
    public static final double FROG_START_Y = 900;

    /** Reaching this y (or below) means the frog made it home - see GameEngine.reachGoal(). */
    public static final double GOAL_Y = 0;

    /** Below this y the frog is on the river (needs a log); above it, on the road/banks. */
    public static final double RIVER_ZONE_MAX_Y = 410;

    /**
     * Extra horizontal tolerance (px) when checking whether the frog landed
     * on a log. Currently 0 (no extra tolerance) - a wider log hit-box
     * (GameEngine's log LaneSpec height) already gives enough margin for
     * fair, predictable landings, so this stays at 0 to avoid "surviving in
     * open water" false positives. Kept as a named constant (rather than
     * removed) in case future speed/density tuning calls for a small
     * allowance again - see GameEngine.findRidingLog().
     */
    public static final double LOG_LANDING_FORGIVENESS = 0;

    public static final int STARTING_LIVES = 3;
    public static final int MAX_LIVES = 5;

    /**
     * Levels are uncapped: the level increases by 1 every time the frog
     * reaches the goal, and speed keeps climbing (level-1)*LEVEL_SPEED_STEP
     * forever - there's no ceiling, the game just keeps getting faster until
     * the player runs out of lives. Traffic/log DENSITY only changes once,
     * the first time the level goes from 1 to 2 (see
     * GameEngine.DENSITY_STEP_UP_LEVEL) - from then on lane counts are fixed
     * and only speed keeps increasing.
     */
    public static final double LEVEL_SPEED_STEP = 0.25;

    /** How many ticks (at 50ms/tick) a reward toast ("+1", "+1 Life") stays visible to the client. */
    public static final int REWARD_DISPLAY_TICKS = 24;

    /** 1-in-N chance that reaching the goal grants a bonus life instead of just points. */
    public static final int LIFE_BONUS_ODDS = 5;
}
