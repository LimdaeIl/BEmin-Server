package run.bemin.api.store.controller;

import static run.bemin.api.store.dto.StoreResponseCode.STORE_CREATED;
import static run.bemin.api.store.dto.StoreResponseCode.STORE_DELETED;
import static run.bemin.api.store.dto.StoreResponseCode.STORE_FETCHED;
import static run.bemin.api.store.dto.StoreResponseCode.STORE_UPDATED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.category.dto.request.UpdateMinimumPriceRequestDto;
import run.bemin.api.category.dto.request.UpdateStoreNameRequestDto;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.dto.StoreDto;
import run.bemin.api.store.dto.request.CreateStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateAddressInStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateAllStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateCategoriesInStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateStoreActivationRequestDto;
import run.bemin.api.store.dto.request.UpdateStoreOwnerRequestDto;
import run.bemin.api.store.dto.request.UpdateStorePhoneRequestDto;
import run.bemin.api.store.dto.request.UpdateStoresActivationStatusRequestDto;
import run.bemin.api.store.service.StoreService;

/**
 * The type Admin store controller.
 */
@Tag(name = "[관리자]가게", description = "AdminStoreController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/stores")
@RestController
public class AdminStoreController {

  private final StoreService storeService;

  /**
   * Gets stores by user email.
   *
   * @param userDetails the user details
   * @return the stores by user email
   */
  @Operation(summary = "[관리자] 로그인된 사용자의 이메일로 단건 가게 목록을 조회하기)",
      description = """
            로그인 유지(필수): 로그인된 사용자의 이메일로 조회합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @GetMapping("/by-user")
  public ResponseEntity<ApiResponse<StoreDto>> getStoresByUserEmail(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    StoreDto storeDto = storeService.getStoreByUserEmail(userDetails.getUsername());

    return ResponseEntity.status(STORE_FETCHED.getStatus())
        .body(ApiResponse.from(STORE_FETCHED.getStatus(), STORE_FETCHED.getMessage(), storeDto));
  }

  /**
   * Create store response entity.
   *
   * @param requestDto  the request dto
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 새로운 가게 등록하기)",
      description = """
            name(필수): 가게 이름은 한글, 영문, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~50글자 이내여야 합니다.
            phone(필수): 가게 전화번호는 숫자와 '-' 기호만 입력 가능합니다.
            minimumPrice(필수): 가게 최소 주문 금액은 0원 이상이어야 합니다.
            userEmail(필수): 이메일 형식에 맞는 주소이여야 합니다.
            zoneCode(필수): 국가기초구역번호는 5자리 숫자입니다.
            bcode(필수): 가게 법정동/법정리 코드는 10자리 숫자입니다.
            jibunAddress(필수): 가게 지번 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
            roadAddress(필수): 가게 도로 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
            detail(필수): 가게 상세 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 1~84글자 이내여야 합니다.
            categoryIds(필수): 최대 4개의 카테고리를 등록할 수 있습니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PostMapping
  public ResponseEntity<ApiResponse<StoreDto>> createStore(@RequestBody @Valid CreateStoreRequestDto requestDto,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
    StoreDto storeDto = storeService.createStore(requestDto, userDetails);

    return ResponseEntity.status(STORE_CREATED.getStatus())
        .body(ApiResponse.from(STORE_CREATED.getStatus(), STORE_CREATED.getMessage(), storeDto));
  }

  /**
   * Update all store response entity.
   *
   * @param storeId     the store id
   * @param requestDto  the request dto
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 정보를 수정하기)",
      description = """
            name(필수): 가게 이름은 한글, 영문, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~50글자 이내여야 합니다.
            phone(필수): 가게 전화번호는 숫자와 '-' 기호만 입력 가능합니다.
            minimumPrice(필수): 가게 최소 주문 금액은 0원 이상이어야 합니다.
            isActive(필수): 가게 활성화 여부 설정 입니다.
            zoneCode(필수): 국가기초구역번호는 5자리 숫자입니다.
            bcode(필수): 가게 법정동/법정리 코드는 10자리 숫자입니다.
            jibunAddress(필수): 가게 지번 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
            roadAddress(필수): 가게 도로 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
            detail(필수): 가게 상세 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 1~84글자 이내여야 합니다.
            categoryIds(필수): 최대 4개의 카테고리를 등록할 수 있습니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreDto>> updateAllStore(@PathVariable("storeId") UUID storeId,
                                                              @RequestBody @Valid UpdateAllStoreRequestDto requestDto,
                                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
    StoreDto storeDto = storeService.updateAllStore(storeId, requestDto, userDetails);

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Soft delete store response entity.
   *
   * @param storeId     the store id
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게를 삭제하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          """)
  @PreAuthorize("hasRole('MASTER')")
  @DeleteMapping("/{storeId}")
  public ResponseEntity<ApiResponse<StoreDto>> softDeleteStore(@PathVariable("storeId") UUID storeId,
                                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {

    StoreDto storeDto = storeService.softDeleteStore(storeId, userDetails);

    return ResponseEntity.status(STORE_DELETED.getStatus())
        .body(ApiResponse.from(STORE_DELETED.getStatus(), STORE_DELETED.getMessage(), storeDto));
  }

  /**
   * Gets admin stores.
   *
   * @param name the name
   * @param page the page
   * @param size the size
   * @return the admin stores
   */
  @Operation(summary = "[관리자] 모든 가게 정보를 조회하기(삭제포함))",
      description = """
          name(선택) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          page(선택) : 조회할 페이지 번호(defaultValue = 0) \n
          size(선택) : 한 페이지에 포함될 항목 수(defaultValue = 10)
          """)
  @PreAuthorize("hasRole('MASTER')")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<StoreDto>>> getAdminStores(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    Page<StoreDto> adminStores = storeService.getAdminAllStores(name, page, size, "createdAt", true);
    return ResponseEntity.status(STORE_FETCHED.getStatus())
        .body(ApiResponse.from(STORE_FETCHED.getStatus(), STORE_FETCHED.getMessage(), adminStores));
  }

  /**
   * Update categories in store response entity.
   *
   * @param storeId     the store id
   * @param requestDto  the request dto
   * @param userDetails the user details
   * @return the response entity
   */
// 가게 카테고리 수정하기
  @Operation(summary = "[관리자] 특정 가게를 삭제하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호 \n
          categoryIds(필수): 최대 4개의 카테고리를 등록할 수 있습니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/categories")
  public ResponseEntity<ApiResponse<StoreDto>> updateCategoriesInStore(@PathVariable("storeId") UUID storeId,
                                                                       @RequestBody @Valid UpdateCategoriesInStoreRequestDto requestDto,
                                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
    StoreDto storeDto = storeService.updateCategoriesInStore(storeId, requestDto, userDetails);

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update address in store response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 주소 수정하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          zoneCode(필수): 국가기초구역번호는 5자리 숫자입니다.
          bcode(필수): 가게 법정동/법정리 코드는 10자리 숫자입니다.
          jibunAddress(필수): 가게 지번 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
          roadAddress(필수): 가게 도로 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 7~84글자 이내여야 합니다.
          detail(필수): 가게 상세 주소는 한글, 영문, 숫자, 특수 문자(-, ,), 공백만 입력 가능 최대 1~84글자 이내여야 합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/address")
  public ResponseEntity<ApiResponse<StoreDto>> updateAddressInStore(@PathVariable("storeId") UUID storeId,
                                                                    @RequestBody @Valid UpdateAddressInStoreRequestDto requestDto) {
    StoreDto storeDto = storeService.updateAddressInStore(storeId, requestDto);

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update is active in store response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
// 특정 가게 활성화 여부 설정하기
  @Operation(summary = "[관리자] 특정 가게 활성화 여부 설정하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          isActive(필수): 가게 활성화 여부 설정 입니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/stores/{storeId}/active")
  public ResponseEntity<ApiResponse<StoreDto>> updateIsActiveInStore(@PathVariable("storeId") UUID storeId,
                                                                     @RequestBody @Valid UpdateStoreActivationRequestDto requestDto) {

    StoreDto storeDto = storeService.updateIsActiveInStore(storeId, requestDto.isActive());

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update stores activation status response entity.
   *
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 여러 가게 활성화 여부 설정하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          isActive(필수): 가게 활성화 여부 설정 입니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/active")
  public ResponseEntity<ApiResponse<List<StoreDto>>> updateStoresActivationStatus(
      @RequestBody @Valid UpdateStoresActivationStatusRequestDto requestDto) {
    List<StoreDto> updatedStores = storeService.updateStoresActivationStatus(requestDto.storeActivationStatusList());

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), updatedStores));
  }

  /**
   * Update minimum price in store response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 최소주문금액 수정하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          minimumPrice(필수): 가게 최소 주문 금액은 0원 이상이어야 합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/minimum-price")
  public ResponseEntity<ApiResponse<StoreDto>> updateMinimumPriceInStore(@PathVariable("storeId") UUID storeId,
                                                                         @RequestBody @Valid UpdateMinimumPriceRequestDto requestDto) {
    StoreDto storeDto = storeService.updateMinimumPriceInStore(storeId, requestDto);

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update name in store response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 이름 수정하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          name(필수): 가게 이름은 한글, 영문, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~50글자 이내여야 합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/name")
  public ResponseEntity<ApiResponse<StoreDto>> updateNameInStore(@PathVariable("storeId") UUID storeId,
                                                                 @RequestBody @Valid UpdateStoreNameRequestDto requestDto) {
    StoreDto storeDto = storeService.updateNameInStore(storeId, requestDto);

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update store owner response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 주인 변경하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          userEmail(필수): 이메일 형식에 맞는 주소이여야 합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/owner")
  public ResponseEntity<ApiResponse<StoreDto>> updateStoreOwner(@PathVariable("storeId") UUID storeId,
                                                                @RequestBody @Valid UpdateStoreOwnerRequestDto requestDto) {
    StoreDto storeDto = storeService.updateStoreOwner(storeId, requestDto.userEmail());

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }

  /**
   * Update phone in store response entity.
   *
   * @param storeId    the store id
   * @param requestDto the request dto
   * @return the response entity
   */
  @Operation(summary = "[관리자] 특정 가게 전화번호 변경하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          phone(필수): 가게 전화번호는 숫자와 '-' 기호만 입력 가능합니다.
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{storeId}/phone")
  public ResponseEntity<ApiResponse<StoreDto>> updatePhoneInStore(@PathVariable("storeId") UUID storeId,
                                                                  @RequestBody @Valid UpdateStorePhoneRequestDto requestDto) {

    StoreDto storeDto = storeService.updatePhoneInStore(storeId, requestDto.phone());

    return ResponseEntity.status(STORE_UPDATED.getStatus())
        .body(ApiResponse.from(STORE_UPDATED.getStatus(), STORE_UPDATED.getMessage(), storeDto));
  }
}
