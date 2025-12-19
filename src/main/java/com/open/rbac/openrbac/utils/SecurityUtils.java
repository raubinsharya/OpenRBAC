package com.open.rbac.openrbac.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.lang.Nullable;

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
}
