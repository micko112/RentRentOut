package org.landm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(frontendBaseUrl);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. KANALI ZA SLUŠANJE (Gde Angular "čeka" poruke sa servera)
        // Ako server hoće nešto da pošalje korisniku, slaće na putanje koje počinju sa /topic (za sve) ili /user (privatno)
        registry.enableSimpleBroker("/topic", "/user");

        // Da bi /user/... rute radile ispravno za privatne poruke
        registry.setUserDestinationPrefix("/user");

        // 3. KANALI ZA SLANJE (Gde Angular "šalje" poruke ka serveru)
        // Ako Angular pošalje poruku na /app/chat.sendMessage, Spring će tražiti metodu sa @MessageMapping("/chat.sendMessage")
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }



}
