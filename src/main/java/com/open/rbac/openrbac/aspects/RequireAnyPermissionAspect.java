package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAnyPermission;
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
public class RequireAnyPermissionAspect {
    private final MeService meService;

    @Around("@annotation(requireAnyPermission)")
    public Object requireAnyPermission(ProceedingJoinPoint joinPoint, RequireAnyPermission requireAnyPermission)
            throws Throwable {
        // 1. Authentication validation
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

        // 3. Validate required permissions
        String[] requiredPermissions = requireAnyPermission.value();
        if (requiredPermissions.length == 0) {
            log.info("No permissions specified in @RequireAnyPermission annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking ANY permission for user: {} against required permissions: {}", username,
                Arrays.toString(requiredPermissions));

        // 4. Check if user has ANY required permission
        try {
            List<String> requiredPermissionsList = Arrays.asList(requiredPermissions);
            boolean hasRequiredPermission = meService.hasAnyPermission(username, requiredPermissionsList);

            if (!hasRequiredPermission) {
                log.info("Access denied: User '{}' does not have any of the required permissions: {}",
                        username, Arrays.toString(requiredPermissions));
                throw new AccessDeniedException(requireAnyPermission.message());
            }
        } catch (Exception e) {
            log.error("Failed to check permissions for user: {}", username, e);
            throw new AccessDeniedException("Failed to verify user permissions");
        }

        log.info("Access granted: User '{}' has required permission(s)", username);
        return joinPoint.proceed();
    }
}