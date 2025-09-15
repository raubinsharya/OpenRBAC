package com.open.rbac.openrbac.aspects;

import com.open.rbac.openrbac.annotations.RequireAllRoles;
import com.open.rbac.openrbac.dtos.RoleDTO;
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

        // 2. Extract username with null check
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.trim().isEmpty()) {
            log.warn("Access denied: Username not found in JWT token");
            throw new AccessDeniedException("Username not found in token");
        }

        // 3. Validate required roles
        String[] requiredRoles = requireAllRoles.value();
        if (requiredRoles.length == 0) {
            log.info("No roles specified in @RequireAllRoles annotation, allowing access");
            return joinPoint.proceed();
        }

        log.info("Checking ALL roles for user: {} against required roles: {}", username, Arrays.toString(requiredRoles));

        // 4. Get user roles
        List<RoleDTO> userRoles;
        try {
            userRoles = meService.getMeRoles(username);
        } catch (Exception e) {
            log.error("Failed to retrieve roles for user: {}", username, e);
            throw new AccessDeniedException("Failed to retrieve user roles");
        }

        if (userRoles == null || userRoles.isEmpty()) {
            log.info("Access denied: User '{}' has no roles assigned", username);
            throw new AccessDeniedException(requireAllRoles.message());
        }

        // 5. Check if user has ALL required roles (AND logic)
        Set<String> userRoleNames = userRoles.stream()
                .map(RoleDTO::name)
                .filter(name -> name != null && !name.trim().isEmpty())
                .collect(Collectors.toSet());

        Set<String> requiredRoleSet = Arrays.stream(requiredRoles)
                .filter(role -> role != null && !role.trim().isEmpty())
                .collect(Collectors.toSet());

        boolean hasAllRequiredRoles = userRoleNames.containsAll(requiredRoleSet);

        if (!hasAllRequiredRoles) {
            Set<String> missingRoles = requiredRoleSet.stream()
                    .filter(role -> !userRoleNames.contains(role))
                    .collect(Collectors.toSet());
            
            log.info("Access denied: User '{}' with roles {} is missing required roles: {}", 
                    username, userRoleNames, missingRoles);
            throw new AccessDeniedException(requireAllRoles.message());
        }

        log.info("Access granted: User '{}' has ALL required roles", username);
        return joinPoint.proceed();
    }
}