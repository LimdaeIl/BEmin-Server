package run.bemin.api.user.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.user.dto.request.UserAddressRequestDto;
import run.bemin.api.user.dto.response.UserAddressResponseDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserAddress;
import run.bemin.api.user.exception.UserAddressNotFoundException;
import run.bemin.api.user.exception.UserNotFoundException;
import run.bemin.api.user.exception.UserUnauthorizedException;
import run.bemin.api.user.repository.UserAddressRepository;
import run.bemin.api.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserAddressService {

  private final UserRepository userRepository;
  private final UserAddressRepository userAddressRepository;

  /**
   * 특정 회원의 배달 주소 추가 - 무조건 대표 주소로 등록
   */
  @Transactional
  public UserAddressResponseDto addAddress(String userEmail, UserAddressRequestDto addressRequestDto) {
    User user = userRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

    // 기존 대표 주소 해제
    resetRepresentativeAddresses(user);

    // 새 주소를 대표 주소로 생성
    UserAddress userAddress = UserAddress.builder()
        .bcode(addressRequestDto.getBcode())
        .jibunAddress(addressRequestDto.getJibunAddress())
        .roadAddress(addressRequestDto.getRoadAddress())
        .detail(addressRequestDto.getDetail())
        .isRepresentative(true)
        .user(user)
        .build();

    UserAddress savedAddress = userAddressRepository.save(userAddress);

    // 회원의 대표 주소 필드 업데이트
    user.setRepresentativeAddress(savedAddress);
    userRepository.save(user);

    return UserAddressResponseDto.fromEntity(savedAddress);
  }

  /**
   * 특정 회원의 주소 목록 조회
   */
  @Transactional(readOnly = true)
  public Page<UserAddressResponseDto> getAllAddresses(String userEmail,
                                                      Boolean isDeleted,
                                                      Integer page,
                                                      Integer size) {

    Sort sort = Sort.by(
        new Sort.Order(Sort.Direction.DESC, "isRepresentative"),
        new Sort.Order(Sort.Direction.ASC, "createdAt")
    );
    Pageable pageable = PageRequest.of(page, size, sort);
    boolean filterDeleted = Optional.ofNullable(isDeleted).orElse(false);

    User user = userRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

    Page<UserAddress> addressPage = userAddressRepository.findByUserAndIsDeleted(user, filterDeleted, pageable);
    return addressPage.map(UserAddressResponseDto::fromEntity);
  }

  /**
   * 대표 주소 변경
   */
  @Transactional
  public UserAddressResponseDto setRepresentativeAddress(String userEmail, UUID addressId) {
    User user = userRepository.findByUserEmail(userEmail)
        .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));

    // 기존 대표 주소 해제
    resetRepresentativeAddresses(user);

    // 새 대표 주소 조회 및 소유 검증
    UserAddress newRep = userAddressRepository.findById(addressId)
        .orElseThrow(() -> new UserAddressNotFoundException(ErrorCode.USER_ADDRESS_NOT_FOUND.getMessage()));
    if (!newRep.getUser().getUserEmail().equals(userEmail)) {
      throw new UserUnauthorizedException(ErrorCode.USER_UNAUTHORIZED.getMessage());
    }

    // 새 주소를 대표 주소로 업데이트
    newRep.setRepresentative(true);
    userAddressRepository.save(newRep);

    // 회원의 대표 주소 업데이트
    user.setRepresentativeAddress(newRep);
    userRepository.save(user);

    return UserAddressResponseDto.fromEntity(newRep);
  }

  /**
   * 회원의 기존 대표 주소를 모두 해제하는 공통 메서드
   */
  private void resetRepresentativeAddresses(User user) {
    List<UserAddress> existingReps = userAddressRepository.findByUserAndIsRepresentativeTrue(user);
    if (!existingReps.isEmpty()) {
      existingReps.forEach(addr -> addr.setRepresentative(false));
      userAddressRepository.saveAll(existingReps);
    }
  }
}
