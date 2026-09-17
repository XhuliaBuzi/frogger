package com.frogger.web.game;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Holds and advances the whole game state. This replaces the original
 * MyGame/Board pair, fixing several bugs found in the desktop version:
 *
 *  - The frog reaching the goal row used to call restart() (i.e. LOSE a
 *    life) instead of scoring. Reaching y=0 now correctly awards a point.
 *  - The obstacle loop used TimeUnit.MICROSECONDS.sleep(100) - a 100
 *    MICROsecond delay, not 100ms, which made the loop run far too fast.
 *    Ticking is now driven by a fixed-rate scheduler (see GameLoopScheduler).
 *  - Object.getSpeed() computed a new x-offset but never applied it - dead
 *    code. Movement/wrapping is now handled directly in MovingEntity.tick().
 *  - "temp == \"R\"" string comparisons are replaced with a proper enum.
 *  - Losing the last life used to just print "Game Over" and keep running.
 *    The engine now sets a real GAME_OVER status that stops the simulation
 *    until a restart is requested.
 *
 * On top of the original game, reaching the goal now also:
 *  - Raises the difficulty level (uncapped) by 1 every time the frog reaches
 *    the goal. Level 1 starts sparse on traffic and dense on logs (easier
 *    river); the first time the level reaches 2, lanes are respawned once
 *    with DENSITY_STEP_UP_LEVEL's counts (more traffic, fewer logs) and stay
 *    at that density from then on - only speed keeps climbing every level
 *    after that, indefinitely.
 *  - Has a 1-in-LIFE_BONUS_ODDS chance of granting a bonus life (capped at
 *    MAX_LIVES) instead of just points, surfaced to the client for a few
 *    ticks via the `reward` field so it can show a brief toast.
 *
 * State is guarded by a lock because ticks (scheduler thread) and player
 * moves (WebSocket thread) can happen concurrently.
 */
@Component
public class GameEngine {

    /** The level at which lane density switches from sparse-traffic to dense-traffic. */
    private static final int DENSITY_STEP_UP_LEVEL = 2;

    // Lane "templates" (position, speed, direction, how many entities) built
    // once in buildLaneSpecs() and reused every time lanes are (re)spawned.
    private final List<LaneSpec> riverLanes = new ArrayList<>();
    private final List<LaneSpec> roadLanes = new ArrayList<>();
    // Guards every field below: tick() runs on the scheduler thread while
    // moveFrog()/reset() run on the WebSocket thread, so without this lock
    // the two could race (e.g. a tick reading the frog's position mid-move).
    private final ReentrantLock lock = new ReentrantLock();
    private final Random random = new Random();

    // The live obstacles currently on the board (rebuilt by spawnAllLanes()).
    private List<MovingEntity> entities = new ArrayList<>();
    private Frog frog;
    private GameStatus status;
    // Every MovingEntity gets a fresh id from this counter so the frontend
    // can track "the same car" across successive state snapshots.
    private int nextId = 1;
    private int level;
    // Flips true (permanently, until reset()) the first time the level
    // reaches DENSITY_STEP_UP_LEVEL; see spawnAllLanes()/reachGoal().
    private boolean denseTraffic;
    // Transient "+1"/"+1 Life" flag for the client toast; see reachGoal()
    // and the countdown in tick().
    private String reward;
    private int rewardTicksRemaining;

    public GameEngine() {
        buildLaneSpecs();
        reset();
    }

    /**
     * Defines the 10 lanes (5 river, 5 road) once at startup: their y
     * position, direction, base speed, and how many entities occupy them at
     * "low" (level 1) vs "high" (level 2+) density. Called once from the
     * constructor; spawnAllLanes() re-reads these specs every time lanes
     * need to be (re)populated (initial reset, restart, or the one-time
     * density step-up), so the actual entity list can be thrown away and
     * rebuilt cheaply without redoing this layout math.
     */
    private void buildLaneSpecs() {
        // Logs: level 1 is generous (easier river crossing); density step-up thins them out.
        double[] riverYs = {60, 130, 200, 270, 340};
        LaneDirection[] riverDirs = {
                LaneDirection.RIGHT, LaneDirection.LEFT, LaneDirection.RIGHT,
                LaneDirection.LEFT, LaneDirection.RIGHT
        };
        for (int i = 0; i < riverYs.length; i++) {
            riverLanes.add(new LaneSpec(EntityType.LOG, riverDirs[i], riverYs[i], 140, 65,
                    1.5 + i * 0.3, 3, 2));
        }

        // Traffic: level 1 is sparse (easier road crossing); density step-up adds more cars/buses.
        double[] roadYs = {500, 580, 660, 740, 820};
        EntityType[] roadTypes = {
                EntityType.CAR, EntityType.BUS, EntityType.CAR, EntityType.BUS, EntityType.CAR
        };
        LaneDirection[] roadDirs = {
                LaneDirection.RIGHT, LaneDirection.LEFT, LaneDirection.RIGHT,
                LaneDirection.LEFT, LaneDirection.RIGHT
        };
        for (int i = 0; i < roadYs.length; i++) {
            EntityType type = roadTypes[i];
            int w = type == EntityType.BUS ? 120 : 100;
            int h = type == EntityType.BUS ? 80 : 70;
            int countLow = type == EntityType.BUS ? 1 : 2;
            int countHigh = type == EntityType.BUS ? 2 : 3;
            roadLanes.add(new LaneSpec(type, roadDirs[i], roadYs[i], w, h, 2.0 + i * 0.4, countLow, countHigh));
        }
    }

