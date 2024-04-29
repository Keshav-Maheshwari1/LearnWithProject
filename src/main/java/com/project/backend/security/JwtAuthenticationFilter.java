package com.project.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
public class JwtAuthenticationFilter implements WebFilter {
    private final Logger logger =  LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private ReactiveUserDetailsService userDetailsService;


    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String requestHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        logger.info("Header: {}", requestHeader);
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            String token = requestHeader.substring(7);
            String email = jwtHelper.getUsernameFromToken(token);
            System.out.println(email);
            Boolean isValid = jwtHelper.validateToken(token,email);
            if(isValid){
                return userDetailsService.findByUsername(email)
                        .flatMap(userDetails -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            return ReactiveSecurityContextHolder.getContext()
                                    .map(securityContext -> {
                                        securityContext.setAuthentication(authentication);
                                        return authentication; // Return the authentication object
                                    })
                                    .then(chain.filter(exchange))
                                    .thenReturn(authentication);
                        }).contextWrite(ReactiveSecurityContextHolder.clearContext()).then();
            }else{
                return Mono.empty();
            }

        }else {
            return chain.filter(exchange);
        }
    }
    private Mono<Void> errorResponse(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap("Authentication failed".getBytes())));
    }

}
