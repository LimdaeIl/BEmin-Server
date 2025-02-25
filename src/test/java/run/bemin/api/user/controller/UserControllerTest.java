package run.bemin.api.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import run.bemin.api.config.MockConfig;
import run.bemin.api.config.TestSecurityConfig;
import run.bemin.api.config.WebSecurityConfig;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.dto.request.UserAddressRequestDto;
import run.bemin.api.user.dto.request.UserUpdateRequestDto;
import run.bemin.api.user.dto.response.UserAddressResponseDto;
import run.bemin.api.user.dto.response.UserResponseDto;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserAddress;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.service.UserAddressService;
import run.bemin.api.user.service.UserService;

@WebMvcTest(
    controllers = UserController.class,
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurityConfig.class
        )
    }
)
@Import({MockConfig.class, TestSecurityConfig.class})
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private UserAddressService userAddressService;


  @Test
  @DisplayName("전체 사용자 조회 테스트 - MASTER 권한")
  void getAllUsersSuccessTest() throws Exception {
    // Given
    UserResponseDto userDto1 = new UserResponseDto("user1@test.com", "User1", "user1", "010-1111-1111", null,
        UserRoleEnum.CUSTOMER, true);
    UserResponseDto userDto2 = new UserResponseDto("user2@test.com", "User2", "user2", "010-2222-2222", null,
        UserRoleEnum.MASTER, false);
    Page<UserResponseDto> page = new PageImpl<>(Arrays.asList(userDto1, userDto2),
        PageRequest.of(0, 10, Sort.by("createdAt")), 2);

    when(userService.getAllUsers(null, 0, 10, "createdAt", true, null))
        .thenReturn(page);

    // When & Then
    mockMvc.perform(get("/api/users")
            .with(csrf())
            .with(user("masterUser").roles("MASTER"))
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content.length()").value(2))
        .andDo(print());
  }

  @Test
  @DisplayName("내 정보 조회 테스트")
  void myInfoSuccessTest() throws Exception {
    // Given
    String addressId = UUID.randomUUID().toString();
    UserResponseDto dto = new UserResponseDto(
        "user1@test.com",
        "user1",
        "nickname",
        "010-1111-1111",
        addressId,
        UserRoleEnum.CUSTOMER,
        false);

    when(userService.getUserByUserEmail("user1@test.com")).thenReturn(dto);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    // When & Then
    mockMvc.perform(get("/api/users/my-info")
            .with(csrf())
            .with(user(userDetails))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.userEmail").value("user1@test.com"))
        .andDo(print());
  }

  @Test
  @DisplayName("내 정보 수정 테스트")
  void myInfoupdateTest() throws Exception {
    String addressId = UUID.randomUUID().toString();
    UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
        .nickname("newnick")
        .phone("010-1111-2222")
        .build();

    UserResponseDto updatedDto = new UserResponseDto(
        "user1@test.com",
        "user1",
        "newnick",
        "010-1111-2222",
        addressId,
        UserRoleEnum.CUSTOMER,
        false);

    when(userService.updateUser(eq("user1@test.com"), any(UserUpdateRequestDto.class)))
        .thenReturn(updatedDto);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    mockMvc.perform(put("/api/users/my-info")
            .with(csrf())
            .with(user(userDetails))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.userEmail").value("user1@test.com"))
        .andExpect(jsonPath("$.data.phone").value(requestDto.getPhone()))
        .andDo(print());
  }

  @Test
  @DisplayName("특정 회원 조회 테스트 - MASTER 권한")
  void testGetUserByUserEmail() throws Exception {
    String addressId = UUID.randomUUID().toString();
    UserResponseDto dto = new UserResponseDto(
        "user1@test.com", "user1", "user1", "010-3333-3333", addressId, UserRoleEnum.CUSTOMER, false);
    when(userService.getUserByUserEmail("user1@test.com")).thenReturn(dto);

    mockMvc.perform(get("/api/users/user1@test.com")
            .with(csrf())
            .with(user("master@test.com").roles("MASTER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andExpect(jsonPath("$.data.userEmail").value("user1@test.com"))
        .andDo(print());
  }

  @Test
  @DisplayName("특정 회원 수정 테스트")
  void testUpdateUser() throws Exception {
    String addressId = UUID.randomUUID().toString();
    UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
        .nickname("newnick")
        .phone("010-4444-4444")
        .build();

    UserResponseDto updatedDto = new UserResponseDto(
        "user1@test.com",
        "newname",
        "newnick",
        "010-4444-4444",
        addressId,
        UserRoleEnum.CUSTOMER, false);
    when(userService.updateUser(eq("user1@test.com"), any(UserUpdateRequestDto.class)))
        .thenReturn(updatedDto);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    mockMvc.perform(put("/api/users/user@test.com")
            .with(csrf())
            .with(user(userDetails))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userEmail").value("user1@test.com"))
        .andExpect(jsonPath("$.name").value("newname"))
        .andExpect(jsonPath("$.nickname").value("newnick"))
        .andExpect(jsonPath("$.phone").value("010-4444-4444"))
        .andDo(print());
  }


  @Test
  @DisplayName("회원 소프트 삭제 테스트")
  void testSoftDeleteUser() throws Exception {
    // userService.softDeleteUser(...) 호출 시 deletedUserDto 반환
    doNothing().when(userService).softDeleteUser("user1@test.com", "user@test.com");

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();
    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    mockMvc.perform(delete("/api/users/user1@test.com")
            .with(csrf())
            .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("성공"))
        .andDo(print());

  }


  @Test
  @DisplayName("특정 사용자 주소 전체 조회 테스트")
  void testGetAddresses() throws Exception {
    UUID randomRepresentativeAddress = UUID.randomUUID();
    UserAddress userAddress1 = UserAddress.builder()
        .id(randomRepresentativeAddress)
        .bcode("12345")
        .jibunAddress("서울시 강남구 97-45")
        .roadAddress("서울시 강남구 테헤란로 40")
        .detail("101호")
        .isRepresentative(true)
        .build();

    UserAddress userAddress2 = UserAddress.builder()
        .id(randomRepresentativeAddress)
        .bcode("0000")
        .jibunAddress("서울시 강남구 123-45")
        .roadAddress("서울시 강남구 테헤란로 10")
        .detail("101호")
        .isRepresentative(false)
        .build();

    UserAddressResponseDto addressDto1 = UserAddressResponseDto.fromEntity(userAddress1);
    UserAddressResponseDto addressDto2 = UserAddressResponseDto.fromEntity(userAddress2);

    Page<UserAddressResponseDto> page = new PageImpl<>(Arrays.asList(addressDto1, addressDto2),
        PageRequest.of(0, 10, Sort.by("createdAt")), 2);

    when(userAddressService.getAllAddresses("user1@test.com", false, 0, 10))
        .thenReturn(page);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    mockMvc.perform(get("/api/users/user1@test.com/addresses")
            .with(csrf())
            .with(user(userDetails))
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("주소 목록 조회 성공"))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.page.totalElements").value(2))
        .andDo(print());
  }

  @Test
  @DisplayName("배달 주소 추가 테스트")
  void testAddAddress() throws Exception {
    // Given
    UserAddressRequestDto requestDto = UserAddressRequestDto.builder()
        .roadAddress("서울시 강남구 테헤란로 10")
        .jibunAddress("서울시 강남구 123-45")
        .bcode("00000")
        .detail("101호")
        .build();
    //새로 추가 되는 주소 (대표 주소로 자동 등록)
    UserAddress dummyAddress = UserAddress.builder()
        .id(UUID.randomUUID())
        .bcode(requestDto.getBcode())
        .jibunAddress(requestDto.getJibunAddress())
        .roadAddress(requestDto.getRoadAddress())
        .detail(requestDto.getDetail())
        .isRepresentative(true)
        .build();
    UserAddressResponseDto responseDto = UserAddressResponseDto.fromEntity(dummyAddress);

    // When
    when(userAddressService.addAddress(eq("user1@test.com"), any(UserAddressRequestDto.class)))
        .thenReturn(responseDto);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    // Then
    mockMvc.perform(post("/api/users/user1@test.com/addresses")
            .with(csrf())
            .with(user(userDetails))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("배달 주소 추가 성공"))
        .andExpect(jsonPath("$.data.representative").value(true)) // 새로 추가된 주소가 대표 주소로 설정되었는지 확인
        .andExpect(jsonPath("$.data.roadAddress").value(requestDto.getRoadAddress()))
        .andDo(print());
  }


  @Test
  @DisplayName("대표 배달 주소로 변경 테스트")
  void testSetRepresentativeAddress() throws Exception {
    // Give
    UUID addressId = UUID.randomUUID();

    UserAddress dummyAddress = UserAddress.builder()
        .id(addressId)
        .bcode("12345")
        .jibunAddress("서울시 강남구 97-45")
        .roadAddress("서울시 강남구 테헤란로 40")
        .detail("101호")
        .isRepresentative(true)
        .build();
    UserAddressResponseDto updatedAddressDto = UserAddressResponseDto.fromEntity(dummyAddress);

    // When
    when(userAddressService.setRepresentativeAddress("user1@test.com", addressId))
        .thenReturn(updatedAddressDto);

    User mockUser = User.builder()
        .userEmail("user1@test.com")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);

    // Then
    mockMvc.perform(put("/api/users/user1@test.com/addresses/" + addressId + "/representative")
            .with(csrf())
            .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("대표 주소로 변경 성공"))
        .andExpect(jsonPath("$.data.userAddressId").value(addressId.toString()))
        .andDo(print());
  }

}

