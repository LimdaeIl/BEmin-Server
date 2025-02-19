package run.bemin.api.category.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.category.dto.CategoryDto;
import run.bemin.api.category.dto.request.CreateCategoryRequestDto;
import run.bemin.api.category.dto.request.SoftDeleteCategoryRequestDto;
import run.bemin.api.category.dto.request.UpdateCategoryRequestDto;
import run.bemin.api.category.entity.Category;
import run.bemin.api.category.exception.CategoryAlreadyExistsByNameException;
import run.bemin.api.category.exception.CategoryNotFoundException;
import run.bemin.api.category.repository.CategoryRepository;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService { // TODO: 회원 등록 유무/권한 체크 기능 구현이 필요하다.

  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  private void existsCategoryByName(String name) {
    if (categoryRepository.existsCategoryByName(name)) {
      throw new CategoryAlreadyExistsByNameException(name);
    }
  }

  private void existsByUserEmail(String userEmail) {
    if (!userRepository.existsByUserEmail(userEmail)) {
      throw new UsernameNotFoundException(userEmail);
    }
  }

  @Transactional
  public CategoryDto createCategory(CreateCategoryRequestDto requestDto, UserDetailsImpl userDetails) {
    existsCategoryByName(requestDto.name());

    existsByUserEmail(userDetails.getUsername());

    Category category = Category.create(requestDto.name(), userDetails.getUsername());

    categoryRepository.save(category);

    return CategoryDto.fromEntity(category);
  }


  @Transactional(readOnly = true)
  public Page<CategoryDto> getAllCategories(
      String name, Boolean isDeleted, Integer page, Integer size, String sortBy, Boolean isAsc, Boolean isAdmin) {

    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    if (isAdmin) {
      // 관리자: 삭제 여부 무관하게 전체 조회
      return categoryRepository.findAll(pageable).map(CategoryDto::fromEntity);
    }

    // 일반 사용자: 삭제되지 않은 카테고리만 조회
    Boolean filterDeleted = Optional.ofNullable(isDeleted).orElse(false);
    Page<Category> categoryPage = (name != null)
        ? categoryRepository.findAllByIsDeletedAndNameContainingIgnoreCase(filterDeleted, name, pageable)
        : categoryRepository.findAllByIsDeleted(filterDeleted, pageable);

    return categoryPage.map(CategoryDto::fromEntity);
  }

  @Transactional
  public CategoryDto updatedCategory(UpdateCategoryRequestDto requestDto) {
    existsCategoryByName(requestDto.name());

    Category category = categoryRepository.findById(requestDto.categoryId())
        .orElseThrow(() -> new CategoryNotFoundException(requestDto.categoryId().toString()));

    category.update(requestDto.userEmail(), requestDto.name(), requestDto.isDeleted());
    Category savedCategory = categoryRepository.save(category);

    return CategoryDto.fromEntity(savedCategory);
  }


  @Transactional
  public CategoryDto softDeleteCategory(SoftDeleteCategoryRequestDto requestDto) {
    Category category = categoryRepository.findById(requestDto.categoryId())
        .orElseThrow(() -> new CategoryNotFoundException(requestDto.categoryId().toString()));

    category.softDelete(requestDto.userEmail());
    Category softDeletedCategory = categoryRepository.save(category);

    return CategoryDto.fromEntity(softDeletedCategory);
  }
}
