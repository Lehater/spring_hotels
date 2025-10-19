package com.example.gateway.filter;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestIdGatewayFilter implements GatewayFilter, Ordered {
  public static final String HEADER = "X-Request-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    var headers = exchange.getRequest().getHeaders();
    if (!headers.containsKey(HEADER)) {
      var mutated =
          exchange
              .getRequest()
              .mutate()
              .header(HEADER, UUID.randomUUID().toString())
              .headers(h -> h.remove(HttpHeaders.COOKIE))
              .build();
      return chain.filter(exchange.mutate().request(mutated).build());
    }
    var mutated = exchange.getRequest().mutate().headers(h -> h.remove(HttpHeaders.COOKIE)).build();
    return chain.filter(exchange.mutate().request(mutated).build());
  }

  @Override
  public int getOrder() {
    return -100;
  }
}
