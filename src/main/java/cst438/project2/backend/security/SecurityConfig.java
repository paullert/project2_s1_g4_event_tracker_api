package cst438.project2.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import cst438.project2.backend.exceptions.ApiProblem;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthConverter jwtAuthConv,
      ObjectMapper mapper
  ) throws Exception {

    AuthenticationEntryPoint unauthenticated = (request, response, ex) -> {
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
      ApiProblem.write(HttpStatus.UNAUTHORIZED, "A valid bearer token is required", request, response, mapper);
    };
    AccessDeniedHandler forbidden = (request, response, ex) ->
        ApiProblem.write(HttpStatus.FORBIDDEN, "You are not allowed to perform this operation",
            request, response, mapper);

    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        )
        .exceptionHandling(errors -> errors.authenticationEntryPoint(unauthenticated)
            .accessDeniedHandler(forbidden))
        .authorizeHttpRequests(auth -> auth
            // Put Public routes here, the permit all allows them to be accessed without authentication
            .requestMatchers(
                HttpMethod.GET,
                "/api/v1/events",
                "/api/v1/events/*",
                "/api/v1/events/{eventId}/totalAttendees",
                "/api/v1/locations",
                "/api/v1/locations/*",
                "/api/v1",
                "/api/v1/index.html"
            ).permitAll()
            // .authenticated = needs auth
            .requestMatchers("/auth/me").authenticated()
            // this makes all requests require auth, except those stated in .permitAll
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth ->
            oauth.authenticationEntryPoint(unauthenticated).accessDeniedHandler(forbidden).jwt(jwt ->
                jwt.jwtAuthenticationConverter(
                    jwtAuthConv
                )
            )
        );

    return http.build();
  }
}
