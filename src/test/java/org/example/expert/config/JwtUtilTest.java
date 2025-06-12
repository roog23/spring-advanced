package org.example.expert.config;

import io.jsonwebtoken.security.Keys;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("토큰이 정상적으로 생성이 되는지 테스트")
    void createToken() {
        //given
        String secretKey = "dlrjtdmstnarutjwkrtjdgodiehlsmseprnlcksgdktjdurlekwkrtjdgkqslek";
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        Key key = Keys.hmacShaKeyFor(bytes);
        ReflectionTestUtils.setField(jwtUtil, "key", key);

        //when
        String token = jwtUtil.createToken(1L, "a@a.com", UserRole.USER);

        //then
        assertThat(token).startsWith("Bearer ");
    }
}