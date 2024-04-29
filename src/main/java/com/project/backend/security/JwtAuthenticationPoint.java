package com.project.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtAuthenticationPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        Mono<Void> voidMono = exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(("Access denied" + ex.getMessage()).getBytes())));
        return voidMono;
    }
}
