package com.nasnav.security;

import com.nasnav.exceptions.ErrorCodes;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.SecurityService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private SecurityService securityService;

    @Before("@annotation(com.nasnav.security.HasPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        HasPermission hasPermission = method.getAnnotation(HasPermission.class);
        if (hasPermission != null) {
            String permissionName = hasPermission.value().name();
            boolean hasAccess = securityService.hasPermission(permissionName);

            if (!hasAccess) {
                throw new RuntimeBusinessException(HttpStatus.UNAUTHORIZED, ErrorCodes.UAUTH$0002);
            }
        }
    }
}
