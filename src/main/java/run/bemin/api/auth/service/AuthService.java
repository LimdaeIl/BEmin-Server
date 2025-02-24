package run.bemin.api.auth.service;

import jakarta.validation.Valid;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.auth.dto.EmailCheckResponseDto;
import run.bemin.api.auth.dto.NicknameCheckResponseDto;
import run.bemin.api.auth.dto.SignupRequestDto;
import run.bemin.api.auth.dto.SignupResponseDto;
import run.bemin.api.auth.dto.TokenDto;
import run.bemin.api.auth.exception.SignupDuplicateEmailException;
import run.bemin.api.auth.exception.SignupDuplicateNicknameException;
import run.bemin.api.auth.exception.SignupInvalidEmailFormatException;
import run.bemin.api.auth.exception.SignupInvalidNicknameFormatException;
import run.bemin.api.auth.jwt.JwtUtil;
import run.bemin.api.auth.repository.AuthRepository;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.dto.UserAddressDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserAddress;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.repository.UserAddressRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
  private final AuthRepository authRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserAddressRepository userAddressRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @Transactional
  public SignupResponseDto signup(@Valid SignupRequestDto requestDto) {
    String encodePassword = passwordEncoder.encode(requestDto.getPassword());

    authRepository.findByUserEmail(requestDto.getUserEmail())
        .ifPresent(user -> {
          throw new SignupDuplicateEmailException(ErrorCode.SIGNUP_DUPLICATE_EMAIL.getMessage());
        });

    authRepository.findByNickname(requestDto.getNickname())
        .ifPresent(user -> {
          throw new SignupDuplicateNicknameException(ErrorCode.SIGNUP_DUPLICATE_NICKNAME.getMessage());
        });

    User user = User.builder()
        .userEmail(requestDto.getUserEmail())
        .password(encodePassword)
        .name(requestDto.getName())
        .nickname(requestDto.getNickname())
        .phone(requestDto.getPhone())
        .role(requestDto.getRole())
        .build();
    User savedUser = authRepository.save(user);

    // AddressDto를 사용해 UserAddress 엔티티 생성 (대표 주소로 저장)
    UserAddressDto addrDto = requestDto.getAddress();
    UserAddress userAddress = UserAddress.builder()
        .bcode(addrDto.getBcode())
        .jibunAddress(addrDto.getJibunAddress())
        .roadAddress(addrDto.getRoadAddress())
        .detail(addrDto.getDetail())
        .isRepresentative(true)  // 회원가입 시 입력한 주소는 대표 주소로 간주
        .user(savedUser)
        .build();
    userAddressRepository.save(userAddress);

    //User 엔티티의 대표 주소를 새로 저장한 userAddress로 설정
    savedUser.setRepresentativeAddress(userAddress);

    return new SignupResponseDto(savedUser.getUserEmail(), savedUser.getRole().getAuthority());
  }

  /**
   * 이메일 중복 체크
   **/
  @Transactional(readOnly = true)
  public EmailCheckResponseDto checkEmail(String email) {
    validateEmail(email);
    boolean isDuplicate = authRepository.existsByUserEmail(email);

    if (isDuplicate) {
      throw new SignupDuplicateEmailException(ErrorCode.SIGNUP_DUPLICATE_EMAIL.getMessage());
    }

    return new EmailCheckResponseDto(
        false,
        "사용 가능한 이메일입니다.",
        null
    );
  }


  // 이메일 형식 검증
  private void validateEmail(String email) {
    if (!Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$", email)) {
      throw new SignupInvalidEmailFormatException(ErrorCode.SIGNUP_INVALID_EMAIL_FORMAT.getMessage());
    }
  }

  /**
   * 닉네임 중복 체크
   **/
  @Transactional(readOnly = true)
  public NicknameCheckResponseDto checkNickname(String nickname) {
    validateNickname(nickname);
    boolean isDuplicate = authRepository.existsByNickname(nickname);

    if (isDuplicate) {
      throw new SignupDuplicateNicknameException(ErrorCode.SIGNUP_DUPLICATE_NICKNAME.getMessage());
    }

    return new NicknameCheckResponseDto(
        false,
        "사용 가능한 닉네임입니다.",
        null
    );
  }


  // 닉네임 형식 검증
  private void validateNickname(String nickname) {
    if (!Pattern.matches("^[a-z0-9]{4,10}$", nickname)) {
      throw new SignupInvalidNicknameFormatException(ErrorCode.SIGNUP_INVALID_NICKNAME_FORMAT.getMessage());
    }
  }

  /**
   * 로그인 메서드
   */
  @CachePut(cacheNames = "LOGIN_USER", key = "'login:' + #userEmail")
  @Transactional
  public TokenDto signin(String userEmail, String password) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(userEmail, password)
    );

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    UserRoleEnum role = userDetails.getRole();

    String accessToken = jwtUtil.createAccessToken(userEmail, role);
    String refreshToken = jwtUtil.createRefreshToken(userEmail);

    TokenDto tokenDto = new TokenDto(
        accessToken,
        refreshToken,
        jwtUtil.getAccessTokenExpiration(),
        jwtUtil.getRefreshTokenExpiration(),
        userEmail,
        userDetails.getNickname(),
        role
    );

    // Redis에 Refresh Token 저장
    redisTemplate.opsForValue()
        .set("RT:" + userEmail, refreshToken, tokenDto.getRefreshTokenExpiresTime(), TimeUnit.MILLISECONDS);
    log.info("Refresh Token stored in Redis: key=RT:{} , token={}", userEmail, refreshToken);

    return tokenDto;
  }

  @Transactional
  public TokenDto refresh(String refreshToken) {

    String pureToken = refreshToken;
    if (pureToken.startsWith(JwtUtil.BEARER_PREFIX)) {
      pureToken = pureToken.substring(JwtUtil.BEARER_PREFIX.length());
    }

    if (!jwtUtil.validateToken(pureToken)) {
      throw new RuntimeException("유효하지 않거나 만료된 Refresh Token 입니다.");
    }

    String userEmail = jwtUtil.getUserEmailFromToken(pureToken);

    String storedRefreshToken = (String) redisTemplate.opsForValue().get("RT:" + userEmail);
    if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
      throw new RuntimeException("저장된 Refresh Token과 일치하지 않거나 만료되었습니다.");
    }

    User user = authRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    String newAccessToken = jwtUtil.createAccessToken(userEmail, user.getRole());

    // 새 Refresh Token 발급 후 Redis 업데이트
    String newRefreshToken = jwtUtil.createRefreshToken(userEmail);
    redisTemplate.opsForValue()
        .set("RT:" + userEmail, newRefreshToken, jwtUtil.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);

    return new TokenDto(
        newAccessToken,
        newRefreshToken,
        jwtUtil.getAccessTokenExpiration(),
        jwtUtil.getRefreshTokenExpiration(),
        userEmail,
        user.getNickname(),
        user.getRole()
    );
  }


  /**
   * 로그아웃 메서드 - Redis에 저장된 Refresh Token 삭제 - 현재 사용 중인 Access Token을 블랙리스트로 등록 (남은 유효시간을 TTL로 설정) - 로그인 캐시도 삭제
   * (@CacheEvict)
   */
  @CacheEvict(cacheNames = "LOGIN_USER", key = "'login:' + #userEmail")
  @Transactional
  public void signout(String accessToken, String userEmail) {

    redisTemplate.delete("RT:" + userEmail);

    // Access Token의 남은 유효시간 계산
    long remainingTime = jwtUtil.getRemainingExpirationTime(accessToken);

    // Access Token을 블랙리스트에 등록
    redisTemplate.opsForValue().set(accessToken, "logout", remainingTime, TimeUnit.MILLISECONDS);
  }

}
