package run.bemin.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import run.bemin.api.auth.jwt.JwtUtil;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsServiceImpl userDetailsService;
  private final RedisTemplate<String, Object> redisTemplate;

  public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService,
                                RedisTemplate<String, Object> redisTemplate) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
      throws ServletException, IOException {

    String tokenValue = jwtUtil.getTokenFromHeader(JwtUtil.AUTHORIZATION_HEADER, req);

    if (tokenValue != null && jwtUtil.validateToken(tokenValue)) {
      // 토큰의 남은 유효시간 10분 이하이면 경고 로그
      long remainingTime = jwtUtil.getRemainingExpirationTime(tokenValue);
      if (remainingTime <= 10 * 60 * 1000L) {
        log.warn("Token will expire in {} seconds", remainingTime / 1000);
        res.addHeader("X-Token-Remaining", String.valueOf(remainingTime));
      }

      // Redis 블랙리스트 체크
      String isLogout = (String) redisTemplate.opsForValue().get(tokenValue);
      if (isLogout == null) { // 블랙리스트에 없다면 정상 인증
        String username = jwtUtil.getUserEmailFromToken(tokenValue);
        setAuthentication(username);
      } else {
        // 블랙리스트에 있다면 인증을 무시하거나 오류 응답 처리
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("{\"message\": \"로그아웃된 토큰입니다.\"}");
        return;
      }
    }

    filterChain.doFilter(req, res);
  }

  // 인증 처리
  public void setAuthentication(String username) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication = createAuthentication(username);
    context.setAuthentication(authentication);

    SecurityContextHolder.setContext(context);
  }

  // 인증 객체 생성
  private Authentication createAuthentication(String username) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }
}