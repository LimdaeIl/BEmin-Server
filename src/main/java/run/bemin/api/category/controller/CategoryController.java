package run.bemin.api.category.controller;


import static run.bemin.api.category.dto.CategoryResponseCode.CATEGORIES_FETCHED;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.category.dto.response.GetCategoryResponseDto;
import run.bemin.api.category.service.CategoryService;
import run.bemin.api.general.response.ApiResponse;

/**
 * The type Category controller.
 */
@Tag(name = "[사용자]카테고리", description = "CategoryController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@RestController
public class CategoryController {

  private final CategoryService categoryService;

  /**
   * Gets all categories.
   *
   * @param name the name
   * @param page the page
   * @param size the size
   * @return the all categories
   */
  @Operation(summary = "[사용자] 카테고리 조회하기(삭제 제외)",
      description = """
          name(선택) : 카테고리 이름은 한글, 숫자, 특수 문자(·, !), 공백만 입력 가능하며, 1~16글자 이내여야 합니다. \n
          page(선택) : 조회할 페이지 번호(defaultValue = 0) \n
          size(선택) : 한 페이지에 포함될 항목 수(defaultValue = 10) \n
          """)
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANANGER') or hasRole('MASTER') or hasRole('OWNER')")
  @GetMapping
  public ResponseEntity<ApiResponse<Page<GetCategoryResponseDto>>> getAllCategories(
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size
  ) {
    Page<GetCategoryResponseDto> categories = categoryService.getAllCategory(
        name,
        false,
        page,
        size,
        "createdAt",
        true);
    return ResponseEntity.ok(
        ApiResponse.from(CATEGORIES_FETCHED.getStatus(), CATEGORIES_FETCHED.getMessage(), categories)
    );
  }
}
