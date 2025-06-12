package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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
class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("유저의 사용자 권한을 바꾸는 기능이 정상 작동하는지 테스트")
    void changeUserRole() {
        // given
        long userId = 1L;
        User user = new User("a@a.com","1234", UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", userId);
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest();
        ReflectionTestUtils.setField(userRoleChangeRequest, "role", "user");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        //when
        userAdminService.changeUserRole(userId,userRoleChangeRequest);

        //then
        assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("유저의 사용자 권한을 바꾸는 기능이 유저가 존재하지 않았을때 오류 처리 테스트")
    void changeUserRoleErrorNotFoundUser() {
        // given
        long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest();

        //when
        InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class,
                () -> userAdminService.changeUserRole(userId, userRoleChangeRequest));

        //then

        assertThat(invalidRequestException.getMessage()).isEqualTo("User not found");
    }
}