package com.bsicenter.crm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Server-generated correlation IDs; never logs headers, tokens or request bodies. */
@Component("bsiRequestContextFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter extends OncePerRequestFilter {
  public static final String ATTRIBUTE = "bsi.requestId";
  private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String id = UUID.randomUUID().toString();
    request.setAttribute(ATTRIBUTE, id);
    response.setHeader("X-Request-ID", id);
    if (request.getRequestURI().startsWith("/api/"))
      response.setHeader("Cache-Control", "no-store");
    long started = System.nanoTime();
    MDC.put("requestId", id);
    try {
      chain.doFilter(request, response);
    } finally {
      if (response.getStatus() >= 500) {
        log.error(
            "Request failed method={} status={} elapsedMs={} requestId={}",
            request.getMethod(),
            response.getStatus(),
            (System.nanoTime() - started) / 1_000_000,
            id);
      }
      MDC.remove("requestId");
    }
  }
}
