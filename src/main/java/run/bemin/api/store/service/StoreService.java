package run.bemin.api.store.service;

import static run.bemin.api.general.exception.ErrorCode.CATEGORY_NOT_FOUND;
import static run.bemin.api.general.exception.ErrorCode.STORE_ALREADY_DELETE;
import static run.bemin.api.general.exception.ErrorCode.STORE_NOT_FOUND;
import static run.bemin.api.general.exception.ErrorCode.USER_NOT_FOUND;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.category.dto.request.UpdateMinimumPriceRequestDto;
import run.bemin.api.category.dto.request.UpdateStoreNameRequestDto;
import run.bemin.api.category.entity.Category;
import run.bemin.api.category.exception.CategoryNotFoundException;
import run.bemin.api.category.repository.CategoryRepository;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.dto.StoreDto;
import run.bemin.api.store.dto.request.CreateStoreRequestDto;
import run.bemin.api.store.dto.request.StoreActivationStatusDto;
import run.bemin.api.store.dto.request.UpdateAddressInStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateAllStoreRequestDto;
import run.bemin.api.store.dto.request.UpdateCategoriesInStoreRequestDto;
import run.bemin.api.store.dto.response.GetStoreResponseDto;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.entity.StoreAddress;
import run.bemin.api.store.exception.CategoryCountExceededException;
import run.bemin.api.store.exception.StoreNotFoundException;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.exception.UserNotFoundException;
import run.bemin.api.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class StoreService {

  private static final int MAX_CATEGORY_COUNT = 4;

  private final StoreRepository storeRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public StoreDto getStoreByUserEmail(String userEmail) {
    return storeRepository.findByOwner_UserEmail(userEmail).stream()
        .findFirst()
        .map(StoreDto::fromEntity)
        .orElse(null); // 가게가 없다면 null 반환하기
  }

  // 카테고리 수(MAX_CATEGORY_COUNT) 검증 및 존재 여부 검증을 함께 수행
  private List<Category> validateAndFetchCategories(List<UUID> categoryIds) {
    if (categoryIds.size() > MAX_CATEGORY_COUNT) {
      throw new CategoryCountExceededException("최대 " + MAX_CATEGORY_COUNT + "개의 카테고리만 선택할 수 있습니다.");
    }
    List<Category> categories = categoryRepository.findAllByIdIn(categoryIds);
    if (categories.size() != categoryIds.size()) {
      throw new CategoryNotFoundException(CATEGORY_NOT_FOUND.getMessage());
    }
    return categories;
  }

  @Transactional
  public StoreDto createStore(CreateStoreRequestDto requestDto, UserDetailsImpl userDetails) {
    List<Category> categories = validateAndFetchCategories(requestDto.categoryIds());

    // 새로운 주인 정보 가져오기
    User newOwner = userRepository.findByUserEmail(requestDto.userEmail())
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage() + ": " + requestDto.userEmail()));

    StoreAddress storeAddress = StoreAddress.builder()
        .zoneCode(requestDto.zoneCode())
        .bcode(requestDto.bcode())
        .jibunAddress(requestDto.jibunAddress())
        .roadAddress(requestDto.roadAddress())
        .detail(requestDto.detail())
        .build();

    // User 객체를 DTO 의 userEmail 대신 인증된 사용자에서 가져옴
    Store store = Store.create(
        requestDto.name(),
        requestDto.phone(),
        requestDto.minimumPrice(),
        true, // isActive 기본값
        storeAddress,
        newOwner // 수정된 부분: User 객체 전달
    );

    storeAddress.setStore(store); // 양방향 연관관계 설정: StoreAddress 에 Store 할당

    categories.forEach(store::addCategory);

    Store savedStore = storeRepository.save(store); // Store 저장 (Cascade 옵션 덕분에 주소도 함께 저장됨)
    return StoreDto.fromEntity(savedStore);
  }

  @Transactional
  public StoreDto updateAllStore(UUID storeId, UpdateAllStoreRequestDto requestDto, UserDetailsImpl userDetails) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    List<Category> categories = validateAndFetchCategories(requestDto.categoryIds());

    // 여기서 userDetails.getUser()를 사용하여 User 객체를 전달합니다.
    store.update(
        requestDto.name(),
        requestDto.phone(),
        requestDto.minimumPrice(),
        requestDto.isActive(),
        userDetails.getUser()  // 수정된 부분: User 객체 전달
    );

    // 가게 주소 업데이트 (가게 주소가 null 이 아니라고 가정)
    store.getStoreAddress().update(
        requestDto.zoneCode(),
        requestDto.bcode(),
        requestDto.jibunAddress(),
        requestDto.roadAddress(),
        requestDto.detail()
    );

    // 카테고리 업데이트 (기존 목록 삭제 후 재설정)
    store.updateCategories(categories, userDetails.getUsername());

    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }


  @Transactional(readOnly = true)
  public GetStoreResponseDto getStore(UUID storeId) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    if (store.getIsDeleted()) {
      throw new RuntimeException(STORE_ALREADY_DELETE.getMessage());
    }
    return GetStoreResponseDto.fromEntity(store);
  }

  @Transactional
  public StoreDto softDeleteStore(UUID storeId, UserDetailsImpl userDetails) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    store.softDelete(userDetails.getUsername());
    Store updatedStore = storeRepository.save(store);

    return StoreDto.fromEntity(updatedStore);
  }

  @Transactional(readOnly = true)
  public Page<GetStoreResponseDto> getAllStores(String storeName,
                                                Boolean isDeleted,
                                                Integer page,
                                                Integer size,
                                                String sortBy,
                                                Boolean isAsc) {
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Boolean filterDeleted = Optional.ofNullable(isDeleted).orElse(false);

    Page<Store> storePage = (storeName != null && !storeName.trim().isEmpty())
        ? storeRepository.findAllByIsDeletedAndNameContainingIgnoreCase(filterDeleted, storeName, pageable)
        : storeRepository.findAllByIsDeleted(filterDeleted, pageable);

    return storePage.map(GetStoreResponseDto::fromEntity);
  }


  @Transactional(readOnly = true)
  public Page<StoreDto> getAdminAllStores(String name,
                                          Integer page,
                                          Integer size,
                                          String sortBy,
                                          Boolean isAsc) {
    Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    if (name != null && !name.trim().isEmpty()) {
      return storeRepository.findAllByNameContainingIgnoreCase(name, pageable)
          .map(StoreDto::fromEntity);
    }
    return storeRepository.findAll(pageable)
        .map(StoreDto::fromEntity);
  }

  public StoreDto updateCategoriesInStore(
      UUID storeId,
      UpdateCategoriesInStoreRequestDto requestDto,
      UserDetailsImpl userDetails) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    List<Category> categories = validateAndFetchCategories(requestDto.categoryIds());

    // 카테고리 업데이트 (기존 목록 삭제 후 재설정)
    store.updateCategories(categories, userDetails.getUsername());

    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }

  @Transactional
  public StoreDto updateAddressInStore(
      UUID storeId,
      UpdateAddressInStoreRequestDto requestDto) {

    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    // 가게 주소 업데이트 (가게 주소가 null 이 아니라고 가정)
    store.getStoreAddress().update(
        requestDto.zoneCode(),
        requestDto.bcode(),
        requestDto.jibunAddress(),
        requestDto.roadAddress(),
        requestDto.detail()
    );

    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }

  @Transactional
  public StoreDto updateIsActiveInStore(UUID storeId, Boolean isActive) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    store.updateIsActive(isActive);
    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }

  @Transactional
  public List<StoreDto> updateStoresActivationStatus(List<StoreActivationStatusDto> statusList) {
    List<UUID> storeIds = statusList.stream()
        .map(StoreActivationStatusDto::storeId)
        .collect(Collectors.toList());

    List<Store> stores = storeRepository.findAllById(storeIds);

    if (stores.isEmpty()) {
      throw new StoreNotFoundException(STORE_NOT_FOUND.getMessage());
    }

    // 각 가게별로 다른 상태 적용
    stores.forEach(store -> {
      statusList.stream()
          .filter(s -> s.storeId().equals(store.getId()))
          .findFirst()
          .ifPresent(s -> store.updateIsActive(s.isActive()));
    });

    storeRepository.saveAll(stores);

    return stores.stream()
        .map(StoreDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Transactional
  public StoreDto updateMinimumPriceInStore(UUID storeId, UpdateMinimumPriceRequestDto requestDto) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    store.updateMinimumPrice(requestDto.minimumPrice());
    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }


  @Transactional
  public StoreDto updateNameInStore(UUID storeId, UpdateStoreNameRequestDto requestDto) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    store.updateName(requestDto.name());
    Store updatedStore = storeRepository.save(store);
    return StoreDto.fromEntity(updatedStore);
  }

  @Transactional
  public StoreDto updateStoreOwner(UUID storeId, String userEmail) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage()));

    // 새로운 주인 정보 가져오기
    User newOwner = userRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND.getMessage() + ": " + userEmail));

    // 가게의 주인 변경 로직 (가게 엔티티에 setter 또는 업데이트 메서드 사용)
    store.updateOwner(newOwner);
    storeRepository.save(store);

    return StoreDto.fromEntity(store);
  }

  @Transactional
  public StoreDto updatePhoneInStore(UUID storeId, String newPhone) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new StoreNotFoundException(STORE_NOT_FOUND.getMessage() + ": " + storeId));

    store.updatePhone(newPhone);
    storeRepository.save(store);

    return StoreDto.fromEntity(store);
  }

  @Transactional
  public Page<Store> searchStores(String categoryName, String storeName, Pageable pageable) {
    return storeRepository.searchStores(categoryName, storeName, pageable);
  }
}