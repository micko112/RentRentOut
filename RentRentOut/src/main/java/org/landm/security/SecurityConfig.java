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
	                    // Proširujemo listu dozvoljenih putanja
	                    .requestMatchers("/api/user/register",
	                            "/api/user/login",
	                            "/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/ads/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/ads").authenticated()
						.requestMatchers(HttpMethod.PUT, "/api/ads/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/ads/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "api/admin/**").hasRole("ADMIN")

						.anyRequest().authenticated()
	            ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
	
	    return http.build();
	}
}
