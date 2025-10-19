package com.example.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingMdcGlobalFilter implements GlobalFilter, Ordered {
  @Override
  public Mono<Void> filter(
      ServerWebExchange exchange,
      org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
    ServerHttpRequest req = exchange.getRequest();
    String rid = req.getHeaders().getFirst("X-Request-Id");
    if (rid != null) MDC.put("requestId", rid);
    return chain.filter(exchange).doFinally(sig -> MDC.clear());
  }

  @Override
  public int getOrder() {
    return -50;
  }
}
