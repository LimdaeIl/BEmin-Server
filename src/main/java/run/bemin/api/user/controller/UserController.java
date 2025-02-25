package run.bemin.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.dto.request.UserAddressRequestDto;
import run.bemin.api.user.dto.request.UserUpdateRequestDto;
import run.bemin.api.user.dto.response.UserAddressResponseDto;
import run.bemin.api.user.dto.response.UserResponseDto;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.exception.UserUnauthorizedException;
import run.bemin.api.user.service.UserAddressService;
import run.bemin.api.user.service.UserService;

/**
 * UserController는 사용자 정보 조회, 수정 및 주소 관리 관련 API 엔드포인트를 제공합니다.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "UserController")
public class UserController {
  private final UserService userService;
  private final UserAddressService userAddressService;

  /**
   * 전체 사용자 조회 API.
   *
   * <p>
   * 이 API는 MASTER 권한을 가진 사용자만 접근할 수 있습니다.
   * </p>
   *
   * @param page      조회할 페이지 번호 (기본값 0)
   * @param size      페이지 크기 (기본값 10)
   * @param isDeleted 삭제 여부 필터 (선택)
   * @param role      사용자 역할 필터 (선택)
   * @return 조건에 맞는 사용자 목록을 페이지 단위로 반환합니다.
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('MASTER')")
  @Operation(summary = "전체 사용자 조회", description = "전체 사용자 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsers(
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
      @RequestParam(value = "role", required = false) UserRoleEnum role
  ) {
    Page<UserResponseDto> users = userService.getAllUsers(
        isDeleted,
        page,
        size,
        "createdAt",
        true,
        role
    );
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", users));
  }

  /**
   * 인증된 사용자 자신의 정보 조회 API.
   *
   * @param userDetails 인증된 사용자 정보
   * @return 현재 로그인한 사용자의 정보를 반환합니다.
   */
  @GetMapping("/my-info")
  @Operation(summary = "내 정보 조회", description = "인증된 사용자의 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<UserResponseDto>> getMyUsers(
      @AuthenticationPrincipal UserDetailsImpl userDetails
  ) {
    UserResponseDto userResponseDto = userService.getUserByUserEmail(userDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", userResponseDto));
  }

  /**
   * 인증된 사용자 자신의 정보 수정 API.
   *
   * @param userDetails 인증된 사용자 정보
   * @param requestDto  수정할 사용자 정보 DTO
   * @return 수정된 사용자 정보를 반환합니다.
   */
  @PutMapping("/my-info")
  @Operation(summary = "내 정보 수정", description = "인증된 사용자의 정보를 수정합니다.")
  public ResponseEntity<ApiResponse<UserResponseDto>> updateMyInfo(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody @Valid UserUpdateRequestDto requestDto) {
    UserResponseDto responseDto = userService.updateUser(userDetails.getUsername(), requestDto);
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", responseDto));
  }

  /**
   * 특정 사용자 조회 API.
   *
   * <p>
   * 이 API는 MASTER 권한을 가진 사용자만 접근할 수 있습니다.
   * </p>
   *
   * @param userEmail 조회할 사용자의 이메일
   * @return 해당 이메일을 가진 사용자의 정보를 반환합니다.
   */
  @GetMapping("/{userEmail}")
  @PreAuthorize("hasAnyRole('MASTER')")
  @Operation(summary = "특정 사용자 조회", description = "이메일을 통해 특정 사용자를 조회합니다.")
  public ResponseEntity<ApiResponse<UserResponseDto>> getUserByUserEmail(
      @PathVariable("userEmail") String userEmail) {
    UserResponseDto userResponseDto = userService.getUserByUserEmail(userEmail);
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", userResponseDto));
  }

  /**
   * 인증된 사용자의 정보 수정 API.
   *
   * @param userDetails 인증된 사용자 정보
   * @param requestDto  수정할 사용자 정보 DTO
   * @return 수정된 사용자 정보를 반환합니다.
   */
  @PutMapping("/{userEmail}")
  @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','MANAGER','MASTER')")
  @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
  public ResponseEntity<UserResponseDto> updateUser(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody @Valid UserUpdateRequestDto requestDto) {

    UserResponseDto responseDto = userService.updateUser(userDetails.getUsername(), requestDto);
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", responseDto).data());
  }

  /**
   * 특정 사용자 소프트 삭제 API.
   *
   * @param userEmail   삭제할 사용자의 이메일
   * @param userDetails 인증된 사용자 정보
   * @return 삭제 성공 메시지와 함께 삭제된 사용자의 이메일을 반환합니다.
   */
  @DeleteMapping("/{userEmail}")
  @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','MANAGER','MASTER')")
  @Operation(summary = "사용자 삭제", description = "특정 사용자를 소프트 삭제합니다.")
  public ResponseEntity<?> softDeleteUser(@PathVariable String userEmail,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
    userService.softDeleteUser(userEmail, userDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", userEmail));
  }

  /**
   * 특정 사용자의 배달 주소 목록 조회 API.
   *
   * <p>
   * 이 API는 CUSTOMER 권한을 가진 사용자만 접근할 수 있습니다.
   * </p>
   *
   * @param userEmail   조회할 사용자의 이메일
   * @param userDetails 인증된 사용자 정보
   * @param page        조회할 페이지 번호 (기본값 0)
   * @param size        페이지 크기 (기본값 10)
   * @return 사용자의 배달 주소 목록을 페이지 단위로 반환합니다.
   */
  @GetMapping("/{userEmail}/addresses")
  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @Operation(summary = "주소 목록 조회", description = "특정 사용자의 배달 주소 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<Page<UserAddressResponseDto>>> getAddresses(
      @PathVariable("userEmail") String userEmail,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    validateAuthenticatedUser(userEmail, userDetails);
    Page<UserAddressResponseDto> addresses = userAddressService.getAllAddresses(
        userEmail,
        false,
        page,
        size);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "주소 목록 조회 성공", addresses));
  }

  /**
   * 특정 사용자의 배달 주소 추가 API.
   *
   * <p>
   * 이 API는 CUSTOMER 권한을 가진 사용자만 접근할 수 있습니다.
   * </p>
   *
   * @param userEmail         주소를 추가할 사용자의 이메일
   * @param addressRequestDto 추가할 주소 정보 DTO
   * @param userDetails       인증된 사용자 정보
   * @return 추가된 주소 정보를 반환합니다.
   */
  @PostMapping("/{userEmail}/addresses")
  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @Operation(summary = "주소 추가", description = "특정 사용자의 배달 주소를 추가합니다.")
  public ResponseEntity<ApiResponse<UserAddressResponseDto>> addAddress(
      @PathVariable("userEmail") String userEmail,
      @RequestBody @Valid UserAddressRequestDto addressRequestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    validateAuthenticatedUser(userEmail, userDetails);

    UserAddressResponseDto addedAddress = userAddressService.addAddress(userEmail, addressRequestDto);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "배달 주소 추가 성공", addedAddress));
  }

  /**
   * 특정 사용자의 대표 배달 주소 변경 API.
   *
   * <p>
   * 이 API는 CUSTOMER 권한을 가진 사용자만 접근할 수 있습니다.
   * </p>
   *
   * @param userEmail   주소를 변경할 사용자의 이메일
   * @param addressId   대표 주소로 변경할 주소 ID
   * @param userDetails 인증된 사용자 정보
   * @return 대표 주소로 변경된 주소 정보를 반환합니다.
   */
  @PutMapping("/{userEmail}/addresses/{addressId}/representative")
  @PreAuthorize("hasAnyRole('CUSTOMER')")
  @Operation(summary = "대표 주소 변경", description = "특정 사용자의 대표 배달 주소로 변경합니다.")
  public ResponseEntity<ApiResponse<UserAddressResponseDto>> setRepresentativeAddress(
      @PathVariable("userEmail") String userEmail,
      @PathVariable("addressId") UUID addressId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    validateAuthenticatedUser(userEmail, userDetails);

    UserAddressResponseDto updatedAddress = userAddressService.setRepresentativeAddress(userEmail, addressId);
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "대표 주소로 변경 성공", updatedAddress));
  }

  /**
   * 인증된 사용자와 요청한 이메일이 일치하는지 검증하는 내부 메서드.
   *
   * @param userEmail   요청 경로의 사용자 이메일
   * @param userDetails 인증된 사용자 정보
   * @throws UserUnauthorizedException 인증 정보가 일치하지 않으면 예외 발생
   */
  private void validateAuthenticatedUser(String userEmail, UserDetailsImpl userDetails) {
    log.info("validateAuthenticatedUser - 시작: path userEmail={}, authenticated user={}", userEmail,
        userDetails.getUsername());

    if (!userEmail.equals(userDetails.getUsername())) {
      log.error("validateAuthenticatedUser - 실패: path userEmail={}, authenticated user={}", userEmail,
          userDetails.getUsername());
      throw new UserUnauthorizedException(ErrorCode.USER_UNAUTHORIZED.getMessage());
    }
    log.info("validateAuthenticatedUser - 성공: 사용자 인증 완료");
  }
}
