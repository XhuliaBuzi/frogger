package com.frogger.web.game;

/**
 * Type of a moving obstacle/platform on the board.
 *
 * CAR and BUS are road hazards - touching one costs a life (see
 * GameEngine.hitsTraffic()). LOG is a river platform - the frog must be
 * standing on one while in the river zone, or it drowns (see
 * GameEngine.findRidingLog()).
 */
public enum EntityType {
    CAR,
    BUS,
    LOG
}
