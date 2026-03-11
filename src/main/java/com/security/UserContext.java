package com.security;

public final class UserContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(AuthUser user) {
        HOLDER.set(user);
    }

    public static AuthUser getCurrentUser() {
        return HOLDER.get();
    }

    public static Long getCurrentUserId() {
        AuthUser user = HOLDER.get();
        return user == null ? null : user.getId();
    }

    public static String getCurrentRole() {
        AuthUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
