package run.bemin.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import run.bemin.api.auth.jwt.JwtUtil;
import run.bemin.api.security.JwtAuthorizationFilter;
import run.bemin.api.security.UserDetailsServiceImpl;


@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtUtil jwtUtil;
  private final UserDetailsServiceImpl userDetailsService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public JwtAuthorizationFilter jwtAuthorizationFilter() {
    return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // CSRF 설정
    http.csrf((csrf) -> csrf.disable());

    // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
    http.sessionManagement((sessionManagement) ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.authorizeHttpRequests((authorizeHttpRequests) ->
        authorizeHttpRequests
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/email/**").permitAll()
            .requestMatchers("/api/users/nickname/**").permitAll()
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated() // 그 외 모든 요청 인증처리
    );

    // 필터 관리
    http.addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);
    http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}