    /** Starts (or restarts, via the client's "restart" message) a fresh game: level 1, full lives, empty score. */
    public void reset() {
        lock.lock();
        try {
            level = 1;
            denseTraffic = false;
            reward = null;
            rewardTicksRemaining = 0;
            spawnAllLanes();
            frog = new Frog(GameConstants.FROG_START_X, GameConstants.FROG_START_Y, GameConstants.STARTING_LIVES);
            status = GameStatus.RUNNING;
        } finally {
            lock.unlock();
        }
    }

    /** Throws away the current entity list and rebuilds every lane from scratch, at the current density. */
    private void spawnAllLanes() {
        entities = new ArrayList<>();
        nextId = 1;
        for (LaneSpec spec : riverLanes) {
            spawnLane(spec, denseTraffic ? spec.countHigh : spec.countLow);
        }
        for (LaneSpec spec : roadLanes) {
            spawnLane(spec, denseTraffic ? spec.countHigh : spec.countLow);
        }
    }

    /** Places `count` evenly-spaced entities along one lane, per its LaneSpec. */
    private void spawnLane(LaneSpec spec, int count) {
        double spacing = spec.type == EntityType.LOG ? 1.5 : 1.0; // extra room between logs specifically
        double gap = GameConstants.BOARD_WIDTH / (double) count * spacing;
        for (int i = 0; i < count; i++) {
            double x = i * gap;
            entities.add(new MovingEntity(nextId++, spec.type, spec.direction, x, spec.y, spec.width, spec.height, spec.speed));
        }
    }

    /** How much every entity's base speed is scaled by at the current level (1.0 at level 1, then +25% per level). */
    private double speedMultiplier() {
        return 1.0 + (level - 1) * GameConstants.LEVEL_SPEED_STEP;
    }

