package run.bemin.api.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.user.service.UserAddressService;
import run.bemin.api.user.service.UserService;

@TestConfiguration
public class MockConfig {
  @Bean
  public AuthService authService() {
    return Mockito.mock(AuthService.class);
  }

  @Bean
  public UserService userService() {
    return Mockito.mock(UserService.class);
  }

  @Bean
  public UserAddressService userAddressService() {
    return Mockito.mock(UserAddressService.class);
  }
}
