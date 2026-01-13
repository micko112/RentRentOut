package org.landm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.landm.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final String secret = "mySecretJWT123456789012345678901";

    //Generates JWT token for remembering logged user
    public String generateToken(User user){
    	Map<String, Object> claims = new HashMap<>();
    	claims.put("roles", user.getStringRoles());
    	long userId = user.getId();
    	
        return Jwts.builder()
        		.setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    //Extracts userId from generated JWT token
    public long extractUserId(String token){
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
