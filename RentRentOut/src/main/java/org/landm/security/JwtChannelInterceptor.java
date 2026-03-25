package org.landm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtUtil jwtUtil;

    public JwtChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        log.debug("STOMP CONNECT pokušaj");

        List<String> authorization = accessor.getNativeHeader("Authorization");

        if (authorization != null && !authorization.isEmpty()) {
            String authHeader = authorization.get(0);

            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    if (jwtUtil.validateToken(token)) {
                        long userId = jwtUtil.extractUserId(token);
                        List<GrantedAuthority> authorities = jwtUtil.extractRoles(token);

                        UsernamePasswordAuthenticationToken authInfo =
                                new UsernamePasswordAuthenticationToken(userId, null, authorities);

                        accessor.setUser(authInfo);
                        log.debug("Korisnik ID {} uspešno autentifikovan za WebSocket", userId);

                        return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                    }
                } catch (Exception e) {
                    log.warn("STOMP JWT greška: {}", e.getMessage());
                }
            }
        } else {
            log.debug("Nema Authorization hedera u STOMP poruci");
        }
        return message;
    }
}