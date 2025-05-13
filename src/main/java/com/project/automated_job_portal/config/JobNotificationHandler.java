package com.project.automated_job_portal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobNotificationHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(JobNotificationHandler.class);
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("New WebSocket connection established: {}", session.getId());
        // Send a welcome message
        session.sendMessage(new TextMessage("Connected to job notifications"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            logger.debug("Received message: {} from session: {}", payload, session.getId());
            // Handle subscription or other commands if needed
            if ("subscribe".equalsIgnoreCase(payload)) {
                session.sendMessage(new TextMessage("Subscribed to job notifications"));
            }
        } catch (Exception e) {
            logger.error("Error handling message: {}", e.getMessage());
            session.sendMessage(new TextMessage("Error: " + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
    }

    // Method to broadcast new job postings to all connected clients
    public void broadcastJobNotification(Object job) throws IOException {
        String message = objectMapper.writeValueAsString(job);
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                    logger.debug("Sent job notification to session: {}", session.getId());
                } catch (IOException e) {
                    logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }
}