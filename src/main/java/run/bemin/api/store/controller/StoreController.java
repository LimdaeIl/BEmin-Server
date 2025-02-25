package run.bemin.api.store.controller;

import static run.bemin.api.store.dto.StoreResponseCode.STORES_FETCHED;
import static run.bemin.api.store.dto.StoreResponseCode.STORE_FETCHED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.store.dto.StoreSimpleDto;
import run.bemin.api.store.dto.response.GetStoreResponseDto;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.service.StoreService;

/**
 * The type Store controller.
 */
@Tag(name = "[사용자]가게", description = "StoreController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
@RestController
public class StoreController {

  private final StoreService storeService;

  /**
   * Gets store.
   *
   * @param storeId the store id
   * @return the store
   */
  @Operation(summary = "[사용자] 특정 가게의 상세 정보 조회하기)",
      description = """
          storeId(필수): 가게 고유 식별 번호
          """)
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANANGER') or hasRole('MASTER') or hasRole('OWNER')")
  @GetMapping("/{storeId}")
  public ResponseEntity<ApiResponse<GetStoreResponseDto>> getStore(
      @PathVariable UUID storeId) {
    GetStoreResponseDto getStoreResponseDto = storeService.getStore(storeId);

    return ResponseEntity
        .status(STORE_FETCHED.getStatus())
        .body(ApiResponse.from(STORE_FETCHED.getStatus(), STORE_FETCHED.getMessage(), getStoreResponseDto));
  }

  /**
   * Gets all stores.
   *
   * @param name the name
   * @param page the page
   * @param size the size
   * @return the all stores
   */
  @Operation(summary = "[사용자] 모든 가게 조회하기(삭제 제외))",
      description = """
          name(선택) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          page(선택) : 조회할 페이지 번호(defaultValue = 0) \n
          size(선택) : 한 페이지에 포함될 항목 수(defaultValue = 10)
          """)
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANANGER') or hasRole('MASTER') or hasRole('OWNER')")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<GetStoreResponseDto>>> getAllStores(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size
  ) {
    Page<GetStoreResponseDto> adminStores = storeService.getAllStores(
        name,
        false,
        page,
        size,
        "createdAt",
        true);
    return ResponseEntity
        .status(STORE_FETCHED.getStatus())
        .body(ApiResponse.from(STORE_FETCHED.getStatus(), STORE_FETCHED.getMessage(), adminStores));
  }

  /**
   * Search stores response entity.
   *
   * @param categoryName the category name
   * @param storeName    the store name
   * @param pageable     the pageable
   * @return the response entity
   */
  @Operation(summary = "[사용자] 모든 가게 조회하기(삭제 제외))",
      description = """
          categoryName(선택): 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다.
          storeName(선택): 가게 이름은 한글, 영문, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~50글자 이내여야 합니다.
          page(선택) : 조회할 페이지 번호(defaultValue = 0) \n
          size(선택) : 한 페이지에 포함될 항목 수(defaultValue = 10)
          """)
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANANGER') or hasRole('MASTER') or hasRole('OWNER')")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<Page<StoreSimpleDto>>> searchStores(
      @RequestParam(required = false) String categoryName,
      @RequestParam(required = false) String storeName,
      Pageable pageable) {

    Page<Store> result = storeService.searchStores(categoryName, storeName, pageable);
    Page<StoreSimpleDto> dtoPage = result.map(StoreSimpleDto::fromEntity);

    return ResponseEntity
        .status(STORES_FETCHED.getStatus())
        .body(ApiResponse.from(STORES_FETCHED.getStatus(), STORES_FETCHED.getMessage(), dtoPage));
  }
}
