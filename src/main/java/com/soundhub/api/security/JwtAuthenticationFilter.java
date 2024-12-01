package com.soundhub.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundhub.api.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> PUBLIC_ENDPOINTS = List.of("/api/v1/auth", "/api/v1/users/checkUser", "/api/v1/genres", "/swagger-ui", "/v3/api-docs", "/ws");
    private final JwtService jwtService;
    private final BlacklistingService blacklistingService;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter method to process incoming requests and handle JWT authentication.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (isPermittedEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = extractTokenFromHeader(request.getHeader(Constants.AUTHORIZATION_HEADER_NAME));
        if (isTokenInvalid(jwt, response)) {
            return;
        }

        authenticateJwtToken(jwt, request, response);
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the endpoint does not require authentication.
     */
    private boolean isPermittedEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream().anyMatch(uri::startsWith);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     */
    private String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith(Constants.BEARER_PREFIX)) {
            return header.substring(Constants.BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Checks if the token is null or blacklisted, and sends an error response if invalid.
     */
    private boolean isTokenInvalid(String jwt, HttpServletResponse response) throws IOException {
        try {
            if (jwt == null || isTokenBlacklisted(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return true;
            }
        } catch (RedisConnectionFailureException e) {
            sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Redis connection failure");
            return true;
        }
        return false;
    }

    /**
     * Authenticates JWT token and sets the authentication in the security context if valid.
     */
    private void authenticateJwtToken(String jwt, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = jwtService.extractUsername(jwt);
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
        }
    }

    /**
     * Checks if the token is blacklisted in Redis.
     */
    private boolean isTokenBlacklisted(String jwt) throws RedisConnectionFailureException {
        return blacklistingService.getJwtBlacklist(jwt) != null;
    }

    /**
     * Sends an error response in JSON format.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        ProblemDetail errorResponse = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), message);
        PrintWriter writer = response.getWriter();
        response.setStatus(status);
        writer.write(MAPPER.writeValueAsString(errorResponse));
        writer.flush();
    }
}
