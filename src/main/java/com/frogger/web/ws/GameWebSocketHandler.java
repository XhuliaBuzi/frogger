package com.frogger.web.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frogger.web.game.GameEngine;
import com.frogger.web.game.MoveDirection;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plain (non-STOMP) WebSocket endpoint at /game.
 *
 * Incoming messages from the browser look like:
 *   {"action":"move","direction":"UP"}
 *   {"action":"restart"}
 *
 * Outgoing messages are a JSON GameStateDto, pushed to every connected
 * session on each tick of the game loop (see GameLoopScheduler).
 *
 * Note: GameEngine is a single Spring-managed singleton, so every connected
 * browser shares the SAME game - fine for a solo demo, but anyone who opens
 * a second tab is controlling (and seeing) the same frog, not their own.
 * True multiplayer would need a GameEngine instance per session/session-id
 * instead of one shared instance.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameEngine gameEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            Map<?, ?> payload = objectMapper.readValue(message.getPayload(), Map.class);
            Object action = payload.get("action");
            if ("move".equals(action)) {
                String dir = String.valueOf(payload.get("direction"));
                try {
                    gameEngine.moveFrog(MoveDirection.valueOf(dir));
                } catch (IllegalArgumentException ignored) {
                    // unknown direction, ignore
                }
            } else if ("restart".equals(action)) {
                gameEngine.reset();
            }
        } catch (Exception ignored) {
            // malformed message, ignore rather than dropping the connection
        }
    }

    /** Pushes the current game state to every connected browser. */
    public void broadcastState() {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(gameEngine.snapshot());
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException ignored) {
                        // session likely closing; it will be removed via afterConnectionClosed
                    }
                }
            }
        } catch (Exception ignored) {
            // serialization failure - nothing sensible to do per-tick
        }
    }
}
