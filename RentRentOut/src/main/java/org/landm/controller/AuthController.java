package org.landm.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.landm.entity.User;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    public AuthController(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody(required = false) Map<String, String> body,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        boolean isMobile = "mobile".equalsIgnoreCase(request.getHeader("X-Client-Platform"));

        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null && body != null) {
            refreshToken = body.get("refreshToken");
        }

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or missing refresh token"));
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isEnabled()) {
            return ResponseEntity.status(401).build();
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        response.addHeader("Set-Cookie", buildCookie("access_token", newAccessToken, accessExpiration / 1000).toString());

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("wsToken", jwtUtil.generateToken(user));
        if (isMobile) {
            res.put("accessToken", newAccessToken);
            res.put("refreshToken", jwtUtil.generateRefreshToken(user));
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("access_token",  "", 0).toString());
        response.addHeader("Set-Cookie", buildCookie("refresh_token", "", 0).toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ws-token")
    public ResponseEntity<?> getWsToken(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        Long userId = (Long) auth.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(Map.of("wsToken", jwtUtil.generateToken(user)));
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
