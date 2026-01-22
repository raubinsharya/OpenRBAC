package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAnyRole;
import com.open.rbac.openrbac.services.MeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequireAnyRoleAspect {
    private final MeService meService;

    @Around("@annotation(requireAnyRole)")
    public Object requireAnyRole(ProceedingJoinPoint joinPoint, RequireAnyRole requireAnyRole) throws Throwable {
        // 1. Authentication validation (keep existing logic)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("Access denied: User not authenticated");
            throw new AccessDeniedException("User not authenticated");
        }

        // 2. Extract username with null check
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.trim().isEmpty()) {
            log.warn("Access denied: Username not found in JWT token");
            throw new AccessDeniedException("Username not found in token");
        }

        // 3. Validate required roles
        String[] requiredRoles = requireAnyRole.value();
        if (requiredRoles.length == 0) {
            log.info("No roles specified in @RequireAnyRole annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking roles for user: {} against required roles: {}", username, Arrays.toString(requiredRoles));

        // 4. Check if user has any of the required roles
        try {
            List<String> requiredRolesList = Arrays.asList(requiredRoles);
            boolean hasRequiredRole = meService.hasAnyRole(username, requiredRolesList);

            if (!hasRequiredRole) {
                log.info("Access denied: User '{}' does not have any of the required roles: {}",
                        username, Arrays.toString(requiredRoles));
                throw new AccessDeniedException(requireAnyRole.message());
            }
        } catch (Exception e) {
            log.error("Failed to check roles for user: {}", username, e);
            throw new AccessDeniedException("Failed to verify user roles");
        }
        log.info("Access granted: User '{}' has required role(s)", username);
        return joinPoint.proceed();
    }
}
