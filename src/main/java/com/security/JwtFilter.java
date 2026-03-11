package com.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.Constants;
import com.common.Result;
import com.common.enums.UserStatus;
import com.entity.User;
import com.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Set<String> OPEN_API = Set.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (shouldSkip(request, uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader(Constants.AUTH_HEADER);
        if (authorization == null || !authorization.startsWith(Constants.AUTH_PREFIX)) {
            writeUnauthorized(response, "缺少登录令牌");
            return;
        }

        try {
            String token = authorization.substring(Constants.AUTH_PREFIX.length());
            Claims claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getId, userId)
                    .eq(User::getDeleted, 0));
            if (user == null || !UserStatus.ACTIVE.name().equals(user.getStatus())) {
                writeUnauthorized(response, "账号不可用");
                return;
            }
            UserContext.setCurrentUser(new AuthUser(user.getId(), user.getUsername(), user.getRole()));
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            writeUnauthorized(response, "令牌无效或已过期");
        } finally {
            UserContext.clear();
        }
    }

    private boolean shouldSkip(HttpServletRequest request, String uri) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !uri.startsWith("/api")
                || OPEN_API.contains(uri)
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/doc.html");
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, message)));
    }
}
