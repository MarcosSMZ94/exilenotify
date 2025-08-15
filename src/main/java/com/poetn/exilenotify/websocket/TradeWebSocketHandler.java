package com.poetn.exilenotify.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TradeWebSocketHandler {
    
    private final SimpMessagingTemplate messagingTemplate;

    public TradeWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastMessage(String message) {
        System.out.println("Broadcasting message via STOMP: " + message);
        messagingTemplate.convertAndSend("/topic/trades", message);
    }
}
