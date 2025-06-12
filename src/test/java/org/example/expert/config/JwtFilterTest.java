package org.example.expert.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import io.jsonwebtoken.Claims;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private Claims claims;

    @Test
    @DisplayName("JwtFilter가 정상 작동하는지 테스트")
    void doFilter() throws ServletException, IOException {
        //given
        String token = "Bearer Token";

        given(request.getRequestURI()).willReturn("/localhost");
        given(request.getHeader("Authorization")).willReturn(token);

        given(jwtUtil.substringToken(anyString())).willReturn(token);
        given(jwtUtil.extractClaims(anyString())).willReturn(claims);

        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("a@a.com");
        given(claims.get("userRole")).willReturn(UserRole.USER);

        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("접근하는 경로가 /auth인 경우 필터를 통과시키는지 테스트")
    void doFilterPass() throws ServletException, IOException {
        //given
        given(request.getRequestURI()).willReturn("/auth");

        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰이 없는 경우 접근했을때 오류 처리 테스트")
    void doFilterErrorNoToken() throws ServletException, IOException {
        //given
        given(request.getRequestURI()).willReturn("/localhost");
        given(request.getHeader("Authorization")).willReturn(null);

        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
    }

    @Test
    @DisplayName("토큰에 값이 없는 경우에 접근했을때 오류 처리 테스트")
    void doFilterErrorTokenNoValue() throws ServletException, IOException {
        //given
        String token = "Bearer Token";

        given(request.getRequestURI()).willReturn("/localhost");
        given(request.getHeader("Authorization")).willReturn(token);

        given(jwtUtil.substringToken(anyString())).willReturn(token);
        given(jwtUtil.extractClaims(anyString())).willReturn(null);

        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
    }

    @Test
    @DisplayName("토큰의 사용 기간이 지난 후에 접근했을때 오류 처리 테스트")
    void doFilterErrorTokenOver() throws ServletException, IOException {
        //given
        String token = "Bearer Token";

        given(request.getRequestURI()).willReturn("/localhost");
        given(request.getHeader("Authorization")).willReturn(token);

        given(jwtUtil.substringToken(anyString())).willReturn(token);
        given(jwtUtil.extractClaims(anyString())).willThrow(new ExpiredJwtException(null, null, "유효기간이 지난 토큰"));

        //when
        jwtFilter.doFilter(request, response, chain);

        //then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "만료된 JWT 토큰입니다.");
    }
}