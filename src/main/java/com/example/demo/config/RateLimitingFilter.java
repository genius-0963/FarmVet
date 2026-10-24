package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 120;
    private final ConcurrentHashMap<String, ClientWindow> clientWindows = new ConcurrentHashMap<>();

    private static class ClientWindow {
        long windowStart;
        AtomicInteger requestCount;

        ClientWindow(long start) {
            this.windowStart = start;
            this.requestCount = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        ClientWindow window = clientWindows.compute(clientIp, (ip, currentWindow) -> {
            if (currentWindow == null || (now - currentWindow.windowStart > 60_000)) {
                return new ClientWindow(now);
            }
            currentWindow.requestCount.incrementAndGet();
            return currentWindow;
        });

        if (window.requestCount.get() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded. Try again in 60 seconds.\",\"errors\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
