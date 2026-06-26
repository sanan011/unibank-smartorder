package az.unibank.smartorder.order.adapter.inbound.web.filter;

import az.unibank.smartorder.order.infrastructure.ratelimit.RedisRateLimiterService;
import az.unibank.smartorder.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService rateLimiterService;
    private final JwtTokenProvider tokenProvider;
    
    @Value("${rate-limit.limit:10}")
    private int limit;
    
    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    public RateLimitFilter(RedisRateLimiterService rateLimiterService, JwtTokenProvider tokenProvider) {
        this.rateLimiterService = rateLimiterService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled || isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = getClientIdentifier(request);
        long remaining = rateLimiterService.checkAndConsume(identifier, limit, windowSeconds);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        
        if (remaining >= 0) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            filterChain.doFilter(request, response);
        } else {
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests\", \"status\": 429}");
        }
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api/v1/auth"); // Exclude auth endpoints from regular IP rate limiting (should use specialized auth limiter)
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            try {
                var userId = tokenProvider.getUserIdFromJWT(jwt);
                if (userId != null) {
                    return userId.toString();
                }
            } catch (Exception ignored) {
                // Ignore invalid tokens, fallback to IP
            }
        }
        
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
