package org.landm.security;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/user/login")
            || path.equals("/api/user/register") 
            || path.equals("/api/auth/validate-email");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
        	
            String token = authHeader.substring(7);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
            	
            	if(jwtUtil.validateToken(token)) {
            		
                    long userId = jwtUtil.extractUserId(token);
                    
//                    request.setAttribute("userId", userId);
                    
                    List<GrantedAuthority> authorities = jwtUtil.extractRoles(token);
                    
                    UsernamePasswordAuthenticationToken authInfo =
                            new UsernamePasswordAuthenticationToken(
                                userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authInfo);
                    
            	}else{
            		
            		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		response.setContentType("application/json");
            		
            		String message = "{\"error\": \"JWT token is either modified, malformed or expired!\"}";
            		
            		response.getWriter().write(message);
            		response.getWriter().flush();
            		
            		return;
            	}
            }
        }

        filterChain.doFilter(request, response);
    }
}
