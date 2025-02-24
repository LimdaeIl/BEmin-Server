package run.bemin.api.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.auth.dto.RefreshResponseDto;
import run.bemin.api.auth.dto.SigninRequestDto;
import run.bemin.api.auth.dto.SigninResponseDto;
import run.bemin.api.auth.dto.SignoutResponseDto;
import run.bemin.api.auth.dto.TokenDto;
import run.bemin.api.auth.exception.RefreshTokenMissingException;
import run.bemin.api.auth.jwt.JwtUtil;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.security.UserDetailsImpl;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthSessionController {

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  /**
   * 로그인
   */
  @PostMapping("/signin")
  public ResponseEntity<ApiResponse<SigninResponseDto>> signin(
      @Valid @RequestBody SigninRequestDto requestDto,
      HttpServletResponse res) {

    TokenDto tokenDto = authService.signin(requestDto.getUserEmail(), requestDto.getPassword());
    // Refresh 토큰 쿠키 설정
    addRefreshCookie(res, tokenDto.getRefreshToken());

    SigninResponseDto responseDto = new SigninResponseDto(
        tokenDto.getAccessToken(),
        tokenDto.getEmail(),
        tokenDto.getNickname(),
        tokenDto.getRole()
    );

    log.info("로그인 요청 = {}", requestDto);
    return ResponseEntity.ok()
        .header(JwtUtil.AUTHORIZATION_HEADER, responseDto.getAccessToken())
        .body(ApiResponse.from(HttpStatus.OK, "성공", responseDto));
  }

  /**
   * 토큰 리프레시
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(
      HttpServletRequest req,
      HttpServletResponse res) {

    String refreshToken = extractRefreshToken(req);
    if (refreshToken == null) {
      throw new RefreshTokenMissingException(ErrorCode.AUTH_REFRESH_TOKEN_MISSING.getMessage());
    }

    TokenDto tokenDto = authService.refresh("Bearer " + refreshToken);
    addRefreshCookie(res, tokenDto.getRefreshToken());

    RefreshResponseDto refreshResponse = new RefreshResponseDto(
        tokenDto.getAccessToken(),
        tokenDto.getEmail(),
        tokenDto.getNickname(),
        tokenDto.getRole()
    );

    return ResponseEntity.ok()
        .header(JwtUtil.AUTHORIZATION_HEADER, refreshResponse.getAccessToken())
        .body(ApiResponse.from(HttpStatus.OK, "토큰 갱신 성공", refreshResponse));
  }

  /**
   * 로그아웃
   */
  @PostMapping("/signout")
  public ResponseEntity<ApiResponse<SignoutResponseDto>> logout(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      HttpServletRequest req) {

    String userEmail = userDetails.getUsername();
    String accessToken = jwtUtil.getTokenFromHeader(JwtUtil.AUTHORIZATION_HEADER, req);
    authService.signout(accessToken, userEmail);

    SignoutResponseDto signoutResponse = new SignoutResponseDto(userEmail, "로그아웃 성공");
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "로그아웃 성공", signoutResponse));
  }

  /**
   * Refresh 토큰을 쿠키에 설정하는 메서드
   */
  private void addRefreshCookie(HttpServletResponse res, String rawRefreshToken) {
    String token = rawRefreshToken;
    if (rawRefreshToken != null && rawRefreshToken.startsWith(JwtUtil.BEARER_PREFIX)) {
      token = rawRefreshToken.substring(JwtUtil.BEARER_PREFIX.length());
    }
    Cookie refreshCookie = new Cookie("refresh", token);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpiration() / 1000));
    // 필요시 Secure 옵션 등 추가 가능
    refreshCookie.setSecure(true);
    res.addCookie(refreshCookie);
  }

  /**
   * 요청 쿠키에서 Refresh 토큰을 추출하는 메서드
   */
  private String extractRefreshToken(HttpServletRequest req) {
    if (req.getCookies() != null) {
      for (Cookie cookie : req.getCookies()) {
        if ("refresh".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
