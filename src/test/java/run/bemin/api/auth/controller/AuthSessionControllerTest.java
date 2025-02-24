package run.bemin.api.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import run.bemin.api.auth.dto.SigninRequestDto;
import run.bemin.api.auth.dto.TokenDto;
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

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;
  ;

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
        .userEmail("user1@gmail.com")
        .password("user1234")
        .build();

    TokenDto tokenDto = new TokenDto("testToken", "Bearer testRefreshToken", 1000L, 2000L,
        "user1@gmail.com", "user1", UserRoleEnum.CUSTOMER);
    when(authService.signin("user1@gmail.com", "user1234")).thenReturn(tokenDto);
    when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2000L);

    // When & Then
    mockMvc.perform(post("/api/auth/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(header().string(JwtUtil.AUTHORIZATION_HEADER, "testToken"))
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.accessToken").value("testToken"))
        .andExpect(jsonPath("$.data.email").value("user1@gmail.com"))
        .andDo(result -> {
          String setCookie = result.getResponse().getHeader("Set-Cookie");
          System.out.println("Set-Cookie: " + setCookie);
        })
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
  @DisplayName("리프레시 토큰 성공 테스트")
  void refreshTokenSuccessTest() throws Exception {

    // Given
    String cookieRefreshToken = "testRefreshToken";
    String refreshToken = "Bearer " + cookieRefreshToken;

    when(jwtUtil.validateToken(cookieRefreshToken)).thenReturn(true);
    when(jwtUtil.getUserEmailFromToken(cookieRefreshToken)).thenReturn("test@gmail.com");
    when(jwtUtil.createAccessToken("test@gmail.com", UserRoleEnum.CUSTOMER)).thenReturn("newAccessToken");
    when(jwtUtil.createRefreshToken("test@gmail.com")).thenReturn("Bearer newRefreshToken");
    when(jwtUtil.getAccessTokenExpiration()).thenReturn(1000L);
    when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2000L);

    TokenDto tokenDto = new TokenDto("newAccessToken", "Bearer newRefreshToken", 1000L, 2000L,
        "test@gmail.com", "testUser", UserRoleEnum.CUSTOMER);
    when(authService.refresh(refreshToken)).thenReturn(tokenDto);

    // When & Then
    mockMvc.perform(post("/api/auth/refresh")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            // refresh 엔드포인트는 쿠키에서 refresh 토큰을 추출
            .cookie(new jakarta.servlet.http.Cookie("refresh", cookieRefreshToken)))
        .andExpect(status().isOk())
        .andExpect(header().string(JwtUtil.AUTHORIZATION_HEADER, "newAccessToken"))
        .andExpect(jsonPath("$.message").value("토큰 갱신 성공"))
        .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
        .andDo(result -> {
          String setCookie = result.getResponse().getHeader("Set-Cookie");
          System.out.println("Set-Cookie (refresh): " + setCookie);
        })
        .andDo(print());

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

    when(jwtUtil.getTokenFromHeader(eq(JwtUtil.AUTHORIZATION_HEADER), any(HttpServletRequest.class)))
        .thenReturn("someAccessToken");
    
    doNothing().when(authService).signout("someAccessToken", "test@gmail.com");

    // When & Then
    mockMvc.perform(post("/api/auth/signout")
            .with(user(userDetails))  // 인증된 사용자로 요청
            .with(csrf())
            .header(JwtUtil.AUTHORIZATION_HEADER, "Bearer someAccessToken"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("로그아웃 성공"))
        .andExpect(jsonPath("$.data.userEmail").value("test@gmail.com"))
        .andExpect(jsonPath("$.data.message").value("로그아웃 성공"))
        .andDo(print());
  }

}
