package az.unibank.smartorder.order.adapter.inbound.web.filter;

import az.unibank.smartorder.order.infrastructure.ratelimit.RedisRateLimiterService;
import az.unibank.smartorder.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisRateLimiterService rateLimiterService;
    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redisTemplate;
    
    @Value("${rate-limit.limit:10}")
    private int limit;
    
    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${security.trusted-proxy-cidrs:127.0.0.1}")
    private List<String> trustedProxyCidrs;

    public RateLimitFilter(RedisRateLimiterService rateLimiterService, JwtTokenProvider tokenProvider, StringRedisTemplate redisTemplate) {
        this.rateLimiterService = rateLimiterService;
        this.tokenProvider = tokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (("POST".equalsIgnoreCase(request.getMethod())) && 
            (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register"))) {
            
            String ip = extractClientIp(request);
            String key = "rate_limit:auth:" + ip;
            Long count = redisTemplate.opsForValue().increment(key);
            
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(60));
            }
            
            if (count != null && count > 5) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many login attempts\", \"status\": 429}");
                return;
            }
        }

        if (isExcludedPath(path)) {
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
               path.startsWith("/api/v1/auth"); // Auth handled separately
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
        return extractClientIp(request);
    }

    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        
        boolean isTrustedProxy = false;
        for (String cidr : trustedProxyCidrs) {
            try {
                IpAddressMatcher matcher = new IpAddressMatcher(cidr);
                if (matcher.matches(remoteAddr)) {
                    isTrustedProxy = true;
                    break;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid CIDR format, ignore
            }
        }
        
        if (isTrustedProxy) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(xfHeader)) {
                return xfHeader.split(",")[0].trim();
            }
        }
        
        return remoteAddr;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
