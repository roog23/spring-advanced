package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("회원가입이 정상적으로 이루어지는지 테스트")
    void signup() {
        //given
        SignupRequest signupRequest = new SignupRequest("a@a.com", "1234", "user");
        User user = new User("a@a.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn("Bearer ");

        //when
        SignupResponse signup = authService.signup(signupRequest);

        //then
        assertThat(signup.getBearerToken()).isEqualTo("Bearer ");
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입을 시도하는 경우 예외처리 테스트")
    void signupErrorEmail() {
        //given
        SignupRequest signupRequest = new SignupRequest("a@a.com", "1234", "user");

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> authService.signup(signupRequest));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 기능이 정상 작동하는지 테스트")
    void signin() {
        //given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "1234");
        User user = new User("a@a.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn("Bearer ");

        //when
        SigninResponse signin = authService.signin(signinRequest);

        //then
        assertThat(signin.getBearerToken()).isEqualTo("Bearer ");

    }

    @Test
    @DisplayName("로그인을 시도하는 정보가 가입되어 있지 않은 경우 예외 처리 테스트")
    void signinErrorNotSignup() {
        //given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "1234");
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("가입되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("로그인을 시도하는 정보의 비밀번호가 저장되어 있는 비밀번호와 다른 경우 예외 처리 테스트")
    void signinErrorWrongPassword() {
        //given
        SigninRequest signinRequest = new SigninRequest("a@a.com", "1234");
        User user = new User("a@a.com", "1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);

        //when
        AuthException authException = assertThrows(AuthException.class, () -> authService.signin(signinRequest));

        //then
        assertThat(authException.getMessage()).isEqualTo("잘못된 비밀번호입니다.");
    }
}