package com.example.assistant.utils;

import com.example.assistant.entity.User;
import com.example.assistant.mapper.UserMapper;
import com.example.assistant.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public final class SecurityUtils {

    private static UserMapper userMapper;

    public SecurityUtils(UserMapper userMapper) {
        SecurityUtils.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        User user = userMapper.selectByUsername(username);
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(role) || auth.equals("ROLE_" + role));
    }

    /**
     * 判断当前用户是否是超级管理员
     */
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * 获取当前用户所有权限标识符
     */
    public static Set<String> getAllPerms(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> collect = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return collect;
    }
}
