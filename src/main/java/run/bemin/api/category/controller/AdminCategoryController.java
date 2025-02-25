package run.bemin.api.category.controller;

import static run.bemin.api.category.dto.CategoryResponseCode.CATEGORIES_FETCHED;
import static run.bemin.api.category.dto.CategoryResponseCode.CATEGORY_CREATED;
import static run.bemin.api.category.dto.CategoryResponseCode.CATEGORY_DELETED;
import static run.bemin.api.category.dto.CategoryResponseCode.CATEGORY_UPDATED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import run.bemin.api.category.dto.CategoryDto;
import run.bemin.api.category.dto.request.CreateCategoryRequestDto;
import run.bemin.api.category.dto.request.UpdateCategoryRequestDto;
import run.bemin.api.category.service.CategoryService;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.security.UserDetailsImpl;

/**
 * The type Admin category controller.
 */
@Tag(name = "[관리자]카테고리", description = "AdminCategoryController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
@RestController
public class AdminCategoryController {

  private final CategoryService categoryService;


  /**
   * Create category response entity.
   *
   * @param requestDto  the request dto
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 단 건 카테고리 등록하기",
      description = """
          name(필수) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PostMapping
  public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
      @RequestBody CreateCategoryRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
    CategoryDto categoryDto = categoryService.createCategory(requestDto, userDetails);

    return ResponseEntity
        .status(CATEGORY_CREATED.getStatus())
        .body(ApiResponse.from(CATEGORY_CREATED.getStatus(), CATEGORY_CREATED.getMessage(), categoryDto));
  }

  /**
   * Create categories response entity.
   *
   * @param requestDtoList the request dto list
   * @param userDetails    the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 두 개 이상의 카테고리 등록하기",
      description = """
          name(필수) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PostMapping("/batch")
  public ResponseEntity<ApiResponse<List<CategoryDto>>> createCategories(
      @RequestBody List<CreateCategoryRequestDto> requestDtoList,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    List<CategoryDto> categories = categoryService.createCategories(requestDtoList, userDetails);

    return ResponseEntity
        .status(CATEGORY_CREATED.getStatus())
        .body(ApiResponse.from(CATEGORY_CREATED.getStatus(), CATEGORY_CREATED.getMessage(), categories));
  }

  /**
   * Gets admin categories.
   *
   * @param name the name
   * @param page the page
   * @param size the size
   * @return the admin categories
   */
  @Operation(summary = "[관리자] 카테고리 조회하기(삭제 포함)",
      description = """
          name(선택) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          page(선택) : 조회할 페이지 번호(defaultValue = 0) \n
          size(선택) : 한 페이지에 포함될 항목 수(defaultValue = 10) \n
          """)
  @PreAuthorize("hasRole('MASTER')")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<CategoryDto>>> getAdminCategories(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size
  ) {
    Page<CategoryDto> categories = categoryService.getAdminAllCategory(
        name,
        page,
        size,
        "createdAt",
        true);
    return ResponseEntity.ok(
        ApiResponse.from(CATEGORIES_FETCHED.getStatus(), CATEGORIES_FETCHED.getMessage(), categories)
    );
  }

  /**
   * Update category response entity.
   *
   * @param categoryId  the category id
   * @param requestDto  the request dto
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 카테고리 수정하기",
      description = """
          name(필수) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          isDeleted(필수) : 카테고리 삭제 여부 입니다. \n
          """)
  @PreAuthorize("hasRole('MASTER')")
  @PatchMapping("/{categoryId}")
  public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
      @PathVariable UUID categoryId,
      @RequestBody UpdateCategoryRequestDto requestDto,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    CategoryDto categoryDto = categoryService.updatedCategory(categoryId, requestDto, userDetails);

    return ResponseEntity
        .status(CATEGORY_UPDATED.getStatus())
        .body(ApiResponse.from(CATEGORY_UPDATED.getStatus(), CATEGORY_UPDATED.getMessage(), categoryDto));
  }

  /**
   * Soft delete category response entity.
   *
   * @param categoryId  the category id
   * @param userDetails the user details
   * @return the response entity
   */
  @Operation(summary = "[관리자] 카테고리 삭제하기",
      description = """
          categoryId(필수) : 삭제할 카테고리 고유 식별 번호 \n
          """)
  @PreAuthorize("hasRole('MASTER')")
  @DeleteMapping("/{categoryId}")
  public ResponseEntity<ApiResponse<CategoryDto>> softDeleteCategory(
      @PathVariable UUID categoryId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    CategoryDto categoryDto = categoryService.softDeleteCategory(categoryId, userDetails);

    return ResponseEntity
        .status(CATEGORY_DELETED.getStatus())
        .body(ApiResponse.from(CATEGORY_DELETED.getStatus(), CATEGORY_DELETED.getMessage(), categoryDto));
  }
}
