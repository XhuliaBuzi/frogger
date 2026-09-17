package com.frogger.web.game;

/**
 * A car, bus or log moving across a lane.
 *
 * This replaces the original project's {@code Object} class, which shadowed
 * {@code java.lang.Object} - a naming choice that's best avoided even though
 * it compiled fine.
 */
public class MovingEntity {

    private final int id;
    private final EntityType type;
    private final LaneDirection direction;
    private final double y;
    private final int width;
    private final int height;
    private final double speed;
    private double x;

    public MovingEntity(int id, EntityType type, LaneDirection direction,
                         double x, double y, int width, int height, double speed) {
        this.id = id;
        this.type = type;
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    /**
     * Advances this entity by one tick and wraps it around the board edges
     * (so a car that exits the right side re-enters from the left, etc.,
     * giving the endless-traffic look).
     *
     * @param speedMultiplier scales the entity's own base speed; this is how
     *                        GameEngine speeds everything up as the level
     *                        increases, without needing to touch each
     *                        entity's stored speed value.
     */
    public void tick(double boardWidth, double speedMultiplier) {
        x += deltaX() * speedMultiplier;
        if (direction == LaneDirection.RIGHT && x > boardWidth) {
            x = -width;
        } else if (direction == LaneDirection.LEFT && x < -width) {
            x = boardWidth;
        }
    }

    /** How much this entity moves on the x-axis in a single tick (signed, direction included). */
    public double deltaX() {
        return direction == LaneDirection.RIGHT ? speed : -speed;
    }

    /**
     * Standard axis-aligned bounding box (AABB) overlap test: true if the
     * rectangle (ox, oy, ow, oh) overlaps this entity's own rectangle.
     * Used both for "did the frog hit a car" and "is the frog standing on
     * this log" checks in GameEngine.
     */
    public boolean intersects(double ox, double oy, double ow, double oh) {
        return ox < x + width && ox + ow > x
                && oy < y + height && oy + oh > y;
    }

    public int getId() {
        return id;
    }

    public EntityType getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
