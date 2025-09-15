package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAllPermissions;
import com.open.rbac.openrbac.dtos.PermissionDTO;
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
import java.util.Set;
import java.util.stream.Collectors;

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

        // 2. Extract username with null check
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.trim().isEmpty()) {
            log.warn("Access denied: Username not found in JWT token");
            throw new AccessDeniedException("Username not found in token");
        }

        // 3. Validate required permissions
        String[] requiredPermissions = requireAllPermissions.value();
        if (requiredPermissions.length == 0) {
            log.info("No permissions specified in @RequireAllPermissions annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking ALL permissions for user: {} against required permissions: {}", username,
                Arrays.toString(requiredPermissions));

        // 4. Get user permissions (all permissions, not paginated)
        List<PermissionDTO> userPermissions;
        try {
            userPermissions = meService.getMePermissions(username);
        } catch (Exception e) {
            log.error("Failed to retrieve permissions for user: {}", username, e);
            throw new AccessDeniedException("Failed to retrieve user permissions");
        }

        if (userPermissions == null || userPermissions.isEmpty()) {
            log.info("Access denied: User '{}' has no permissions assigned", username);
            throw new AccessDeniedException(requireAllPermissions.message());
        }

        // 5. Parse required permissions and check if user has ALL (AND logic)
        Set<String> userPermissionStrings = userPermissions.stream()
                .filter(p -> p.resource() != null && p.action() != null)
                .map(p -> p.resource() + ":" + p.action())
                .collect(Collectors.toSet());

        Set<String> requiredPermissionSet = Arrays.stream(requiredPermissions)
                .filter(perm -> perm != null && !perm.trim().isEmpty())
                .collect(Collectors.toSet());

        boolean hasAllRequiredPermissions = userPermissionStrings.containsAll(requiredPermissionSet);

        if (!hasAllRequiredPermissions) {
            Set<String> missingPermissions = requiredPermissionSet.stream()
                    .filter(perm -> !userPermissionStrings.contains(perm))
                    .collect(Collectors.toSet());

            log.info("Access denied: User '{}' with permissions {} is missing required permissions: {}",
                    username, userPermissionStrings, missingPermissions);
            throw new AccessDeniedException(requireAllPermissions.message());
        }

        log.info("Access granted: User '{}' has ALL required permissions", username);
        return joinPoint.proceed();
    }
}