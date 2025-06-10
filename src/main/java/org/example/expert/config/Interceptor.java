package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String bearerJwt = request.getHeader("Authorization");
        String jwt = jwtUtil.substringToken(bearerJwt);
        Claims claims = jwtUtil.extractClaims(jwt);
        String userId = claims.getSubject();
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        if (userRole.equals(UserRole.USER)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
            return false;
        }

        String requestURI = request.getRequestURI();
        LocalDateTime accessTime = LocalDateTime.now();
        log.info("URL : {}", requestURI);
        log.info("start : {}", accessTime);
        log.info("userId : {}", userId);
        return true;
    }
}
