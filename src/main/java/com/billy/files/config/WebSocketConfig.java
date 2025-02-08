package com.billy.files.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.AbstractWebSocketMessage;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para enviar mensajes desde el servidor
        config.enableSimpleBroker("/topic"); 
        // Prefijo de destino a donde se enviarán mensajes desde el cliente
        config.setApplicationDestinationPrefixes("/app");
    }

	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket al cual se conectará el front-end
        registry.addEndpoint("/ws-split").setAllowedOriginPatterns("*").withSockJS();
    }
}
