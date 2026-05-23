package com.resumeparser.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RouterFunctions.route;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> candidateServiceRoute() {
        return route()
                .route(path("/api/candidates/**"), http())
                // Instead of a URI, we use the Load Balancer filter directly!
                .filter(lb("CANDIDATE-SERVICE"))
                .build()
                .and(route()
                        .route(path("/api/parser/**"), http())
                        .filter(lb("PARSER-SERVICE"))
                        .build());
    }
}
