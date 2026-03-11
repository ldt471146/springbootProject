package com.aspect;

import com.annotation.RequireRole;
import com.common.enums.RoleEnum;
import com.exception.BusinessException;
import com.security.AuthUser;
import com.security.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RoleAspect {

    @Around("@annotation(requireRole)")
    public Object around(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        AuthUser currentUser = UserContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }
        boolean matched = Arrays.stream(requireRole.value())
                .map(RoleEnum::name)
                .anyMatch(role -> role.equals(currentUser.getRole()));
        if (!matched) {
            throw new BusinessException(403, "无权限访问");
        }
        return joinPoint.proceed();
    }
}
