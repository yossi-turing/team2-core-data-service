package com.therapy.coredataservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * הפרדת אימות בין חוזה 1 לחוזה 5:
 * <ul>
 *   <li>{@code /api/v1/auth/**} ו-Swagger: פתוחים.</li>
 *   <li>{@code /api/v1/**}: JWT עם ROLE_THERAPIST.</li>
 *   <li>{@code /internal/api/v1/**}: Header {@code X-Internal-Service-Key} בלבד.</li>
 *   <li>כל השאר: חסום.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_DOC_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   SecurityErrorWriter errorWriter,
                                                   @Value("${app.security.internal-service-key}") String internalServiceKey)
            throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);
        InternalApiKeyFilter internalKeyFilter = new InternalApiKeyFilter(internalServiceKey, errorWriter);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> { })
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_DOC_PATHS).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/internal/api/v1/**").hasAuthority(InternalApiKeyFilter.ROLE_INTERNAL_SERVICE)
                        .requestMatchers("/api/v1/**").hasAuthority(JwtService.ROLE_THERAPIST)
                        .anyRequest().denyAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                errorWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                                        "Authentication required: missing, invalid or expired token"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                errorWriter.write(request, response, HttpStatus.FORBIDDEN,
                                        "Access denied for this resource")))
                .addFilterBefore(internalKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
