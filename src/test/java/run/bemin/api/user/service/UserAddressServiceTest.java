package run.bemin.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import run.bemin.api.user.dto.request.UserAddressRequestDto;
import run.bemin.api.user.dto.response.UserAddressResponseDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserAddress;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.exception.UserAddressNotFoundException;
import run.bemin.api.user.repository.UserAddressRepository;
import run.bemin.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserAddressRepository userAddressRepository;

  @InjectMocks
  private UserAddressService userAddressService;

  private User user1;
  private User user2;
  private UserAddress addressExisting;
  private UserAddress addressNew;
  private UserAddress addressOld;
  private UserAddressRequestDto addressRequestDto;

  @BeforeEach
  void setUp() {
    user1 = User.builder()
        .userEmail("user1@test.com")
        .name("User1")
        .nickname("user1")
        .phone("010-1111-1111")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    user2 = User.builder()
        .userEmail("user2@test.com")
        .name("User2")
        .nickname("user2")
        .phone("010-2222-2222")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    // 기존 대표 주소
    addressExisting = UserAddress.builder()
        .id(UUID.randomUUID())
        .bcode("oldBcode")
        .jibunAddress("old jibun")
        .roadAddress("old road")
        .detail("old detail")
        .isRepresentative(true)
        .user(user1)
        .build();

    addressRequestDto = UserAddressRequestDto.builder()
        .bcode("newBcode")
        .jibunAddress("new jibun")
        .roadAddress("new road")
        .detail("new detail")
        .build();

    // 새로운 주소 엔티티
    addressNew = UserAddress.builder()
        .id(UUID.randomUUID())
        .bcode(addressRequestDto.getBcode())
        .jibunAddress(addressRequestDto.getJibunAddress())
        .roadAddress(addressRequestDto.getRoadAddress())
        .detail(addressRequestDto.getDetail())
        .isRepresentative(true)
        .user(user1)
        .build();

    addressOld = UserAddress.builder()
        .id(UUID.randomUUID())
        .bcode("oldBcode")
        .jibunAddress("old jibun")
        .roadAddress("old road")
        .detail("old detail")
        .isRepresentative(false)
        .user(user2)
        .build();

    addressNew = UserAddress.builder()
        .id(UUID.randomUUID())
        .bcode(addressRequestDto.getBcode())
        .jibunAddress(addressRequestDto.getJibunAddress())
        .roadAddress(addressRequestDto.getRoadAddress())
        .detail(addressRequestDto.getDetail())
        .isRepresentative(true)
        .user(user2)
        .build();


  }

  @Test
  @DisplayName("특정 회원의 배달 주소 추가 - addAddress() 성공")
  void addAddress_success() {
    // Given
    when(userRepository.findByUserEmail("user1@test.com")).thenReturn(Optional.of(user1));

    when(userAddressRepository.findByUserAndIsRepresentativeTrue(user1))
        .thenReturn(Collections.singletonList(addressExisting));

    // 새로운 주소 저장
    when(userAddressRepository.save(any(UserAddress.class))).thenReturn(addressNew);

    // When
    UserAddressResponseDto responseDto = userAddressService.addAddress("user1@test.com", addressRequestDto);

    // Then
    // 기존 대표 주소의 isRepresentative가 false로 변경되었는지 확인
    assertThat(addressExisting.getIsRepresentative()).isFalse();
    // 새로운 주소가 대표 주소로 설정되었는지 확인
    assertThat(responseDto.getBcode()).isEqualTo("newBcode");
    assertThat(responseDto.isRepresentative()).isTrue();
    // 회원의 대표 주소가 새 주소로 업데이트되었는지 확인
    assertEquals(addressNew, user1.getRepresentativeAddress());
    assertEquals(false, addressExisting.getIsRepresentative());

    System.out.println("old address: "
        + addressExisting.getIsRepresentative());
    System.out.println("new address  " + responseDto.isRepresentative());
  }

  @Test
  @DisplayName("특정 회원의 주소 목록 조회 - getAllAddresses() 성공")
  void getAllAddressesTest() {
    // Given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt"));
    // 회원 조회
    when(userRepository.findByUserEmail("user2@test.com")).thenReturn(Optional.of(user2));

    Page<UserAddress> addressPage = new PageImpl<>(Arrays.asList(addressOld, addressNew), pageable, 2);
    when(userAddressRepository.findByUserAndIsDeleted(eq(user2), eq(false), any(Pageable.class)))
        .thenReturn(addressPage);

    // When
    Page<UserAddressResponseDto> result = userAddressService.getAllAddresses("user2@test.com", false, 0, 10);

    // Then
    System.out.println("주소 목록 조회 결과: " + result);
    result.getContent().forEach(dto -> {
      System.out.println("주소: " + dto.getBcode() + ", isRepresentative: " + dto.isRepresentative());
    });

    assertThat(result).hasSize(2);
  }

  @Test
  @DisplayName("회원 대표 주소 변경 - setRepresentativeAddress() 성공")
  void setRepresentativeAddressTest() {
    // Given
    when(userRepository.findByUserEmail("user2@test.com")).thenReturn(Optional.of(user2));
    when(userAddressRepository.findByUserAndIsRepresentativeTrue(user2))
        .thenReturn(Arrays.asList(addressNew));
    // 기존 대표 주소(addressNew) 업데이트 시 호출
    when(userAddressRepository.save(addressNew)).thenReturn(addressNew);

    // 새로 대표 주소(addressOld)로 지정
    UUID newAddressId = addressOld.getId();
    when(userAddressRepository.findById(newAddressId)).thenReturn(Optional.of(addressOld));
    when(userAddressRepository.save(addressOld)).thenReturn(addressOld);
    when(userRepository.save(user2)).thenReturn(user2);

    // When
    UserAddressResponseDto responseDto = userAddressService.setRepresentativeAddress("user2@test.com", newAddressId);

    // Then
    // 기존 대표 주소(addressNew)가 해제되었고, 새 주소(addressOld)가 대표 주소로 설정
    assertThat(addressNew.getIsRepresentative()).isFalse();
    assertThat(addressOld.getIsRepresentative()).isTrue();
    assertEquals(addressOld, user2.getRepresentativeAddress());
    System.out.println("대표 주소 변경 후: " + responseDto.getJibunAddress());
  }

  @Test
  @DisplayName("회원 대표 주소 변경 실패 - 해당 주소가 존재하지 않을 경우")
  void setRepresentativeAddress_failure_notFound() {
    // Given
    when(userRepository.findByUserEmail("user1@test.com")).thenReturn(Optional.of(user1));
    UUID nonExistentId = UUID.randomUUID();
    when(userAddressRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // When & Then
    UserAddressNotFoundException exception = assertThrows(UserAddressNotFoundException.class,
        () -> userAddressService.setRepresentativeAddress("user1@test.com", nonExistentId));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

}
