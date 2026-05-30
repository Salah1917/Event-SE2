package com.university.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .uri("lb://user-service"))
                .route("event-service", r -> r
                        .path("/api/events/**")
                        .uri("lb://event-service"))
                .route("registration-service", r -> r
                        .path("/api/registrations/**", "/api/participants/**")
                        .uri("lb://registration-service"))
                .build();
    }
}
