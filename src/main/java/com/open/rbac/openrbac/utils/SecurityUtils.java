package com.open.rbac.openrbac.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SecurityUtils {

    /**
     * Extracts information from the JWT of the authenticated user using the
     * provided mapper function.
     * 
     * @param <T>       The return type of the mapper function
     * @param jwtMapper A function that takes the JWT object and returns an object
     *                  of type T
     * @return The result of the mapper function, or null if no authenticated user
     *         is found
     */
    @Nullable
    public static <T> T getAuthenticatedUser(Function<Jwt, T> jwtMapper) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwtMapper.apply(jwt);
        }
        return null;
    }

    /**
     * Extracts the Keycloak realm name from the JWT issuer claim.
     * Expected issuer format: http://<host>/realms/{realmName}
     *
     * @param jwt the authenticated JWT
     * @return the realm name, or null if the issuer is missing/malformed
     */
    @Nullable
    public static String extractRealmFromJwt(Jwt jwt) {
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        if (issuer == null) return null;
        String[] parts = issuer.split("/realms/");
        if (parts.length < 2 || parts[1].isBlank()) return null;
        return parts[1];
    }

    /**
     * Returns true if the JWT contains "realm_admin" inside the
     * {@code realm_access.roles} claim.
     *
     * @param jwt the authenticated JWT
     * @return true when the user carries the realm_admin role
     */
    public static boolean isRealmAdmin(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return false;
        Object roles = realmAccess.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.contains("realm_admin");
        }
        return false;
    }
}
