package com.neilan.control.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(Environment environment) {
        boolean isLocal = Arrays.asList(environment.getActiveProfiles()).contains("local");

        CorsConfiguration config = new CorsConfiguration();
        if (isLocal) {
            config.setAllowedOrigins(List.of(
                    "http://127.0.0.1:5500",
                    "http://localhost:5500",
                    "http://127.0.0.1:3000",
                    "http://localhost:3000",
                    "http://127.0.0.1:8090",
                    "http://localhost:8090"
            ));
        } else {
            config.setAllowedOriginPatterns(List.of(
                    "https://*.vercel.app",
                    "https://neilan-control.vercel.app"
            ));
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
