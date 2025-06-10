package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class Aop {

    private final JwtUtil jwtUtil;

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object aopLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new NullPointerException();
        }

        // URL 로깅
        HttpServletRequest request = attributes.getRequest();
        String requestURI = request.getRequestURI();
        // 시간 로깅
        LocalDateTime accessTime = LocalDateTime.now();

        // 유저 id 로깅
        String bearerJwt = request.getHeader("Authorization");
        String jwt = jwtUtil.substringToken(bearerJwt);
        Claims claims = jwtUtil.extractClaims(jwt);
        String userId = claims.getSubject();

        log.info("userId : {}, start : {}, URL : {}", userId, accessTime, requestURI);

        // requestBody 로깅
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            log.info("Request Body: {}", new ObjectMapper().writeValueAsString(arg));
        }
        // 매소드를 실행합니다.
        Object result = joinPoint.proceed();
        // responseBody 로깅
        log.info("Response Body: {}", new ObjectMapper().writeValueAsString(result));
        return result;
    }
}
