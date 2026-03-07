package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAllRoles;
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
public class RequireAllRolesAspect {
    private final MeService meService;

    @Around("@annotation(requireAllRoles)")
    public Object requireAllRoles(ProceedingJoinPoint joinPoint, RequireAllRoles requireAllRoles) throws Throwable {
        // 1. Authentication validation
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("Access denied: User not authenticated");
            throw new AccessDeniedException("User not authenticated");
        }

        // 2. Extract user ID with null check
        String keycloakUserId = jwt.getSubject();
        if (keycloakUserId == null || keycloakUserId.trim().isEmpty()) {
            log.warn("Access denied: Subject not found in JWT token");
            throw new AccessDeniedException("Subject not found in token");
        }

        // 3. Validate required roles
        String[] requiredRoles = requireAllRoles.value();
        if (requiredRoles.length == 0) {
            log.info("No roles specified in @RequireAllRoles annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking ALL roles for user: {} against required roles: {}", keycloakUserId,
                Arrays.toString(requiredRoles));

        // 4. Check if user has ALL required roles
        try {
            List<String> requiredRolesList = Arrays.asList(requiredRoles);
            boolean hasAllRequiredRoles = meService.hasAllRoles(keycloakUserId, requiredRolesList);

            if (!hasAllRequiredRoles) {
                log.info("Access denied: User '{}' does not have ALL required roles: {}",
                        keycloakUserId, Arrays.toString(requiredRoles));
                throw new AccessDeniedException(requireAllRoles.message());
            }
        } catch (Exception e) {
            log.error("Failed to check roles for user: {}", keycloakUserId, e);
            throw new AccessDeniedException("Failed to verify user roles");
        }

        log.info("Access granted: User '{}' has ALL required roles", keycloakUserId);
        return joinPoint.proceed();
    }
}