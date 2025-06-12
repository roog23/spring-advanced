package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 조회가 정상 동작하는지")
    void getUser() {
        //given
        long userId = 1L;
        User user = new User("a@a.com","1234", UserRole.USER);

        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        UserResponse result = userService.getUser(userId);

        //then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("a@a.com");
    }

    @Test
    @DisplayName("유저가 없는 경우 조회시 오류 처리가 잘 동작하는지")
    void getUserErrorNotFoundUser() {
        //given
        long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class,
                () -> userService.getUser(userId));
        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("유저가 비밀번호를 바꿀때 정상적으로 변경되는지")
    void changePassword() {
        //given
        long userId = 1L;
        User user = new User("a@a.com","1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("1234", "4321");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.encode(userChangePasswordRequest.getNewPassword())).willReturn("4321");
        given(passwordEncoder.matches("4321","1234")).willReturn(false);
        given(passwordEncoder.matches("1234","1234")).willReturn(true);

        //when
        userService.changePassword(userId, userChangePasswordRequest);

        //then
        assertThat(user.getPassword()).isEqualTo("4321");
    }

    @Test
    @DisplayName("유저가 비밀번호를 바꿀때 유저를 찾지 못한 경우 오류처리가 정상 작동하는지")
    void changePasswordErrorNotFoundUser() {
        //given
        long userId = 1L;

        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("1234", "1234");
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, userChangePasswordRequest));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("유저가 비밀번호를 바꿀때 같은 비밀번호일때 오류처리가 정상 작동하는지")
    void changePasswordErrorPasswordSame() {
        //given
        long userId = 1L;
        User user = new User("a@a.com","1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("1234", "1234");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("1234","1234")).willReturn(true);
        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, userChangePasswordRequest));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

    @Test
    @DisplayName("유저가 비밀번호를 바꿀때 잘못된 비밀번호를 입력했을 경우 오류처리가 정상 작동하는지")
    void changePasswordErrorWrongPassword() {
        //given
        long userId = 1L;
        User user = new User("a@a.com","1234", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("4321", "4321");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("4321","1234")).willReturn(false);
        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, userChangePasswordRequest));

        //then
        assertThat(invalidRequestException.getMessage()).isEqualTo("잘못된 비밀번호입니다.");
    }
}