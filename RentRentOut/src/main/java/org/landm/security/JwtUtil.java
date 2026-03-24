package org.landm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.landm.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    // Access token — 15 min, nosi roles
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getStringRoles());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    // Refresh token — 7 dana, samo userId
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    // Alias — zadržano radi kompatibilnosti (WebSocket interceptor)
    public String generateToken(User user) {
        return generateAccessToken(user);
    }

    //Extracts userId from generated JWT token
    public Long extractUserId(String token){
        String subj = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return Long.parseLong(subj);
    }
    
    public List<GrantedAuthority> extractRoles(String token) {
    	Claims claims = Jwts.parserBuilder()
    	        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
    	        .build()
    	        .parseClaimsJws(token)
    	        .getBody();

//        String rolesString = (String) claims.get("roles");
//        
//        List<String> roles = Arrays.stream(rolesString.split(" "))
//        		.filter(r -> r.isBlank())
//        		.collect(Collectors.toList());

    	List<String> roles = (List<String>) claims.get("roles");
    	
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    
    public Date extractExpiration(String token) {
    	return Jwts.parserBuilder()
    			.setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
    			.build()
    			.parseClaimsJws(token)
    			.getBody()
    			.getExpiration();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
