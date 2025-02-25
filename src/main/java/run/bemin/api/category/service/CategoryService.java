package run.bemin.api.category.service;

import static run.bemin.api.general.exception.ErrorCode.CATEGORY_ALREADY_EXISTS;
import static run.bemin.api.general.exception.ErrorCode.CATEGORY_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
import run.bemin.api.category.dto.response.GetCategoryResponseDto;
import run.bemin.api.category.entity.Category;
import run.bemin.api.category.exception.CategoryAlreadyExistsByNameException;
import run.bemin.api.category.exception.CategoryNotFoundException;
import run.bemin.api.category.repository.CategoryRepository;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

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

    Category category = Category.create(requestDto.name());
    categoryRepository.save(category);

    return CategoryDto.fromEntity(category);
  }

  @Transactional
  public List<CategoryDto> createCategories(List<CreateCategoryRequestDto> requestDtoList,
                                            UserDetailsImpl userDetails) {
    // 요청된 모든 카테고리 이름을 추출
    List<String> requestedNames = requestDtoList.stream()
        .map(CreateCategoryRequestDto::name)
        .collect(Collectors.toList());

    // 한 번의 쿼리로 이미 존재하는 카테고리 이름 조회
    List<String> existingNames = categoryRepository.findNamesIn(requestedNames);
    if (!existingNames.isEmpty()) {
      throw new CategoryAlreadyExistsByNameException(
          CATEGORY_ALREADY_EXISTS.getMessage() + ": " + existingNames);
    }

    // 중복이 없는 경우, 카테고리 생성
    List<Category> categoriesToCreate = requestDtoList.stream()
        .map(dto -> Category.create(dto.name()))
        .collect(Collectors.toList());

    // saveAll() 결과를 List 로 변환
    Iterable<Category> savedCategoriesIterable = categoryRepository.saveAll(categoriesToCreate);
    List<Category> savedCategories = new ArrayList<>();
    savedCategoriesIterable.forEach(savedCategories::add);

    return savedCategories.stream()
        .map(CategoryDto::fromEntity)
        .collect(Collectors.toList());
  }


  @Transactional(readOnly = true)
  public Page<CategoryDto> getAdminAllCategory(String name,
                                               Integer page,
                                               Integer size,
                                               String sortBy,
                                               Boolean isAsc) {
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    if (name != null && !name.trim().isEmpty()) {
      // 이름 검색이 있으면 검색 조건을 적용
      return categoryRepository.findAllByNameContainingIgnoreCase(name, pageable)
          .map(CategoryDto::fromEntity);
    }
    // 검색어가 없으면 전체 조회
    return categoryRepository.findAll(pageable)
        .map(CategoryDto::fromEntity);
  }


  @Transactional(readOnly = true)
  public Page<GetCategoryResponseDto> getAllCategory(String name,
                                                     Boolean isDeleted,
                                                     Integer page,
                                                     Integer size,
                                                     String sortBy,
                                                     Boolean isAsc) {
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Boolean filterDeleted = Optional.ofNullable(isDeleted).orElse(false);

    Page<Category> categoryPage = (name != null && !name.trim().isEmpty())
        ? categoryRepository.findAllByIsDeletedAndNameContainingIgnoreCase(filterDeleted, name, pageable)
        : categoryRepository.findAllByIsDeleted(filterDeleted, pageable);

    return categoryPage.map(GetCategoryResponseDto::fromEntity);
  }

  @Transactional
  public CategoryDto updatedCategory(UUID categoryId, UpdateCategoryRequestDto requestDto,
                                     UserDetailsImpl userDetails) {
    existsCategoryByName(requestDto.name());

    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(CATEGORY_NOT_FOUND.name() + ": " + categoryId));

    category.update(userDetails.getUsername(), requestDto.name(), requestDto.isDeleted());
    Category savedCategory = categoryRepository.save(category);

    return CategoryDto.fromEntity(savedCategory);
  }


  @Transactional
  public CategoryDto softDeleteCategory(UUID categoryId, UserDetailsImpl userDetails) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(CATEGORY_NOT_FOUND.name() + ": " + categoryId));

    category.softDelete(userDetails.getUsername());
    Category softDeletedCategory = categoryRepository.save(category);

    return CategoryDto.fromEntity(softDeletedCategory);
  }


}
