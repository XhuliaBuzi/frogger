package com.frogger.web.game;

/**
 * Plain data holder for the player's state: position, lives, and score.
 * All game rules (movement validation, collisions, scoring) live in
 * {@link GameEngine}; this class just stores the numbers.
 *
 * WIDTH/HEIGHT match GameConstants.CELL (50) so the frog occupies exactly
 * one grid cell, which is what makes its jumps line up cleanly with the
 * lanes it's crossing.
 */
public class Frog {

    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;

    private double x;
    private double y;
    private int lives;
    private int score;

    public Frog(double startX, double startY, int startingLives) {
        this.x = startX;
        this.y = startY;
        this.lives = startingLives;
        this.score = 0;
    }

    public void resetPosition(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
