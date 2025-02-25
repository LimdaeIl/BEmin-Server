package run.bemin.api.auth.controller;

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


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class SessionController {

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
   * 토큰 리프레시
   */
  @PostMapping("/refresh")
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
    return ResponseBuilder.buildResponse(null, "로그아웃 성공", signoutResponse, JwtUtil.AUTHORIZATION_HEADER);
  }
}
