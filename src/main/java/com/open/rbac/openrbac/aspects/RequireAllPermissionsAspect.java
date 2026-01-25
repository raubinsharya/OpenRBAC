package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAllPermissions;
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
public class RequireAllPermissionsAspect {
    private final MeService meService;

    @Around("@annotation(requireAllPermissions)")
    public Object requireAllPermissions(ProceedingJoinPoint joinPoint, RequireAllPermissions requireAllPermissions)
            throws Throwable {
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

        // 3. Validate required permissions
        String[] requiredPermissions = requireAllPermissions.value();
        if (requiredPermissions.length == 0) {
            log.info("No permissions specified in @RequireAllPermissions annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking ALL permissions for user: {} against required permissions: {}", keycloakUserId,
                Arrays.toString(requiredPermissions));

        // 4. Check if user has ALL required permissions
        try {
            List<String> requiredPermissionsList = Arrays.asList(requiredPermissions);
            boolean hasAllRequiredPermissions = meService.hasAllPermissions(keycloakUserId, requiredPermissionsList);

            if (!hasAllRequiredPermissions) {
                log.info("Access denied: User '{}' is missing some of the required permissions: {}",
                        keycloakUserId, Arrays.toString(requiredPermissions));
                throw new AccessDeniedException(requireAllPermissions.message());
            }
        } catch (Exception e) {
            log.error("Failed to check permissions for user: {}", keycloakUserId, e);
            throw new AccessDeniedException("Failed to verify user permissions");
        }

        log.info("Access granted: User '{}' has ALL required permissions", keycloakUserId);
        return joinPoint.proceed();
    }
}