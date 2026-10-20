package cst438.project2.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthConverter jwtAuthConv
  ) throws Exception {

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )
        .authorizeHttpRequests(auth -> auth
            // Put Public routes here, the permit all allows them to be accessed without authentication
            .requestMatchers(
                HttpMethod.GET,
                "/api/v1/events",
                "/api/v1/events/*",
                "/api/v1/events/{eventId}/totalAttendees",
                "/api/v1/locations",
                "/api/v1/locations/*"
            ).permitAll()
            // .authenticated = needs auth
            .requestMatchers("/auth/me").authenticated()
            // this makes all requests require auth, except those stated in .permitAll
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth ->
            oauth.jwt(jwt ->
                jwt.jwtAuthenticationConverter(
                    jwtAuthConv
                )
            )
        );

    return http.build();
  }
}