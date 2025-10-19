package com.example.common.web.support;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    try {
      if (req.getHeader("X-Request-Id") == null)
        res.addHeader("X-Request-Id", UUID.randomUUID().toString());
      chain.doFilter(req, res);
    } finally {
    }
  }
}
