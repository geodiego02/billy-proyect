package com.billy.files.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	@Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
		logger.info("Configurando el Message Broker...");
        // Prefijo para enviar mensajes desde el servidor
        config.enableSimpleBroker("/topic"); 
        // Prefijo de destino a donde se enviarán mensajes desde el cliente
        config.setApplicationDestinationPrefixes("/app");
    }

	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
		logger.info("Registrando endpoint STOMP en '/ws-split'...");
        // Endpoint WebSocket al cual se conectará el front-end
        registry.addEndpoint("/ws-split").setAllowedOriginPatterns("*").withSockJS();
    }
}
