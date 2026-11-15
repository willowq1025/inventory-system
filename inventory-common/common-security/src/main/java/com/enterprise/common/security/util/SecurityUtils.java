package com.enterprise.common.security.util;

import com.enterprise.common.core.context.CurrentUser;
import com.enterprise.common.core.context.UserContext;
import com.enterprise.common.security.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) return user;
        CurrentUser contextUser = UserContext.get();
        return contextUser instanceof LoginUser user ? user : null;
    }

    public static CurrentUser currentUser() {
        LoginUser user = getLoginUser();
        return user != null ? user : UserContext.get();
    }

    public static Long userId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getUserId();
    }

    public static String username() {
        LoginUser user = getLoginUser();
        return user == null ? "system" : user.getUsername();
    }

    public static Long deptId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getDeptId();
    }

    public static boolean hasPermission(String permission) {
        LoginUser user = getLoginUser();
        return user != null && user.hasPermission(permission);
    }
}
