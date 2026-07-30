package com.ahmadisyraf39.spabs_v2.security.jwt;

import com.ahmadisyraf39.spabs_v2.common.dto.ErrorResponse;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS =
            Set.of("/api/v1/auth/change-password", "/api/v1/auth/me");

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAllowedWhileMustChange = ALLOWED_PATHS.contains(request.getRequestURI());

        if (!isAllowedWhileMustChange
                && authentication != null
                && authentication.getPrincipal() instanceof UserPrincipal principal
                && principal.getUser().isMustChangePassword()) {
            ErrorResponse body = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message("Password change required before continuing")
                    .timestamp(LocalDateTime.now())
                    .build();
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
