package run.bemin.api.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import run.bemin.api.auth.service.AuthService;
import run.bemin.api.auth.util.JwtUtil;
import run.bemin.api.user.service.UserAddressService;
import run.bemin.api.user.service.UserService;

@TestConfiguration
public class MockConfig {
  @Bean
  public AuthService authService() {
    return Mockito.mock(AuthService.class);
  }

  @Bean
  public JwtUtil jwtUtil() {
    return Mockito.mock(JwtUtil.class);
  }

  @Bean
  public RedisTemplate redisTemplate() {
    return Mockito.mock(RedisTemplate.class);
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
