package com.afrudeen.gateway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey key;
    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        HttpMethod method = req.getMethod();

        // 1) never trust identity headers sent by a client
        ServerHttpRequest clean = req.mutate()
                .headers(h -> { h.remove("X-User-Id"); h.remove("X-User-Role"); }).build();
        ServerWebExchange cleanExchange = exchange.mutate().request(clean).build();

        // 2) open endpoints (CORS preflight, login, register, docs, health)
        if (HttpMethod.OPTIONS.equals(method) || isOpen(path)) {
            return chain.filter(cleanExchange);
        }

        // 3) require "Authorization: Bearer <token>"
        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        }

        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload(); // checks signature + exp
            String role = claims.get("role", String.class);
            Number userId = claims.get("userId", Number.class);

            // 4) role rule: only ADMIN may change products
            if (path.startsWith("/products") && !HttpMethod.GET.equals(method)
                    && !"ADMIN".equals(role)) {
                return reject(exchange, HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest withUser = clean.mutate()
                    .header("X-User-Id", String.valueOf(userId.longValue()))
                    .header("X-User-Role", role).build();
            return chain.filter(cleanExchange.mutate().request(withUser).build());

        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, HttpStatus.UNAUTHORIZED); // bad or expired token
        }

    }

    private boolean isOpen(String path) {

        return path.equals("/users/login") || path.equals("/users/register")
                || path.startsWith("/swagger-ui") || path.contains("/v3/api-docs")
                || path.startsWith("/webjars") || path.startsWith("/actuator/health");

    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {

        exchange.getResponse().setStatusCode(status);

        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    } // run before routing filters
}