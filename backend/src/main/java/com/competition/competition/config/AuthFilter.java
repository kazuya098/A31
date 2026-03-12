package com.competition.competition.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 校验请求头 X-Auth-Token，将当前用户 id 放入 request 属性，供 Controller 使用。
 * 白名单路径不校验 token。
 */
@Order(1)
public class AuthFilter extends OncePerRequestFilter {

    private static final String HEADER_TOKEN = "X-Auth-Token";
    private static final String ATTR_USER_ID = "currentUserId";

    private final AuthTokenStore tokenStore;
    private final List<String> permitPaths = List.of("/api/auth/login", "/api/health");

    public AuthFilter(AuthTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (permitPaths.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = request.getHeader(HEADER_TOKEN);
        Long userId = token != null ? tokenStore.getUserId(token) : null;
        if (userId != null) {
            request.setAttribute(ATTR_USER_ID, userId);
        }
        filterChain.doFilter(request, response);
    }
}
