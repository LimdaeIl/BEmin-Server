package run.bemin.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import run.bemin.api.user.dto.UserResponseDto;
import run.bemin.api.user.dto.UserUpdateRequestDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.exception.UserDuplicateNicknameException;
import run.bemin.api.user.exception.UserListNotFoundException;
import run.bemin.api.user.exception.UserNoFieldUpdatedException;
import run.bemin.api.user.exception.UserNotFoundException;
import run.bemin.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User user1, user2, user3;

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
        .isDeleted(true)
        .role(UserRoleEnum.MASTER)
        .build();

    user3 = User.builder()
        .userEmail("user3@test.com")
        .name("User3")
        .nickname("user3")
        .phone("010-3333-3333")
        .role(UserRoleEnum.CUSTOMER)
        .build();
  }

  @Test
  @DisplayName("전체 회원 조회 성공 테스트 - 전체 조회")
  void getAllUsers_success_isDeletedNull() {
    // Given
    Page<User> userPage = new PageImpl<>(
        Arrays.asList(user1, user2, user3),
        PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate")),
        3
    );

    when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

    // When
    Page<UserResponseDto> result = userService.getAllUsers(null, 0, 10, "createdDate", true, null);

    // Then
    System.out.println("조회 결과 : " + result);
    result.getContent().forEach(dto -> {
      System.out.println("회원 이메일: " + dto.getUserEmail() + ", isDeleted: " + dto.getIsDeleted());
    });

    assertThat(result).hasSize(3);
    assertEquals("user1@test.com", result.getContent().get(0).getUserEmail());
    assertEquals("user2@test.com", result.getContent().get(1).getUserEmail());
    assertEquals("user3@test.com", result.getContent().get(2).getUserEmail());
  }

  @Test
  @DisplayName("전체 회원 조회 성공 테스트 - isDeleted가 false인 회원 조회")
  void getAllUsers_success_isDeletedFalse() {
    // Given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate"));

    when(userRepository.findByIsDeleted(eq(false), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Pageable p = invocation.getArgument(1, Pageable.class);
          // 전체 목록에서 isDeleted가 false인 회원만 필터링
          var filtered = Arrays.asList(user1, user2, user3).stream()
              .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
              .toList();
          return new PageImpl<>(filtered, p, filtered.size());
        });

    // When
    Page<UserResponseDto> result = userService.getAllUsers(false, 0, 10, "createdDate", true, null);

    // Then
    System.out.println("조회 결과: " + result);
    result.getContent().forEach(dto -> {
      System.out.println("회원 이메일: " + dto.getUserEmail() + ", isDeleted: " + dto.getIsDeleted());
    });

    assertThat(result).hasSize(2);
    assertEquals("user1@test.com", result.getContent().get(0).getUserEmail());
    assertEquals("user3@test.com", result.getContent().get(1).getUserEmail());
  }

  @Test
  @DisplayName("전체 회원 조회 성공 테스트 - isDeleted가 true안 회원 조회")
  void getAllUsers_success_isDeletedTrue() {
    // Given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate"));

    when(userRepository.findByIsDeleted(eq(true), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Pageable p = invocation.getArgument(1, Pageable.class);
          // 전체 목록에서 isDeleted가 true인 회원만 필터링
          var filtered = Arrays.asList(user1, user2, user3).stream()
              .filter(user -> !Boolean.FALSE.equals(user.getIsDeleted()))
              .toList();
          return new PageImpl<>(filtered, p, filtered.size());
        });

    // When
    Page<UserResponseDto> result = userService.getAllUsers(false, 0, 10, "createdDate", true, null);

    // Then
    System.out.println("조회 결과: " + result);
    result.getContent().forEach(dto -> {
      System.out.println("회원 이메일: " + dto.getUserEmail() + ", isDeleted: " + dto.getIsDeleted());
    });

    assertThat(result).hasSize(1);
    assertEquals("user2@test.com", result.getContent().get(0).getUserEmail());
  }

  @Test
  @DisplayName("특정 권한을 가진 회원 조회 성공 테스트")
  void getAllUsers_success_isRole() {
    // Given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate"));

    when(userRepository.findByRole(eq(UserRoleEnum.CUSTOMER), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Pageable p = invocation.getArgument(1, Pageable.class);
          // 전체 목록에서 role이 CUSTOMER인 회원만 필터링
          var filtered = Arrays.asList(user1, user2, user3).stream()
              .filter(user -> user.getRole().equals(UserRoleEnum.CUSTOMER))
              .toList();
          return new PageImpl<>(filtered, p, filtered.size());
        });

    // When
    Page<UserResponseDto> result = userService.getAllUsers(null, 0, 10, "createdDate", true, UserRoleEnum.CUSTOMER);

    // Then
    System.out.println("조회 결과: " + result);
    result.getContent().forEach(dto -> {
      System.out.println("회원 이메일: " + dto.getUserEmail() + ", role: " + dto.getRole());
    });

    assertThat(result).hasSize(2);
    assertEquals("user1@test.com", result.getContent().get(0).getUserEmail());
    assertEquals("user3@test.com", result.getContent().get(1).getUserEmail());
  }

  @Test
  @DisplayName("특정 권한 & 탈퇴 여부 기반 회원 조회 성공 테스트")
  void getAllUsers_success_isDeleted_isRole() {
    // Given
    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdDate"));

    // isDeleted가 false이고, role이 CUSTOMER인 경우
    when(userRepository.findByIsDeletedAndRole(eq(false), eq(UserRoleEnum.CUSTOMER), any(Pageable.class)))
        .thenAnswer(invocation -> {
          Pageable p = invocation.getArgument(2, Pageable.class);
          // isDeleted가 false이고 role이 CUSTOMER인 회원만 필터링
          var filtered = Arrays.asList(user1, user2, user3).stream()
              .filter(user -> Boolean.FALSE.equals(user.getIsDeleted())
                  && user.getRole().equals(UserRoleEnum.CUSTOMER))
              .toList();
          return new PageImpl<>(filtered, p, filtered.size());
        });

    // When
    Page<UserResponseDto> result = userService.getAllUsers(false, 0, 10, "createdDate", true, UserRoleEnum.CUSTOMER);

    // Then
    System.out.println("조회 결과: " + result);
    result.getContent().forEach(dto -> {
      System.out.println("회원 이메일: " + dto.getUserEmail() + ", isDeleted: " + dto.getIsDeleted());
    });

    assertThat(result).hasSize(2);
    assertEquals("user1@test.com", result.getContent().get(0).getUserEmail());
    assertEquals("user3@test.com", result.getContent().get(1).getUserEmail());
  }

  @Test
  @DisplayName("전체 회원 조회 실패 테스트- 전체 회원이 없을 경우")
  void getAllUsers_empty() {
    // Given
    Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
    when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

    // When & Then
    UserListNotFoundException exception = assertThrows(UserListNotFoundException.class,
        () -> userService.getAllUsers(null, 0, 10, "createdDate", true, null));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }


  @Test
  @DisplayName("특정 회원 조회 성공 테스트")
  void getUserByUserEmail_success() {
    // Given
    when(userRepository.findByUserEmail("user1@test.com")).thenReturn(Optional.of(user1));

    // When
    UserResponseDto dto = userService.getUserByUserEmail("user1@test.com");

    // Then
    System.out.println("회원 이메일: " + dto.getUserEmail());
    assertEquals("user1@test.com", dto.getUserEmail());
    assertEquals("user1", dto.getNickname());
  }

  @Test
  @DisplayName("특정 회원 조회 실패 테스트 - 존재하지 않는 경우")
  void getUserByUserEmail_notFound() {
    // Given
    when(userRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

    // When & Then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userService.getUserByUserEmail("none@test.com"));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("회원 정보 업데이트 - 성공")
  void updateUser_success() {
    // Given
    when(userRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));

    UserUpdateRequestDto updateRequest = UserUpdateRequestDto.builder()
        .password("newPass")
        .nickname("newNick")
        .phone("010-1111-2222")
        .build();

    when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
    when(userRepository.existsByNickname("newNick")).thenReturn(false);

    // When
    UserResponseDto response = userService.updateUser("user1@test.com", updateRequest);

    // Then
    assertEquals("user1@test.com", response.getUserEmail());
    assertEquals("newNick", response.getNickname());
    assertEquals("010-1111-2222", response.getPhone());
    System.out.println(
        "업데이트 성공: " + response.getUserEmail() + ", " + response.getNickname() + ", " + response.getPhone());
  }

  @Test
  @DisplayName("회원 정보 업데이트 - 필드가 빈값일 경우")
  void updateUser_noUpdateFields() {
    // Given
    when(userRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));

    UserUpdateRequestDto updateRequest = UserUpdateRequestDto.builder()
        .password("")
        .nickname("")
        .phone("")
        .build();

    // When & Then
    UserNoFieldUpdatedException exception = assertThrows(UserNoFieldUpdatedException.class,
        () -> userService.updateUser("user1@test.com", updateRequest));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }

  @Test
  @DisplayName("회원 정보 업데이트 - 닉네임 중복 시 실패")
  void updateUser_duplicateNickname() {
    // Given
    when(userRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));

    UserUpdateRequestDto updateRequest = UserUpdateRequestDto.builder()
        .nickname("duplicateNick")
        .build();
    when(userRepository.existsByNickname("duplicateNick")).thenReturn(true);

    // When & Then
    UserDuplicateNicknameException exception = assertThrows(UserDuplicateNicknameException.class,
        () -> userService.updateUser("user1@test.com", updateRequest));

    System.out.println("Expected exception occurred: " + exception.getMessage());
  }


  @Test
  @DisplayName("회원 탈퇴 - 성공")
  void softDeleteUser_success() {
    // Given
    when(userRepository.findByUserEmail("user1@test.com")).thenReturn(Optional.of(user1));

    // When
    userService.softDeleteUser("user1@test.com", "user1@test.com");

    // Then
    verify(userRepository).findByUserEmail("user1@test.com");
    assertTrue(user1.getIsDeleted());
    System.out.println("회원 탈퇴 후 isDeleted: " + true);
  }

}
