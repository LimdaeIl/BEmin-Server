package run.bemin.api.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import run.bemin.api.category.dto.CategoryDto;
import run.bemin.api.category.dto.request.CreateCategoryRequestDto;
import run.bemin.api.category.dto.request.UpdateCategoryRequestDto;
import run.bemin.api.category.entity.Category;
import run.bemin.api.category.exception.CategoryAlreadyExistsByNameException;
import run.bemin.api.category.exception.CategoryNotFoundException;
import run.bemin.api.category.repository.CategoryRepository;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CategoryService categoryService;


  @Test
  @DisplayName("[성공|카테고리] 카테고리 생성하기")
  void givenCreateCategoryRequestAndUser_whenCategoryNameNotDuplicate_thenCreateCategorySucceeds() {
    // Given: 요청 DTO 와 인증된 사용자 정보를 준비
    CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Test Category");
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    // 1. 기존에 같은 이름의 카테고리가 없으며, 사용자도 존재한다고 가정
    when(categoryRepository.existsCategoryByName(requestDto.name())).thenReturn(false);
    when(userRepository.existsByUserEmail("test@example.com")).thenReturn(true);

    // 2. Category.create()를 통해 생성된 카테고리를 저장했다고 가정
    Category savedCategory = Category.create("Test Category");
    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

    // When: 서비스 메서드 호출
    CategoryDto result = categoryService.createCategory(requestDto, userDetails);

    // Then: 결과 검증
    assertNotNull(result);
    assertEquals("Test Category", result.name());
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("[실패|카테고리|이름 중복] 카테고리 생성하기 - CategoryAlreadyExistsByNameException 발생")
  void givenCreateCategoryRequest_whenCategoryNameIsDuplicate_thenThrowsCategoryAlreadyExistsByNameException() {
    // Given: 이미 존재하는 카테고리 이름으로 요청
    CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("Test Category");
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

    // 1. 새로운 이름에 대해 중복이 있다고 가정(중복 여부 확인에서 true 반환)
    when(categoryRepository.existsCategoryByName(requestDto.name())).thenReturn(true);

    // When & Then: 중복인 경우 CategoryAlreadyExistsByNameException 이 발생해야 함
    CategoryAlreadyExistsByNameException exception = assertThrows(
        CategoryAlreadyExistsByNameException.class,
        () -> categoryService.createCategory(requestDto, userDetails)
    );
    // 예외 메시지가 요청한 카테고리 이름을 포함하는지 검증 (선택 사항)
    assertTrue(exception.getMessage().contains("Test Category"));

    // save 메서드는 호출되지 않아야 함
    verify(categoryRepository, never()).save(any(Category.class));
  }

  @Test
  @DisplayName("[성공|카테고리] 카테고리 수정하기")
  void givenExistingCategoryAndValidUpdateRequest_whenUpdateCategory_thenCategoryUpdatedSuccessfully() {
    // Given: 기존 카테고리가 존재하는 상황
    UUID categoryId = UUID.randomUUID();
    UpdateCategoryRequestDto updateRequest = new UpdateCategoryRequestDto("Updated Category", false);
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    // 1. 새로운 이름에 대해 중복이 없다고 가정
    when(categoryRepository.existsCategoryByName(updateRequest.name())).thenReturn(false);

    // 2. 기존 카테고리를 조회하고 업데이트 후 저장한다고 가정
    Category existingCategory = Category.create("Old Category");
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

    // When: 업데이트 실행
    CategoryDto result = categoryService.updatedCategory(categoryId, updateRequest, userDetails);

    // Then: 결과 검증
    assertNotNull(result);
    assertEquals("Updated Category", result.name());
    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).save(existingCategory);
  }

  @Test
  @DisplayName("[실패|카테고리|ID 없음] 카테고리 수정하기 - CategoryNotFoundException 발생")
  void givenNonexistentCategoryId_whenUpdateCategory_thenThrowsCategoryNotFoundException() {
    // Given: 존재하지 않는 카테고리 ID
    UUID categoryId = UUID.randomUUID();
    UpdateCategoryRequestDto updateRequest = new UpdateCategoryRequestDto("Updated Category", false);
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);

    // 카테고리 조회 시 Optional.empty()를 반환
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

    // When & Then: 존재하지 않는 카테고리인 경우 CategoryNotFoundException 이 발생해야 함
    CategoryNotFoundException exception = assertThrows(
        CategoryNotFoundException.class,
        () -> categoryService.updatedCategory(categoryId, updateRequest, userDetails)
    );
    // 예외 메시지에 카테고리 ID가 포함되어 있는지 검증 (선택 사항)
    assertTrue(exception.getMessage().contains(categoryId.toString()));
  }

  @Test
  @DisplayName("[성공|카테고리] 카테고리 삭제하기")
  void givenExistingCategoryAndUser_whenSoftDeleteCategory_thenCategoryIsSoftDeleted() {
    // Given: 삭제할 카테고리가 존재하는 상황
    UUID categoryId = UUID.randomUUID();
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    Category existingCategory = Category.create("Test Category");
    when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
    when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

    // When: 소프트 삭제 실행
    CategoryDto result = categoryService.softDeleteCategory(categoryId, userDetails);

    // Then: 결과 검증 (isDeleted 가 true 여야 함)
    assertNotNull(result);
    assertTrue(result.isDeleted());
    verify(categoryRepository).findById(categoryId);
    verify(categoryRepository).save(existingCategory);
  }

  @Test
  @DisplayName("[성공|카테고리] 모든 카테고리 조회하기")
  void givenValidMultipleCreateCategoryRequestsAndUser_whenCreateCategories_thenCategoriesCreatedSuccessfully() {
    // Given: 여러 카테고리 생성 요청
    CreateCategoryRequestDto req1 = new CreateCategoryRequestDto("Category1");
    CreateCategoryRequestDto req2 = new CreateCategoryRequestDto("Category2");
    List<CreateCategoryRequestDto> requestList = List.of(req1, req2);

    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    when(userDetails.getUsername()).thenReturn("test@example.com");

    // 1. 이미 존재하는 카테고리 이름이 없다고 가정
    when(categoryRepository.findNamesIn(anyList())).thenReturn(new ArrayList<>());

    // 각 카테고리 생성 후 저장
    Category cat1 = Category.create("Category1");
    Category cat2 = Category.create("Category2");
    List<Category> savedCategories = List.of(cat1, cat2);
    when(categoryRepository.saveAll(anyList())).thenReturn(savedCategories);

    // When: 배치 생성 실행
    List<CategoryDto> result = categoryService.createCategories(requestList, userDetails);

    // Then: 결과 검증
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(categoryRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("[성공|카테고리] 카테고리 이름으로 조회하기")
  void givenSearchNameAndPagination_whenGetAdminAllCategory_thenReturnMatchingCategoriesPage() {
    // Given: 이름 검색어가 있는 경우
    String searchName = "Test";
    int page = 0;
    int size = 10;
    Category category = Category.create("Test Category");
    Page<Category> categoryPage = new PageImpl<>(List.of(category));
    when(categoryRepository.findAllByNameContainingIgnoreCase(eq(searchName), any(Pageable.class)))
        .thenReturn(categoryPage.map(c -> c));

    // When: 조회 실행
    Page<CategoryDto> result = categoryService.getAdminAllCategory(searchName, page, size, "createdAt", true);

    // Then: 결과 검증
    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(categoryRepository).findAllByNameContainingIgnoreCase(eq(searchName), any(Pageable.class));
  }
}
