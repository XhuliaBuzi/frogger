package com.frogger.web.game;

import com.frogger.web.ws.GameWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives the game at a fixed 20 ticks/second (every 50ms), then pushes the
 * new state to every connected client.
 *
 * This replaces the original desktop game's `while (true) { ... sleep ... }`
 * loop, which both ran on the wrong thread (mutating Swing components off
 * the Event Dispatch Thread) and used TimeUnit.MICROSECONDS instead of
 * MILLISECONDS for its delay.
 */
@Component
public class GameLoopScheduler {

    private final GameEngine gameEngine;
    private final GameWebSocketHandler webSocketHandler;

    public GameLoopScheduler(GameEngine gameEngine, GameWebSocketHandler webSocketHandler) {
        this.gameEngine = gameEngine;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 50)
    public void tick() {
        gameEngine.tick();
        webSocketHandler.broadcastState();
    }
}
