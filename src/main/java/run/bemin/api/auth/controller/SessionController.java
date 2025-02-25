package run.bemin.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.auth.dto.TokenDto;
import run.bemin.api.auth.dto.request.SigninRequestDto;
import run.bemin.api.auth.dto.response.RefreshResponseDto;
import run.bemin.api.auth.dto.response.SigninResponseDto;
import run.bemin.api.auth.dto.response.SignoutResponseDto;
import run.bemin.api.auth.exception.RefreshTokenMissingException;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.auth.util.CookieUtil;
import run.bemin.api.auth.util.JwtUtil;
import run.bemin.api.auth.util.ResponseBuilder;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.security.UserDetailsImpl;

/**
 * SessionController는 사용자 로그인, 토큰 갱신 및 로그아웃 관련 API 엔드포인트를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "세션", description = "사용자 로그인/로그아웃 및 토큰 관리 컨트롤러")
public class SessionController {

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  /**
   * 사용자 로그인 API.
   *
   * @param requestDto 로그인 요청 DTO (이메일, 비밀번호 포함)
   * @param res        HTTP 응답 객체 (리프레시 토큰 쿠키 설정에 사용)
   * @return 로그인 결과와 함께 발급된 액세스 토큰 및 사용자 정보를 반환합니다.
   */
  @PostMapping("/signin")
  @Operation(summary = "로그인", description = "사용자 로그인 및 토큰 발급을 수행합니다.")
  public ResponseEntity<ApiResponse<SigninResponseDto>> signin(
      @Valid @RequestBody SigninRequestDto requestDto,
      HttpServletResponse res) {

    TokenDto tokenDto = authService.signin(requestDto.getUserEmail(), requestDto.getPassword());
    CookieUtil.addRefreshCookie(res, tokenDto.getRefreshToken(), jwtUtil);

    SigninResponseDto responseDto = new SigninResponseDto(
        tokenDto.getAccessToken(),
        tokenDto.getEmail(),
        tokenDto.getNickname(),
        tokenDto.getRole()
    );
    log.info("로그인 요청: userEmail={}", requestDto.getUserEmail());

    return ResponseBuilder.buildResponse(responseDto.getAccessToken(), "성공", responseDto, JwtUtil.AUTHORIZATION_HEADER);
  }

  /**
   * 액세스 토큰 갱신 API.
   *
   * @param req HTTP 요청 객체 (쿠키에 저장된 리프레시 토큰 사용)
   * @param res HTTP 응답 객체 (갱신된 리프레시 토큰 쿠키 설정에 사용)
   * @return 갱신된 액세스 토큰 및 관련 사용자 정보를 반환합니다.
   * @throws RefreshTokenMissingException 리프레시 토큰이 없는 경우 예외 발생
   */
  @PostMapping("/refresh")
  @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용해 액세스 토큰을 갱신합니다.")
  public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(
      HttpServletRequest req,
      HttpServletResponse res) {

    String refreshToken = CookieUtil.getRefreshTokenFromCookies(req);
    if (refreshToken == null) {
      throw new RefreshTokenMissingException(ErrorCode.AUTH_REFRESH_TOKEN_MISSING.getMessage());
    }
    TokenDto tokenDto = authService.refresh(JwtUtil.BEARER_PREFIX + refreshToken);
    CookieUtil.addRefreshCookie(res, tokenDto.getRefreshToken(), jwtUtil);

    RefreshResponseDto refreshResponse = new RefreshResponseDto(
        tokenDto.getAccessToken(),
        tokenDto.getEmail(),
        tokenDto.getNickname(),
        tokenDto.getRole()
    );

    return ResponseBuilder.buildResponse(refreshResponse.getAccessToken(), "토큰 갱신 성공", refreshResponse,
        JwtUtil.AUTHORIZATION_HEADER);
  }

  /**
   * 사용자 로그아웃 API.
   *
   * @param userDetails 인증된 사용자 정보
   * @param req         HTTP 요청 객체 (액세스 토큰 확인에 사용)
   * @return 로그아웃 결과 메시지와 사용자 정보를 반환합니다.
   */
  @PostMapping("/signout")
  @Operation(summary = "로그아웃", description = "사용자 로그아웃을 수행하며, 토큰을 무효화합니다.")
  public ResponseEntity<ApiResponse<SignoutResponseDto>> logout(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      HttpServletRequest req) {

    String userEmail = userDetails.getUsername();
    String accessToken = jwtUtil.getTokenFromHeader(JwtUtil.AUTHORIZATION_HEADER, req);
    authService.signout(accessToken, userEmail);

    SignoutResponseDto signoutResponse = new SignoutResponseDto(userEmail, "로그아웃 성공");
    return ResponseBuilder.buildResponse(null, "로그아웃 성공", signoutResponse, JwtUtil.AUTHORIZATION_HEADER);
  }
}
