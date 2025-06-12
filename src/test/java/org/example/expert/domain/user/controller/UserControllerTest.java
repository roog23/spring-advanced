package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret.key=" + "bXktc2VjcmV0LWtleS1mb3ItdGVzdC1zaG91bGQtYmUtbG9uZw=="
})
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private User user;

    @BeforeEach
    void setup() {
        String encode = passwordEncoder.encode("password");
        user = new User("a@a.com", encode, UserRole.USER);
        userRepository.save(user);

        token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
    }

    @Test
    @DisplayName("유저 조회 기능이 정상 작동하는지 통합 테스트")
    void getUser() throws Exception {
        mockMvc.perform(get("/users/{userId}", user.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    @DisplayName("유저 비밀번호 변경 기능이 정상 작동하는지 통합 테스트")
    void changePassword() throws Exception {
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("password", "NEWpassword123");

        mockMvc.perform(put("/users")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userChangePasswordRequest)))
                .andExpect(status().isOk());
    }
}