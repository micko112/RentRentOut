package org.landm.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Štiti od Brute-Force napada ograničavanjem broja zahteva po IP adresi.
 *
 * Zaštićeni endpointi:
 *  - POST /api/user/login          → 10 pokušaja / 1 minut
 *  - POST /api/user/forgot-password → 5 pokušaja / 5 minuta
 *  - POST /api/user/register        → 5 pokušaja / 1 minut
 *  - POST /api/user/google-login   → 15 pokušaja / 1 minut
 *  - POST /api/user/facebook-login → 15 pokušaja / 1 minut
 *  - POST /api/user/apple-login    → 15 pokušaja / 1 minut
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record RatePolicy(int capacity, int refillTokens, Duration refillDuration) {}

    private static final Map<String, RatePolicy> POLICIES = Map.of(
        "/api/user/login",           new RatePolicy(10, 10, Duration.ofMinutes(1)),
        "/api/user/forgot-password", new RatePolicy(5,  5,  Duration.ofMinutes(5)),
        "/api/user/register",        new RatePolicy(5,  5,  Duration.ofMinutes(1)),
        "/api/user/google-login",    new RatePolicy(15, 15, Duration.ofMinutes(1)),
        "/api/user/facebook-login",  new RatePolicy(15, 15, Duration.ofMinutes(1)),
        "/api/user/apple-login",     new RatePolicy(15, 15, Duration.ofMinutes(1))
    );

    // ip:endpoint → Bucket
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        RatePolicy policy = POLICIES.get(uri);

        if (policy != null && "POST".equalsIgnoreCase(request.getMethod())) {
            String ip = getClientIp(request);
            String key = ip + ":" + uri;

            Bucket bucket = buckets.computeIfAbsent(key, k -> buildBucket(policy));

            if (!bucket.tryConsume(1)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"Previše pokušaja. Pokušajte ponovo za malo.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket buildBucket(RatePolicy policy) {
        Bandwidth limit = Bandwidth.classic(
            policy.capacity(),
            Refill.greedy(policy.refillTokens(), policy.refillDuration())
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
