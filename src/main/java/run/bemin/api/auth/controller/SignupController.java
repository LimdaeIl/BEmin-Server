package run.bemin.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * SignupController는 사용자 회원가입 및 중복 확인 관련 API 엔드포인트를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "회원가입", description = "사용자 회원가입 및 중복 확인 컨트롤러")
public class SignupController {

  private final AuthService authService;

  /**
   * 사용자 회원가입 API.
   *
   * @param requestDTO 회원가입 요청 DTO (필요한 사용자 정보 포함)
   * @return 회원가입 결과와 함께 생성된 사용자 정보를 반환합니다.
   */
  @PostMapping("/signup")
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  public ApiResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto requestDTO) {
    SignupResponseDto responseDto = authService.signup(requestDTO);
    return ApiResponse.from(HttpStatus.OK, "성공", responseDto);
  }

  /**
   * 이메일 중복 확인 API.
   *
   * @param email 확인할 이메일 주소
   * @return 이메일 중복 여부와 관련 메시지를 반환합니다.
   */
  @GetMapping("/email/exists")
  @Operation(summary = "이메일 중복 확인", description = "이메일의 중복 여부를 확인합니다.")
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
   * 닉네임 중복 확인 API.
   *
   * @param nickname 확인할 닉네임
   * @return 닉네임 중복 여부와 관련 메시지를 반환합니다.
   */
  @GetMapping("/nickname/exists")
  @Operation(summary = "닉네임 중복 확인", description = "닉네임의 중복 여부를 확인합니다.")
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
