package org.landm.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
            || path.equals("/api/user/register");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long userId = jwtUtil.extractUserId(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

            	if(true) {
                    UsernamePasswordAuthenticationToken authInfo =
                            new UsernamePasswordAuthenticationToken(
                                userId, null, null);

                        SecurityContextHolder.getContext().setAuthentication(authInfo);
            	}else {
            		throw new RuntimeException("Bad jwt cookie");
            	}
            	

            }
        }

        filterChain.doFilter(request, response);
    }
}
