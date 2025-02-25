package run.bemin.api.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
import run.bemin.api.auth.dto.request.SignupRequestDto;
import run.bemin.api.auth.dto.response.EmailCheckResponseDto;
import run.bemin.api.auth.dto.response.NicknameCheckResponseDto;
import run.bemin.api.auth.dto.response.SignupResponseDto;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.config.MockConfig;
import run.bemin.api.config.TestSecurityConfig;
import run.bemin.api.config.WebSecurityConfig;
import run.bemin.api.user.dto.UserAddressDto;
import run.bemin.api.user.entity.UserRoleEnum;

@WebMvcTest(
    controllers = SignupController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurityConfig.class
        )
    }
)
@Import({MockConfig.class, TestSecurityConfig.class})
class SignupControllerTest {

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
  @DisplayName("회원가입 성공 테스트")
  void signupSuccessTest() throws Exception {
    // Given
    UserAddressDto addressDto = UserAddressDto.builder()
        .bcode("bcode1")
        .jibunAddress("jibunAddress1")
        .roadAddress("roadAddress1")
        .detail("detail1")
        .build();

    SignupRequestDto requestDto = SignupRequestDto.builder()
        .userEmail("test@gmail.com")
        .password("test1234")
        .name("testUser")
        .nickname("test123")
        .phone("010-1111-1111")
        .role(UserRoleEnum.CUSTOMER)
        .address(addressDto)
        .build();

    SignupResponseDto responseDto = new SignupResponseDto("test@gmail.com", "ROLE_CUSTOMER");

    when(authService.signup(any(SignupRequestDto.class))).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(post("/api/auth/signup")
            .with(csrf())
            .with(anonymous())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.userEmail").value("test@gmail.com"))
        .andExpect(jsonPath("$.data.role").value("ROLE_CUSTOMER"))
        .andDo(print());

  }

  @Test
  @DisplayName("이메일 중복 확인 성공 테스트 - 사용 가능한 이메일")
  void checkEmailTest() throws Exception {
    // Given
    String email = "test@gmail.com";

    EmailCheckResponseDto emailResponse = new EmailCheckResponseDto(false, "사용 가능한 이메일입니다.", null);

    when(authService.checkEmail(email)).thenReturn(emailResponse);

    // When & Then
    mockMvc.perform(get("/api/auth/email/exists")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("이메일 중복 여부 확인"))
        .andExpect(jsonPath("$.data.duplicate").value(false))
        .andExpect(jsonPath("$.data.message").value("사용 가능한 이메일입니다."))
        .andDo(print());
  }

  @Test
  @DisplayName("이메일 중복 확인 실패 테스트 - 이메일 중복일 경우")
  void checkEmailDuplicateTest() throws Exception {
    // Given
    String email = "duplicate@gmail.com";
    EmailCheckResponseDto emailResponse = new EmailCheckResponseDto(true, "이미 존재하는 이메일입니다.", "S001");
    when(authService.checkEmail(email)).thenReturn(emailResponse);

    // When & Then
    mockMvc.perform(get("/api/auth/email/exists")
            .param("email", email))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."))
        .andExpect(jsonPath("$.data.code").value("S001"))
        .andDo(print());
  }

  @Test
  @DisplayName("이메일 중복 확인 실패 테스트 - 이메일 형식이 올바르지 않을 경우")
  void checkInvalidEmailFormatTest() throws Exception {
    // Given
    String email = "invalid@";

    when(authService.checkEmail(email))
        .thenThrow(new run.bemin.api.auth.exception.SignupInvalidEmailFormatException("이메일 형식이 올바르지 않습니다."));

    // When & Then
    mockMvc.perform(get("/api/auth/email/exists")
            .param("email", email))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."))
        .andExpect(jsonPath("$.code").value("S002"))
        .andDo(print());
  }

  @Test
  @DisplayName("이메일 중복 확인 실패 테스트 - 이메일 입력하지 않을 경우")
  void checkEmailNotProvidedTest() throws Exception {
    // Given
    String email = "";

    // When & Then
    mockMvc.perform(get("/api/auth/email/exists")
            .param("email", email))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이메일과 닉네임은 필수입니다."))
        .andExpect(jsonPath("$.code").value("S005"))
        .andDo(print());
  }


  @Test
  @DisplayName("닉네임 중복 확인 테스트 - 사용 가능한 닉네임")
  void checkNicknameTest() throws Exception {
    // Given
    String nickname = "testnick";

    NicknameCheckResponseDto nicknameResponse = new NicknameCheckResponseDto(false, "사용 가능한 닉네임입니다.", null);

    when(authService.checkNickname(nickname)).thenReturn(nicknameResponse);

    // When & Then
    mockMvc.perform(get("/api/auth/nickname/exists")
            .param("nickname", nickname))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("닉네임 중복 여부 확인"))
        .andExpect(jsonPath("$.data.duplicate").value(false))
        .andExpect(jsonPath("$.data.message").value("사용 가능한 닉네임입니다."))
        .andDo(print());
  }

  @Test
  @DisplayName("닉네임 중복 확인 실패 테스트 - 닉네임 중복일 경우")
  void checkNicknameDuplicateTest() throws Exception {
    // Given
    String nickname = "testUser";
    NicknameCheckResponseDto nicknameResponse = new NicknameCheckResponseDto(true, "이미 존재하는 닉네임입니다.", "S003");
    when(authService.checkNickname(nickname)).thenReturn(nicknameResponse);

    // When & Then
    mockMvc.perform(get("/api/auth/nickname/exists")
            .param("nickname", nickname))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."))
        .andExpect(jsonPath("$.data.code").value("S003"))
        .andDo(print());
  }

  @Test
  @DisplayName("닉네임 중복 확인 실패 테스트 - 닉네임 형식이 올바르지 않을 경우")
  void checkInvalidNicknameFormatTest() throws Exception {
    // Given
    String nickname = "t";

    when(authService.checkNickname(nickname))
        .thenThrow(new run.bemin.api.auth.exception.SignupInvalidNicknameFormatException("닉네임 형식이 올바르지 않습니다."));

    // When & Then
    mockMvc.perform(get("/api/auth/nickname/exists")
            .param("nickname", nickname))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("닉네임 형식이 올바르지 않습니다."))
        .andExpect(jsonPath("$.code").value("S004"))
        .andDo(print());
  }

  @Test
  @DisplayName("닉네임 중복 확인 실패 테스트 - 닉네임 입력하지 않을 경우")
  void checkNicknameNotProvidedTest() throws Exception {
    // Given
    String nickname = "";

    // When & Then
    mockMvc.perform(get("/api/auth/nickname/exists")
            .param("nickname", nickname))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("이메일과 닉네임은 필수입니다."))
        .andExpect(jsonPath("$.code").value("S005"))
        .andDo(print());
  }

}
