package com.app.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

/**
 * Helper for audit fields: who created/updated an entity.
 * Returns "ADMIN:username" or "EMPLOYEE:username" for display and storage.
 */
public final class AuditHelper {

    private AuditHelper() {}

    /**
     * Returns the current user as an audit string, or null if unauthenticated.
     * Format: "ADMIN:username" or "EMPLOYEE:username".
     */
    public static String currentUserAuditString() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) return null;
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        if (authorities != null) {
            for (GrantedAuthority a : authorities) {
                String role = a.getAuthority();
                if ("ROLE_ADMIN".equals(role) || "ROLE_SUPER_ADMIN".equals(role)) {
                    return "ADMIN:" + username;
                }
                if ("ROLE_EMPLOYEE".equals(role)) {
                    return "EMPLOYEE:" + username;
                }
            }
        }
        return "USER:" + username;
    }

    /**
     * Returns a display-friendly label for an audit string, e.g. "Admin (admin1)" or "Employee PGABC123".
     */
    public static String formatAuditForDisplay(String auditString) {
        if (auditString == null || auditString.isBlank()) return "—";
        int colon = auditString.indexOf(':');
        if (colon <= 0 || colon == auditString.length() - 1) return auditString;
        String kind = auditString.substring(0, colon);
        String name = auditString.substring(colon + 1);
        return switch (kind.toUpperCase()) {
            case "ADMIN" -> "Admin (" + name + ")";
            case "EMPLOYEE" -> "Employee " + name;
            case "USER" -> name;
            default -> auditString;
        };
    }
}
