package com.keepy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // It's good practice to keep your frontend URL in application.properties
    // e.g., app.frontend.url=http://localhost:3000
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Apply to all endpoints
                .allowedOrigins(frontendUrl) // Allow your Next.js app
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP verbs
                .allowedHeaders("*") // Allow all headers (Authorization, Content-Type, etc.)
                .allowCredentials(true) // Required for cookies or certain auth headers
                .maxAge(3600); // Cache the CORS response for 1 hour
    }
}