package org.landm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

	private JwtFilter jwtFilter;
	
	public SecurityConfig(JwtFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}
	
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/user/register").permitAll()
//                        .anyRequest().authenticated());
//
//        return http.build();
//    }
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http.csrf(csrf -> csrf.disable())
	            .authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/user/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.PUT, "/api/ads/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/ads/**").authenticated()

						.requestMatchers(HttpMethod.POST, "/api/rental-contract").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/rental-contract").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/rental-contract").authenticated()

						.requestMatchers(HttpMethod.POST, "/api/rental-contract/**").authenticated()
						.requestMatchers(HttpMethod.PATCH, "/api/rental-contract/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/rental-contract/**").authenticated()

						.requestMatchers(HttpMethod.DELETE, "/api/ads/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "api/admin/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/user/*/reviews").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/user/*/ads").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/user/*/phone").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/user/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()


						.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/images/upload").authenticated()

						.requestMatchers(HttpMethod.GET, "/api/auth/validate-email").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/reviews").authenticated()

						.requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()


						.anyRequest().authenticated()
	            ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
	
	    return http.build();
	}
}
