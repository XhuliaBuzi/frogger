package com.frogger.web.config;

import com.frogger.web.ws.GameWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the raw WebSocket endpoint the browser client connects to.
 * Plain WebSocket (no STOMP/SockJS) is used deliberately: the frontend
 * can talk to it with nothing but the native browser WebSocket API.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameWebSocketHandler gameWebSocketHandler;

    public WebSocketConfig(GameWebSocketHandler gameWebSocketHandler) {
        this.gameWebSocketHandler = gameWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/game" is the single endpoint the frontend's game.js connects to.
        // setAllowedOrigins("*") is intentionally permissive for this demo
        // (no login/session to protect); lock this down to a specific
        // origin if this ever serves anything more sensitive.
        registry.addHandler(gameWebSocketHandler, "/game")
                .setAllowedOrigins("*");
    }
}
