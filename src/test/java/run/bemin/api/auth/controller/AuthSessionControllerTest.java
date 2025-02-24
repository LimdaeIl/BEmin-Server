package run.bemin.api.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import run.bemin.api.auth.dto.SigninRequestDto;
import run.bemin.api.auth.dto.SigninResponseDto;
import run.bemin.api.auth.jwt.JwtUtil;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.config.MockConfig;
import run.bemin.api.config.TestSecurityConfig;
import run.bemin.api.config.WebSecurityConfig;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@WebMvcTest(
    controllers = AuthSessionController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurityConfig.class
        )
    }
)
@Import({MockConfig.class, TestSecurityConfig.class})
class AuthSessionControllerTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  @Autowired
  private AuthService authService;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  @DisplayName("로그인 성공 테스트")
  void signinSuccessTest() throws Exception {
    // Given
    SigninRequestDto requestDto = SigninRequestDto.builder()
        .userEmail("test@gmail.com")
        .password("test1234")
        .build();

    SigninResponseDto responseDto = new SigninResponseDto(
        "testToken",
        "test@gmail.com",
        "testUser",
        UserRoleEnum.CUSTOMER
    );

//    when(authService.signin(anyString(), anyString())).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(post("/api/auth/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto))
        )
        .andExpect(status().isOk())
        // 헤더에 JWT 토큰이 담기는지 확인
        .andExpect(header().string(JwtUtil.AUTHORIZATION_HEADER, "testToken"))
        // JSON 응답 검증
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.token").value("testToken"))
        .andExpect(jsonPath("$.data.email").value("test@gmail.com"))
        .andDo(print());
  }

  @Test
  @DisplayName("로그인 실패 - 이메일 형식이 올바르지 않을 경우")
  void signinFailureInvalidEmailFormatTest() throws Exception {

    String jsonRequest = "{\"userEmail\": \"invalid@\", \"password\": \"password123\"}";

    mockMvc.perform(post("/api/auth/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."))
        .andExpect(jsonPath("$.code").value("S002"));
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호를 입력하지 않을 경우")
  void signinFailureEmptyPasswordTest() throws Exception {
    String jsonRequest = "{\"userEmail\": \"test@gmail.com\", \"password\": \"\"}";

    mockMvc.perform(post("/api/auth/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRequest))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("비밀번호를 입력해주세요."))
        .andExpect(jsonPath("$.code").value("L002"));
  }

  @Test
  @DisplayName("로그아웃 성공 테스트")
  void signoutSuccessTest() throws Exception {
    // Given
    User testUser = User.builder()
        .userEmail("test@gmail.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();
    UserDetailsImpl userDetails = new UserDetailsImpl(testUser);

    // When & Then
    mockMvc.perform(post("/api/auth/signout")
            .with(user(userDetails))  // 인증된 사용자로 요청
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("로그아웃 성공"))
        .andExpect(jsonPath("$.data.userEmail").value("test@gmail.com"))
        .andExpect(jsonPath("$.data.message").value("로그아웃 성공"))
        .andDo(print());
  }
}
