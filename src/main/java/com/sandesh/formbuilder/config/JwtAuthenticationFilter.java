package com.sandesh.formbuilder.config;


import com.sandesh.formbuilder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // The endpoints listed below won't have jwt filter
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {



        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;


        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Missing or invalid Authorization header");
            return;
        }


        jwt = authorizationHeader.substring(7);
        try {
            username = jwtUtil.getUsernameFromToken(jwt);
        } catch (Exception e) {
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid or expired JWT token");
            return;
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid or expired JWT token");
                return;
            }
        } else if (username == null) {
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid JWT token: Unable to extract username");
            return;
        }

        chain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
                String.format("{\"success\": false, \"status\": \"%s\", \"message\": \"%s\", \"data\": null, \"errors\": [\"%s\"]}",
                        status.getReasonPhrase(), message, message)
        );
    }
}