    /** Advances the simulation by one frame. Called by the scheduler. */
    public void tick() {
        lock.lock();
        try {
            if (rewardTicksRemaining > 0) {
                rewardTicksRemaining--;
                if (rewardTicksRemaining == 0) {
                    reward = null;
                }
            }
            if (status != GameStatus.RUNNING) {
                return;
            }
            double multiplier = speedMultiplier();
            for (MovingEntity entity : entities) {
                entity.tick(GameConstants.BOARD_WIDTH, multiplier);
            }
            handleFrogPhysics(multiplier);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Runs every tick while the frog is RUNNING: moves it along with
     * whatever log it's riding (river zone), or checks for a traffic hit
     * (road zone). This is the "ongoing" check - see checkImmediateLanding()
     * below for the extra check done right at the moment of a jump, which
     * exists for a different reason (timing, not periodic movement).
     */
    private void handleFrogPhysics(double multiplier) {
        boolean inRiver = frog.getY() < GameConstants.RIVER_ZONE_MAX_Y;
        if (inRiver) {
            MovingEntity ridingLog = findRidingLog();
            if (ridingLog == null) {
                loseLife();
                return;
            }
            double newX = frog.getX() + ridingLog.deltaX() * multiplier;
            if (newX < 0 || newX + Frog.WIDTH > GameConstants.BOARD_WIDTH) {
                loseLife();
                return;
            }
            frog.setX(newX);
        } else if (hitsTraffic()) {
            loseLife();
        }
    }

    /**
     * Checked once, synchronously, right when the frog's move lands it on a
     * new tile - rather than waiting for the next scheduled tick() (up to
     * one tick interval later), by which time a fast-moving log may have
     * already drifted out from under the frog. Removes that timing gap so a
     * landing that looked good to the player is judged at the moment they
     * made it, not slightly later.
     */
    private void checkImmediateLanding() {
        boolean inRiver = frog.getY() < GameConstants.RIVER_ZONE_MAX_Y;
        if (inRiver) {
            if (findRidingLog() == null) {
                loseLife();
            }
        } else if (hitsTraffic()) {
            loseLife();
        }
    }

    /** Finds a log under the frog, with a little extra horizontal tolerance so near-misses still count. */
    private MovingEntity findRidingLog() {
        for (MovingEntity entity : entities) {
            if (entity.getType() == EntityType.LOG
                    && entity.intersects(frog.getX() - GameConstants.LOG_LANDING_FORGIVENESS, frog.getY(),
                            Frog.WIDTH + 2 * GameConstants.LOG_LANDING_FORGIVENESS, Frog.HEIGHT)) {
                return entity;
            }
        }
        return null;
    }

    /** True if any car or bus currently overlaps the frog's position. */
    private boolean hitsTraffic() {
        for (MovingEntity entity : entities) {
            if ((entity.getType() == EntityType.CAR || entity.getType() == EntityType.BUS)
                    && entity.intersects(frog.getX(), frog.getY(), Frog.WIDTH, Frog.HEIGHT)) {
                return true;
            }
        }
        return false;
    }

    /** Deducts one life, ends the game if that was the last one, and always respawns the frog at the start. */
    private void loseLife() {
        frog.setLives(frog.getLives() - 1);
        if (frog.getLives() <= 0) {
            frog.setLives(0);
            status = GameStatus.GAME_OVER;
        }
        frog.resetPosition(GameConstants.FROG_START_X, GameConstants.FROG_START_Y);
    }

    /**
     * Handles a player input (arrow key or on-screen button) coming from
     * the WebSocket. Validates the move stays on the board, applies it,
     * then either scores the goal or runs the immediate landing check -
     * exactly one of those two happens per successful move, never both.
     */
    public void moveFrog(MoveDirection direction) {
        lock.lock();
        try {
            if (status != GameStatus.RUNNING) {
                return;
            }
            double x = frog.getX();
            double y = frog.getY();
            switch (direction) {
                case LEFT -> x -= GameConstants.CELL;
                case RIGHT -> x += GameConstants.CELL;
                case UP -> y -= GameConstants.CELL;
                case DOWN -> y += GameConstants.CELL;
            }
            if (x < 0 || x + Frog.WIDTH > GameConstants.BOARD_WIDTH) {
                return;
            }
            if (y < 0 || y + Frog.HEIGHT > GameConstants.BOARD_HEIGHT) {
                return;
            }
            frog.setX(x);
            frog.setY(y);

            if (y <= GameConstants.GOAL_Y) {
                reachGoal();
            } else {
                checkImmediateLanding();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called when a move lands the frog at y &lt;= GOAL_Y. Awards a point,
     * advances the level, does the one-time density step-up if this is the
     * level-2 crossing, rolls the life-bonus chance, and respawns the frog
     * at the start for the next crossing.
     */
    private void reachGoal() {
        frog.setScore(frog.getScore() + 1);
        level += 1; // uncapped, +1 every time the frog reaches the goal

        if (!denseTraffic && level >= DENSITY_STEP_UP_LEVEL) {
            denseTraffic = true;
            spawnAllLanes(); // one-time reflow: more traffic, fewer logs, from now on
        }

        boolean grantsLife = random.nextInt(GameConstants.LIFE_BONUS_ODDS) == 0
                && frog.getLives() < GameConstants.MAX_LIVES;
        if (grantsLife) {
            frog.setLives(frog.getLives() + 1);
            reward = "LIFE";
        } else {
            reward = "POINTS";
        }
        rewardTicksRemaining = GameConstants.REWARD_DISPLAY_TICKS;

        frog.resetPosition(GameConstants.FROG_START_X, GameConstants.FROG_START_Y);
    }

    /** Builds the immutable DTO sent to clients this tick - see GameStateDto for what each field means. */
    public GameStateDto snapshot() {
        lock.lock();
        try {
            List<EntityDto> dtos = new ArrayList<>(entities.size());
            for (MovingEntity entity : entities) {
                dtos.add(new EntityDto(entity.getId(), entity.getType().name(),
                        entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight()));
            }
            return new GameStateDto(frog.getX(), frog.getY(), Frog.WIDTH, Frog.HEIGHT,
                    frog.getLives(), frog.getScore(), level, status.name(), reward, dtos);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Immutable template for one lane: everything needed to (re)spawn its
     * entities via spawnLane(). countLow/countHigh are the entity counts
     * used before/after the one-time density step-up (see denseTraffic).
     */
    private static final class LaneSpec {
        final EntityType type;
        final LaneDirection direction;
        final double y;
        final int width;
        final int height;
        final double speed;
        final int countLow;
        final int countHigh;

        LaneSpec(EntityType type, LaneDirection direction, double y, int width, int height,
                 double speed, int countLow, int countHigh) {
            this.type = type;
            this.direction = direction;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            this.countLow = countLow;
            this.countHigh = countHigh;
        }
    }
}
