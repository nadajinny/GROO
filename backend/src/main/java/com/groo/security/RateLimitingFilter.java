package com.groo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groo.common.ApiResponse;
import com.groo.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final Map<String, RequestWindow> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();
        if (isAllowed(key)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> apiResponse = ApiResponse.error(
                "Too many requests. Please try again later.",
                "TOO_MANY_REQUESTS");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private boolean isAllowed(String key) {
        RequestWindow window = counters.computeIfAbsent(key, k -> new RequestWindow());
        synchronized (window) {
            Instant now = Instant.now();
            if (now.isAfter(window.windowStart.plusSeconds(properties.getWindowSeconds()))) {
                window.windowStart = now;
                window.counter.set(0);
            }
            if (window.counter.incrementAndGet() > properties.getMaxRequests()) {
                return false;
            }
        }
        return true;
    }

    private static final class RequestWindow {
        private Instant windowStart = Instant.now();
        private final AtomicInteger counter = new AtomicInteger(0);
    }
}
