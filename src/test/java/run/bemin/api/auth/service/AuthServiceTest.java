package run.bemin.api.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import run.bemin.api.auth.dto.TokenDto;
import run.bemin.api.auth.dto.request.SigninRequestDto;
import run.bemin.api.auth.dto.request.SignupRequestDto;
import run.bemin.api.auth.dto.response.SignupResponseDto;
import run.bemin.api.auth.exception.RefreshTokenInvalidException;
import run.bemin.api.auth.exception.RefreshTokenMismatchException;
import run.bemin.api.auth.exception.SigninUnauthorizedException;
import run.bemin.api.auth.exception.SignupDuplicateEmailException;
import run.bemin.api.auth.exception.SignupDuplicateNicknameException;
import run.bemin.api.auth.exception.SignupInvalidEmailFormatException;
import run.bemin.api.auth.exception.SignupInvalidNicknameFormatException;
import run.bemin.api.auth.repository.AuthRepository;
import run.bemin.api.auth.util.JwtUtil;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.dto.UserAddressDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserAddress;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.exception.UserNotFoundException;
import run.bemin.api.user.repository.UserAddressRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AuthRepository authRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private RedisTemplate redisTemplate;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private UserAddressRepository userAddressRepository;

  @InjectMocks
  private AuthService authService;

  private User testUser;
  private SignupRequestDto signupRequestDto;
  private SigninRequestDto signinRequestDto;
  private UserAddressDto userAddressDto;


  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .userEmail("test@gmail.com")
        .password("test1234")
        .name("testUser")
        .nickname("testUser1")
        .phone("010-1111-1111")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    userAddressDto = UserAddressDto.builder()
        .bcode("bcode1")
        .jibunAddress("jibunAddress1")
        .roadAddress("roadAddress1")
        .detail("detail1")
        .build();

    signupRequestDto = SignupRequestDto.builder()
        .userEmail(testUser.getUserEmail())
        .password(testUser.getPassword())
        .name(testUser.getName())
        .nickname(testUser.getNickname())
        .phone(testUser.getPhone())
        .role(testUser.getRole())
        .address(userAddressDto)
        .build();
  }

  @Test
  @DisplayName("회원가입 성공 테스트")
  void SignupSuccessTest() {
    // Given
    // 중복된 이메일이나 닉네임이 없는 상황 가정
    when(authRepository.findByUserEmail(signupRequestDto.getUserEmail()))
        .thenReturn(Optional.empty());
    when(authRepository.findByNickname((signupRequestDto.getNickname())))
        .thenReturn(Optional.empty());
    // 인코딩된 비밀번호 반환
    when(passwordEncoder.encode(signupRequestDto.getPassword()))
        .thenReturn("encodedPassword");

    User savedUser = User.builder()
        .userEmail(signupRequestDto.getUserEmail())
        .password("encodedPassword")
        .name(signupRequestDto.getName())
        .nickname(signupRequestDto.getNickname())
        .phone(signupRequestDto.getPhone())
        .role(signupRequestDto.getRole())
        .build();
    when(authRepository.save(any(User.class))).thenReturn(savedUser);

    UserAddress savedUserAddress = UserAddress.builder()
        .bcode(userAddressDto.getBcode())
        .jibunAddress(userAddressDto.getJibunAddress())
        .roadAddress(userAddressDto.getRoadAddress())
        .detail(userAddressDto.getDetail())
        .isRepresentative(true)
        .user(savedUser)
        .build();
    when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedUserAddress);

    // When
    SignupResponseDto response = authService.signup(signupRequestDto);

    // Then
    assertEquals("test@gmail.com", response.getUserEmail());
    assertEquals(testUser.getRole().getAuthority(), response.getRole());
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 이메일 중복")
  void SignupDuplicateEmailTest() {
    // Given
    User existingEmail = User.builder().userEmail(signupRequestDto.getUserEmail()).build();
    when(authRepository.findByUserEmail(signupRequestDto.getUserEmail()))
        .thenReturn(Optional.of(existingEmail));

    // When & Then
    SignupDuplicateEmailException exception = assertThrows(SignupDuplicateEmailException.class,
        () -> authService.signup(signupRequestDto));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 닉네임 중복")
  void SignupDuplicateNicknameTest() {
    // Given
    User existingNickname = User.builder().nickname(signupRequestDto.getNickname()).build();
    when(authRepository.findByNickname(signupRequestDto.getNickname()))
        .thenReturn(Optional.of(existingNickname));

    // When & Then
    SignupDuplicateNicknameException exception = assertThrows(SignupDuplicateNicknameException.class,
        () -> authService.signup(signupRequestDto));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("회원가입 - 이메일 형식이 올바르지 않을 경우")
  void invalidEmailFormatTest() {
    // Given
    String invalidEmail = "invalid@test";

    // When & Then
    SignupInvalidEmailFormatException exception = assertThrows(SignupInvalidEmailFormatException.class,
        () -> authService.checkEmail(invalidEmail));

    assertEquals("이메일 형식이 올바르지 않습니다.", exception.getMessage());
    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("회원가입 - 닉네임 형식이 올바르지 않을 경우")
  void invalidNicknameFormatTest() {
    // Given
    String invalidNickname = "!";

    // When & Then
    SignupInvalidNicknameFormatException exception = assertThrows(SignupInvalidNicknameFormatException.class,
        () -> authService.checkNickname(invalidNickname));

    assertEquals("닉네임 형식이 올바르지 않습니다.", exception.getMessage());
    System.out.println("Expected exception occurred: " + exception.getMessage());
  }


  @Test
  @DisplayName("로그인 성공 테스트")
  void signinSuccessTest() {
    // Given
    signinRequestDto = SigninRequestDto.builder()
        .userEmail("test@gmail.com")
        .password("test1234")
        .build();

    testUser = User.builder()
        .userEmail("test@gmail.com")
        .password("encodedTest1234")
        .nickname("testUser1")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    // 모의 Authentication 객체 생성 및 반환
    Authentication dummyAuth = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(dummyAuth);
    UserDetailsImpl userDetails = new UserDetailsImpl(testUser);
    when(dummyAuth.getPrincipal()).thenReturn(userDetails);

    // User 재조회
    when(authRepository.findByUserEmail(signinRequestDto.getUserEmail()))
        .thenReturn(Optional.of(testUser));

    // jwtUtil 모의 설정
    when(jwtUtil.createAccessToken(testUser.getUserEmail(), testUser.getRole()))
        .thenReturn("accessTokenTest");
    when(jwtUtil.createRefreshToken(testUser.getUserEmail()))
        .thenReturn("Bearer refreshTokenTest");
    when(jwtUtil.getAccessTokenExpiration()).thenReturn(1000L);
    when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2000L);

    // redisTemplate의 ValueOperations 모의 설정
    ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    when(valueOps.get("RT:" + testUser.getUserEmail())).thenReturn("Bearer refreshTokenTest");

    // When
    TokenDto tokenDto = authService.signin(signinRequestDto.getUserEmail(), signinRequestDto.getPassword());

    // Then
    assertEquals("accessTokenTest", tokenDto.getAccessToken());
    assertEquals("Bearer refreshTokenTest", tokenDto.getRefreshToken());

    // System.out.println을 통해 출력 확인
    System.out.println("Access Token: " + tokenDto.getAccessToken());
    System.out.println("Refresh Token: " + tokenDto.getRefreshToken());

    // Redis에 저장된 값 확인
    Object redisStoredValue = redisTemplate.opsForValue().get("RT:" + testUser.getUserEmail());
    System.out.println("Redis 저장값 (RT:" + testUser.getUserEmail() + "): " + redisStoredValue);

    // Redis에 Refresh Token 저장 여부 검증
    verify(valueOps)
        .set(eq("RT:" + testUser.getUserEmail()), eq("Bearer refreshTokenTest"), eq(2000L), eq(TimeUnit.MILLISECONDS));
  }


  @Test
  @DisplayName("로그인 실패 - 존재하지 않은 회원")
  void SigninFailureNonExistentUserTest() {
    // Given
    signinRequestDto = SigninRequestDto.builder()
        .userEmail("non@test.com")
        .password("non123")
        .build();

    // When
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    // Then
    SigninUnauthorizedException exception = assertThrows(SigninUnauthorizedException.class, () ->
        authService.signin(signinRequestDto.getUserEmail(), signinRequestDto.getPassword())
    );

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("토큰 리프레시 성공 테스트")
  void refreshTokenSuccessTestWithPrint() {
    // Given
    String refreshToken = "Bearer refreshTokenTest";
    String pureRefreshToken = "refreshTokenTest";

    when(jwtUtil.validateToken(pureRefreshToken)).thenReturn(true);
    when(jwtUtil.getUserEmailFromToken(pureRefreshToken)).thenReturn("test@gmail.com");

    ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    // 기존 토큰 반환
    when(valueOps.get("RT:" + "test@gmail.com")).thenReturn(refreshToken);

    when(authRepository.findByUserEmail("test@gmail.com"))
        .thenReturn(Optional.of(testUser));

    // 새 토큰 생성 및 만료시간 반환
    when(jwtUtil.createAccessToken("test@gmail.com", testUser.getRole()))
        .thenReturn("newAccessToken");
    when(jwtUtil.createRefreshToken("test@gmail.com"))
        .thenReturn("newRefreshToken");
    when(jwtUtil.getAccessTokenExpiration()).thenReturn(1000L);
    when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2000L);

    // When
    TokenDto refreshedTokenDto = authService.refresh(refreshToken);

    // Then
    assertEquals("newAccessToken", refreshedTokenDto.getAccessToken());
    assertEquals("newRefreshToken", refreshedTokenDto.getRefreshToken());

    System.out.println("New Access Token: " + refreshedTokenDto.getAccessToken());
    System.out.println("New Refresh Token: " + refreshedTokenDto.getRefreshToken());

    when(valueOps.get("RT:" + "test@gmail.com")).thenReturn("newRefreshToken");

    Object redisStoredValue = redisTemplate.opsForValue().get("RT:" + "test@gmail.com");
    System.out.println("Redis 저장값 (RT:test@gmail.com): " + redisStoredValue);

    // Redis에 Refresh Token 저장 여부 검증
    verify(valueOps)
        .set(eq("RT:" + "test@gmail.com"), eq("newRefreshToken"), eq(2000L), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  @DisplayName("토큰 리프레시 실패 테스트 - 유효하지 않거나 만료된 Refresh Token")
  void refreshTokenInvalidTest() {
    // Given
    String refreshToken = "Bearer refreshTokenTest";
    String pureToken = "refreshTokenTest";

    when(jwtUtil.validateToken(pureToken)).thenReturn(false);

    // When & Then
    RefreshTokenInvalidException exception = assertThrows(RefreshTokenInvalidException.class,
        () -> authService.refresh(refreshToken));
    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("토큰 리프레시 실패 테스트 - 저장된 토큰과 불일치")
  void refreshTokenMismatchTest() {
    // Given
    String refreshToken = "Bearer refreshTokenTest";
    String pureToken = "refreshTokenTest";

    when(jwtUtil.validateToken(pureToken)).thenReturn(true);
    when(jwtUtil.getUserEmailFromToken(pureToken)).thenReturn(testUser.getUserEmail());

    ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    when(valueOps.get("RT:" + testUser.getUserEmail())).thenReturn("Bearer differentToken");

    // When & Then
    RefreshTokenMismatchException exception = assertThrows(RefreshTokenMismatchException.class,
        () -> authService.refresh(refreshToken));
    System.out.println("\"Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("토큰 리프레시 실패 테스트 - 사용자 미존재")
  void refreshTokenUserNotFoundTest() {
    // Given
    String refreshToken = "Bearer refreshTokenTest";
    String pureToken = "refreshTokenTest";

    when(jwtUtil.validateToken(pureToken)).thenReturn(true);
    when(jwtUtil.getUserEmailFromToken(pureToken)).thenReturn(testUser.getUserEmail());

    ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    // Redis에 저장된 토큰이 요청 토큰과 일치하는 경우
    when(valueOps.get("RT:" + testUser.getUserEmail())).thenReturn(refreshToken);

    when(authRepository.findByUserEmail(testUser.getUserEmail())).thenReturn(Optional.empty());

    // When & Then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> authService.refresh(refreshToken));
    System.out.println("Expected exception occurred: " + exception.getMessage());
  }


  @Test
  @DisplayName("로그아웃 성공 테스트")
  void logoutSuccessTestWithPrint() {
    // Given
    String accessToken = "accessTokenTest";
    String userEmail = testUser.getUserEmail();
    // access 토큰 남은 유효시간 반환
    when(jwtUtil.getRemainingExpirationTime(accessToken)).thenReturn(1000L);

    // 블랙리스트 등록용
    ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOps);

    // When
    authService.signout(accessToken, userEmail);

    // Then: Redis에서 RT:userEmail 키 삭제 검증
    verify(redisTemplate).delete("RT:" + userEmail);
    verify(valueOps).set(eq(accessToken), eq("logout"), eq(1000L), eq(TimeUnit.MILLISECONDS));

    System.out.println("로그아웃 테스트 - Redis에서 RT:" + userEmail + " 키가 삭제되었습니다.");
    System.out.println("로그아웃 테스트 - Access Token(" + accessToken + ")이 블랙리스트에 TTL 1000L로 등록되었습니다.");
  }


}