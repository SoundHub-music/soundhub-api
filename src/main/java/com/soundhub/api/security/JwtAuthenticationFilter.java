package com.soundhub.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundhub.api.Constants;
import jakarta.annotation.Nullable;
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
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final BlacklistingService blacklistingService;
    private final UserDetailsService userDetailsService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Check if the route exists
        if (!isRouteExists(request)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
            return;
        }

        // Validate the Authorization header
        String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER_NAME);
        if (!isAuthHeaderValid(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = extractTokenFromHeader(authHeader);

        // Check if the token is blacklisted
        try {
            if (isTokenBlacklisted(jwt)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                filterChain.doFilter(request, response);
                return;
            }

        }
        catch (RedisConnectionFailureException e) {
            log.error("isTokenBlacklisted: Redis connection failure", e);
            sendErrorResponse(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Validate and authenticate the token
        authenticateJwtToken(jwt, request, response);

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    private void authenticateJwtToken(String jwt, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username;
        try {
            username = jwtService.extractUsername(jwt);
            log.debug("Authenticating user: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private boolean isAuthHeaderValid(@Nullable String authHeader) {
        return authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX);
    }

    private String extractTokenFromHeader(String header) {
        return header.substring(Constants.BEARER_PREFIX.length());
    }

    private boolean isTokenBlacklisted(String jwt) throws RedisConnectionFailureException {
        return blacklistingService.getJwtBlacklist(jwt) != null;
    }

    private boolean isRouteExists(HttpServletRequest request) {
        try {
            HandlerExecutionChain handlerChain = requestMappingHandlerMapping.getHandler(request);
            return handlerChain != null;
        } catch (Exception e) {
            log.error("Error while checking route existence", e);
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        ProblemDetail errorResponse = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), message);
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter writer = response.getWriter();

        response.setStatus(status);
        writer.write(mapper.writeValueAsString(errorResponse));
        writer.flush();
    }
}