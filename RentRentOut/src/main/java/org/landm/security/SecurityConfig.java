package org.landm.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

	private final JwtFilter jwtFilter;

	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}
	
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	            .headers(headers -> headers
	                .frameOptions(frame -> frame.deny())
	                .contentTypeOptions(Customizer.withDefaults())
	                .httpStrictTransportSecurity(hsts -> hsts
	                    .includeSubDomains(true)
	                    .maxAgeInSeconds(31536000))
	                .referrerPolicy(referrer -> referrer
	                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
	                .contentSecurityPolicy(csp -> csp.policyDirectives(
	                    "default-src 'self'; " +
	                    "script-src 'self' 'unsafe-inline' accounts.google.com apis.google.com connect.facebook.net; " +
	                    "style-src 'self' 'unsafe-inline' fonts.googleapis.com; " +
	                    "font-src 'self' fonts.gstatic.com data:; " +
	                    "img-src 'self' data: blob: https:; " +
	                    "connect-src 'self' wss: https://accounts.google.com https://graph.facebook.com; " +
	                    "frame-src 'self' accounts.google.com;"
	                )))
	            .exceptionHandling(exceptions -> exceptions
	                .authenticationEntryPoint((request, response, authException) -> {
	                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	                    response.setContentType("application/json");
	                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
	                }))
	            .authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/user/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/user/google-login").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/user/facebook-login").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/user/apple-login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/user/me").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/ads/me/saved").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/ads/*/saved-status").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.PUT, "/api/ads/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/ads/*/view").authenticated()


						.requestMatchers(HttpMethod.POST, "/api/rental-contract").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/rental-contract").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/rental-contract").authenticated()

						.requestMatchers(HttpMethod.POST, "/api/rental-contract/block").authenticated()

						.requestMatchers(HttpMethod.GET, "/api/rental-contract/finished-with/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/rental-contract/**").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/rental-contract/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/rental-contract/**").authenticated()

						.requestMatchers(HttpMethod.POST, "/api/ads/*/report").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/ads/**").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/locations").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/user/*/reviews").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/user/*/ads").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/user/*/phone").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/user/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/reviews/contract-with/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/images/upload").authenticated()

						.requestMatchers(HttpMethod.GET, "/sitemap.xml").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/validate-email").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
					.requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
					.requestMatchers(HttpMethod.GET,  "/api/auth/ws-token").authenticated()


						.requestMatchers("/api/chat/**").authenticated()
						.requestMatchers("/api/notifications/**").authenticated()
						.requestMatchers("/api/push/**").authenticated()
						.requestMatchers("/ws/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/promotions/packages").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/promotions/ad/**").permitAll()
						.requestMatchers("/api/promotions/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/promotions/**").authenticated()


						.anyRequest().authenticated()
	            )
	            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
	
	    return http.build();
	}
}
