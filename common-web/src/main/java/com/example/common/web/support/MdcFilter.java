package com.example.common.web.support;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MdcFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    try {
      String rid = req.getHeader("X-Request-Id");
      if (rid != null && !rid.isBlank()) MDC.put("requestId", rid);
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getName() != null) MDC.put("userId", auth.getName());
      String bid = req.getHeader("X-Booking-Id");
      if (bid != null && !bid.isBlank()) MDC.put("bookingId", bid);
      chain.doFilter(req, res);
    } finally {
      MDC.clear();
    }
  }
}
