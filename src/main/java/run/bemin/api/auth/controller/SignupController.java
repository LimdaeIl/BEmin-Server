package run.bemin.api.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.auth.dto.request.SignupRequestDto;
import run.bemin.api.auth.dto.response.EmailCheckResponseDto;
import run.bemin.api.auth.dto.response.NicknameCheckResponseDto;
import run.bemin.api.auth.dto.response.SignupResponseDto;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.general.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class SignupController {

  private final AuthService authService;

  /**
   * 회원가입
   */
  @PostMapping("/signup")
  public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto requestDTO) {
    SignupResponseDto responseDto = authService.signup(requestDTO);
    return ApiResponse.from(HttpStatus.OK, "성공", responseDto);
  }

  /**
   * 이메일 중복 확인
   */
  @GetMapping("/email/exists")
  public ResponseEntity<ApiResponse<EmailCheckResponseDto>> checkEmail(
      @RequestParam @NotBlank(message = "이메일을 입력해주세요.") String email
  ) {
    EmailCheckResponseDto responseDto = authService.checkEmail(email);
    ApiResponse<EmailCheckResponseDto> response;
    if (responseDto.isDuplicate()) {
      response = ApiResponse.from(HttpStatus.BAD_REQUEST, responseDto.getMessage(), responseDto);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    response = ApiResponse.from(HttpStatus.OK, "이메일 중복 여부 확인", responseDto);
    return ResponseEntity.ok(response);
  }


  /**
   * 닉네임 중복 확인
   */
  @GetMapping("/nickname/exists")
  public ResponseEntity<ApiResponse<NicknameCheckResponseDto>> checkNickname(
      @RequestParam @NotBlank(message = "닉네임을 입력해주세요.") String nickname
  ) {
    NicknameCheckResponseDto responseDto = authService.checkNickname(nickname);
    ApiResponse<NicknameCheckResponseDto> response;
    if (responseDto.isDuplicate()) {
      response = ApiResponse.from(HttpStatus.BAD_REQUEST, responseDto.getMessage(), responseDto);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    response = ApiResponse.from(HttpStatus.OK, "닉네임 중복 여부 확인", responseDto);
    return ResponseEntity.ok(response);
  }
}
