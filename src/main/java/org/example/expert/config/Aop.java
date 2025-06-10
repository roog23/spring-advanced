package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class Aop {

    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object aopLogging(ProceedingJoinPoint joinPoint) throws Throwable {
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
