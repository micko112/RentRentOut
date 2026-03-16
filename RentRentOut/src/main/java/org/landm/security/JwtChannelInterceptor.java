package org.landm.security;

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

    private final JwtUtil jwtUtil;

    public JwtChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Ispisujemo da vidimo da li je Angular uopšte probao da se nakači
            System.out.println(">>> STOMP CONNECT POKUŠAJ...");

            // Kod WebSocketa, hederi su upakovani u liste, zato koristimo getNativeHeader
            List<String> authorization = accessor.getNativeHeader("Authorization");

            if (authorization != null && !authorization.isEmpty()) {
                String authHeader = authorization.get(0);

                System.out.println(">>> TOKEN STIGAO U CEV: " + authHeader.substring(0, 20) + "..."); // Štampamo samo početak da ne prljamo log

                if (authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        if (jwtUtil.validateToken(token)) {
                            long userId = jwtUtil.extractUserId(token);
                            List<GrantedAuthority> authorities = jwtUtil.extractRoles(token);

                            UsernamePasswordAuthenticationToken authInfo =
                                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

                            accessor.setUser(authInfo);
                            System.out.println(">>> KORISNIK ID " + userId + " USPEŠNO ZAKAČEN ZA WEBSOCKET!");

                            // KLJUČNO: Vraćamo izmenjenu poruku sa korisnikom unutra!
                            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
                        }
                    } catch (Exception e) {
                        System.out.println(">>> STOMP JWT GREŠKA: " + e.getMessage());
                    }
                }
            } else {
                System.out.println(">>> NEMA AUTHORIZATION HEDERA U STOMP PORUCI!");
            }
        }
        return message;
    }
}