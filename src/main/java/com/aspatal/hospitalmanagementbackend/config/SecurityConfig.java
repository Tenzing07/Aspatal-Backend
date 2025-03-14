package com.aspatal.hospitalmanagementbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.aspatal.hospitalmanagementbackend.util.JwtAuthenticationFilter;
import com.aspatal.hospitalmanagementbackend.util.JwtUtil;

import java.util.List;

/**
 * Security configuration class for managing authentication and authorization in the system.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * Constructor-based dependency injection for JwtUtil.
     * @param jwtUtil Utility class for handling JWT operations.
     */
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Configures password encoding using BCrypt.
     * This ensures that passwords are securely hashed before being stored.
     * @return BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain, setting up authentication and authorization rules.
     * @param http HttpSecurity object for defining security configurations.
     * @return Configured SecurityFilterChain.
     * @throws Exception if any error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enables CORS (Cross-Origin Resource Sharing) for frontend communication.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Disables CSRF (Cross-Site Request Forgery) protection (since we use JWT authentication).
                .csrf(csrf -> csrf.disable())

                // Defines authorization rules for different API endpoints.
                .authorizeHttpRequests(auth -> auth
                        // Allows unauthenticated access to authentication-related and chat endpoints.
                        .requestMatchers("/api/auth/**", "/chat/**").permitAll()

                        // Restricts admin endpoints to users with the ADMIN role.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Requires authentication for all other endpoints.
                        .anyRequest().authenticated())

                // Adds JWT authentication filter before the default authentication filter.
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures the authentication manager to manage user authentication.
     * @param authenticationConfiguration Authentication configuration provided by Spring Security.
     * @return Configured AuthenticationManager.
     * @throws Exception if any error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures CORS settings to allow frontend (Angular) to communicate with the backend.
     * @return Configured CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Specifies the frontend URL allowed to make requests.
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Defines the HTTP methods allowed.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Specifies allowed request headers.
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Allows credentials (e.g., cookies, authorization headers) for secure communication.
        configuration.setAllowCredentials(true);

        // Applies the configuration to all API endpoints.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
